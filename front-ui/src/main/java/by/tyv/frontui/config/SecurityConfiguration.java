package by.tyv.frontui.config;

import by.tyv.frontui.service.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain webFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.GET, "/", "/signup", "/actuator/health/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/signup").permitAll()
                        .pathMatchers(HttpMethod.GET, "/main").hasAuthority("SCOPE_user")
                        .pathMatchers(HttpMethod.POST, "/user/**").hasAuthority("SCOPE_user")
                        .anyExchange().hasAuthority("SCOPE_internal_call"))
                .oauth2ResourceServer(spec -> spec.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository regs,
                                                                         ReactiveOAuth2AuthorizedClientService svc) {

        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(regs, svc);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    public TokenProvider tokenProvider(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return new TokenProvider() {
            static private final String USERNAME = "preferred_username";
            static private Authentication principal() {
                return new UsernamePasswordAuthenticationToken("scheduler", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")));
            }

            @Override
            public Mono<String> getCurrent() {
                return getContext().map(SecurityContext::getAuthentication)
                        .cast(JwtAuthenticationToken.class)
                        .map(token -> token.getToken().getTokenValue());
            }

            @Override
            public Mono<String> getCurrentUsername() {
                return getContext().map(SecurityContext::getAuthentication)
                        .cast(JwtAuthenticationToken.class)
                        .map(token -> token.getToken().getClaims().get(USERNAME))
                        .map(String::valueOf);
            }

            @Override
            public Mono<String> getNewTechnical() {
                OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("bank-services-cc")
                        .attributes(attributes -> attributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME, new String[]{"internal_call"}))
                        .principal(principal())
                        .build();

                return authorizedClientManager.authorize(request)
                        .switchIfEmpty(Mono.error(new IllegalStateException("OAuth client not authorized")))
                        .map(client -> client.getAccessToken().getTokenValue());
            }
        };
    }
}

package by.tyv.exchangegenerator.config;

import by.tyv.exchangegenerator.service.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

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
            static private Authentication principal() {
                return new UsernamePasswordAuthenticationToken("scheduler", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")));
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

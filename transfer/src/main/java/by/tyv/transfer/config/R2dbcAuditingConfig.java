package by.tyv.transfer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@ConditionalOnProperty(name="application.r2dbc.auditing.enable", havingValue="true")
@EnableR2dbcAuditing
class R2dbcAuditingConfig { }

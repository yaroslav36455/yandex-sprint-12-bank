package by.tyv.account.config;

import by.tyv.account.enums.CurrencyCode;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(R2dbcTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @Bean
    public R2dbcCustomConversions getCustomConverters(ConnectionFactory connectionFactory) {
        R2dbcDialect r2dbcDialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(r2dbcDialect, List.of(
                new CurrencyCodeReadConverter(),
                new CurrencyCodeWriteConverter())
        );
    }


    @ReadingConverter
    private static class CurrencyCodeReadConverter implements Converter<String, CurrencyCode> {

        @Override
        public CurrencyCode convert(String source) {
            return CurrencyCode.valueOf(source);
        }
    }

    @WritingConverter
    private static class CurrencyCodeWriteConverter implements Converter<CurrencyCode, String> {

        @Override
        public String convert(CurrencyCode source) {
            return source.name();
        }
    }
}

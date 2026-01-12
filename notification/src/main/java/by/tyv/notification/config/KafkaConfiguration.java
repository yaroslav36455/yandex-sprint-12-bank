package by.tyv.notification.config;

import by.tyv.notification.model.dto.NotificationDto;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import static by.tyv.notification.listener.NotificationListener.DLC_ENDING;

@Configuration
public class KafkaConfiguration {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template,
                                            @Value("${kafka.retries}") int retries,
                                            @Value("${kafka.backoff.initial}") long initial,
                                            @Value("${kafka.backoff.multiplier}") double multiplier,
                                            @Value("${kafka.backoff.max}") long max) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template, (rec, ex) ->
                        new TopicPartition(rec.topic() + DLC_ENDING, rec.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retries);
        backOff.setInitialInterval(initial);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(max);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.addNotRetryableExceptions(IllegalArgumentException.class);

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationDto> notificationKafkaListenerFactory(
            ConsumerFactory<String, NotificationDto> consumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}

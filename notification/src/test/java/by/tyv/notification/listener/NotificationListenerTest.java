package by.tyv.notification.listener;

import by.tyv.notification.model.dto.NotificationDto;
import by.tyv.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.Map;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",})
@EmbeddedKafka(partitions = 1, topics = {
        NotificationListener.NOTIFICATION_TOPIC,
        NotificationListener.NOTIFICATION_TOPIC_DLQ
})
public class NotificationListenerTest {

    @MockitoSpyBean
    private NotificationService notificationService;
    @Autowired
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;


    @Test
    public void shouldProcessMessageSuccessfully_withoutDlq() {
        kafkaTemplate.send(NotificationListener.NOTIFICATION_TOPIC, "key-1", new NotificationDto("user-1", "ok"));

        System.out.println("Embedded brokers: " + embeddedKafka.getBrokersAsString());

        // проверяем что обработчик вызвался (ждём немного, т.к. async)
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                Mockito.verify(notificationService).handleMessage("user-1", "ok")
        );

        // проверяем что в DLQ ничего не прилетело
        try (Consumer<String, NotificationDto> consumer = dlqConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, NotificationListener.NOTIFICATION_TOPIC_DLQ);
            ConsumerRecords<String, NotificationDto> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofMillis(300));
            Assertions.assertThat(records.count()).isEqualTo(0);
        }
    }

    @Test
    public void shouldSendToDlq_afterRetries() {
        Mockito.doThrow(new RuntimeException("Неизвестная ошибка"))
                .when(notificationService).handleMessage("user-2", "fail");

        kafkaTemplate.send(NotificationListener.NOTIFICATION_TOPIC, "key-2", new NotificationDto("user-2", "fail"));

        // ждём сообщение в DLQ
        try (Consumer<String, NotificationDto> consumer = dlqConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, NotificationListener.NOTIFICATION_TOPIC_DLQ);

            ConsumerRecord<String, NotificationDto> dlq =
                    KafkaTestUtils.getSingleRecord(consumer, NotificationListener.NOTIFICATION_TOPIC_DLQ, Duration.ofSeconds(5));

            Assertions.assertThat(dlq.value().getLogin()).isEqualTo("user-2");
            Assertions.assertThat(dlq.value().getMessage()).isEqualTo("fail");
        }

        // и проверяем, что были ретраи: вызовов будет 1 + retries
        Mockito.verify(notificationService, Mockito.atLeast(2)).handleMessage("user-2", "fail");
    }

    private Consumer<String, NotificationDto> dlqConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("dlq-test-group", "true", embeddedKafka);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationDto.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "by.tyv.notification.model.dto");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new KafkaConsumer<>(props);
    }
}

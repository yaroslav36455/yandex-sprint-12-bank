package by.tyv.account.service;

import by.tyv.account.config.TestcontainersConfiguration;
import by.tyv.account.enums.MessageStatus;
import by.tyv.account.model.dto.NotificationDto;
import by.tyv.account.repository.DeferredNotificationRepository;
import by.tyv.account.service.impl.NotificationServiceImpl;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = { NotificationService.NOTIFICATION_TOPIC })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false"
})
public class NotificationServiceTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    private Map<String, Object> props;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DeferredNotificationRepository deferredNotificationRepository;

    @BeforeEach
    public void beforeEach() {
        props = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationDto.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    }

    @Test
    @Sql({"/sql/clean.sql", "/sql/insert_deferred_notifications.sql"})
    public void sendNotification() {
        List<NotificationDto> notificationDtoList = new ArrayList<>();
        try (Consumer<String, NotificationDto> consumer = new KafkaConsumer<>(props)) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, NotificationService.NOTIFICATION_TOPIC);

            StepVerifier.create(notificationService.sendCreatedNotifications())
                    .verifyComplete();

            ConsumerRecords<String, NotificationDto> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            Assertions.assertThat(records.count()).isEqualTo(4);
            records.records(NotificationService.NOTIFICATION_TOPIC)
                    .forEach(record -> notificationDtoList.add(record.value()));
        }

        Assertions.assertThat(notificationDtoList)
                .containsExactlyInAnyOrder(
                        new NotificationDto("user-1", "Сообщение-1"),
                        new NotificationDto("user-2", "Сообщение-1"),
                        new NotificationDto("user-1", "Сообщение-3"),
                        new NotificationDto("user-3", "Сообщение-1")
                );

        StepVerifier.create(deferredNotificationRepository.findAll())
                .recordWith(ArrayList::new)
                .thenConsumeWhile(e -> true)
                .consumeRecordedWith(list ->
                        Assertions.assertThat(list)
                                .filteredOn(e -> e.getStatus().equals(MessageStatus.CREATED.toString()))
                                .isEmpty()
                )
                .verifyComplete();
    }
}

package by.tyv.notification.listener;

import by.tyv.notification.model.dto.NotificationDto;
import by.tyv.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {
    static public final String NOTIFICATION_TOPIC = "notification_topic";
    static public final String DLC_ENDING = ".dlq";
    static public final String NOTIFICATION_TOPIC_DLQ = NOTIFICATION_TOPIC + DLC_ENDING;

    private final NotificationService notificationService;

    @KafkaListener(topics = NOTIFICATION_TOPIC, containerFactory = "notificationKafkaListenerFactory")
    public void onMessage(NotificationDto notificationDto,
                          Acknowledgment ack,
                          @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            notificationService.handleMessage(notificationDto.getLogin(), notificationDto.getMessage());
            ack.acknowledge();
        } catch (Throwable e) {
            log.error("Ошибка обработки сообщения key={}", key, e);
            throw e;
        }
    }

    @KafkaListener(topics = NOTIFICATION_TOPIC_DLQ, containerFactory = "notificationKafkaListenerFactory")
    public void onDlq(NotificationDto dto,
                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                      @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
        log.error("DLQ сообщение из {} error={} dto={}", topic, error, dto);
    }
}

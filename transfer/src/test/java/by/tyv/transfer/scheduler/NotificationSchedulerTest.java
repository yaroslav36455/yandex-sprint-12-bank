package by.tyv.transfer.scheduler;

import by.tyv.transfer.SpringBootIntegrationTest;
import by.tyv.transfer.enums.MessageStatus;
import by.tyv.transfer.model.entity.DeferredNotificationEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;

public class NotificationSchedulerTest extends SpringBootIntegrationTest {

    @Test
    @DisplayName("Отправка нотификаций, все неотправленные раннее нотификации отправлены успешно")
    @Sql({"/sql/clean.sql", "/sql/insert_deferred_notifications.sql"})
    public void sendAllScheduledNotifications() {
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция выполнена успешно"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-2").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция запрещена"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-3").toUriString()))
                .withRequestBody(WireMock.equalTo("Ошибка операции: Недостаточно денег на счету"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        notificationScheduler.sendNotifications();

        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция выполнена успешно")));
        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-2").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция запрещена")));
        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-3").toUriString()))
                .withRequestBody(WireMock.equalTo("Ошибка операции: Недостаточно денег на счету")));

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(notifications -> {
                    Assertions.assertThat(notifications).hasSize(5);
                    Assertions.assertThat(notifications).map(DeferredNotificationEntity::getStatus).containsOnly(MessageStatus.SENT.toString());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Отправка нотификаций, лишь некоторые нотификации были отправлены успешно")
    @Sql({"/sql/clean.sql", "/sql/insert_deferred_notifications.sql"})
    public void sendPartialScheduledNotifications() {
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция выполнена успешно"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-2").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция запрещена"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        wireMockServerNotification.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-3").toUriString()))
                .withRequestBody(WireMock.equalTo("Ошибка операции: Недостаточно денег на счету"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        notificationScheduler.sendNotifications();

        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция выполнена успешно")));
        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-2").toUriString()))
                .withRequestBody(WireMock.equalTo("Операция запрещена")));
        wireMockServerNotification.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/notifications/{login}/message").buildAndExpand("username-3").toUriString()))
                .withRequestBody(WireMock.equalTo("Ошибка операции: Недостаточно денег на счету")));

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(notifications -> {
                    Assertions.assertThat(notifications).hasSize(5);
                    Assertions.assertThat(notifications)
                            .filteredOn(e -> !e.getLogin().equals("username-2"))
                            .map(DeferredNotificationEntity::getStatus)
                            .containsOnly(MessageStatus.SENT.toString());
                    Assertions.assertThat(notifications)
                            .filteredOn(e -> e.getLogin().equals("username-2"))
                            .singleElement()
                            .extracting(DeferredNotificationEntity::getStatus)
                            .isEqualTo(MessageStatus.CREATED.toString());
                })
                .verifyComplete();
    }
}

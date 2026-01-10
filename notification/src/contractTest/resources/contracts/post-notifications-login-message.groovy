package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /notifications/{login}/message вызван, сервис имитирует отправку сообщения и возвращает статус 200 OK"
    request {
        method 'POST'
        urlPath($(consumer(regex('/notifications/[A-Za-z0-9._-]+/message')), producer('/notifications/username/message')))
        headers {
            contentType(textPlain())
        }
        body(
                'Операция выполнена успешно'
        )
    }
    response {
        status 200
    }
}
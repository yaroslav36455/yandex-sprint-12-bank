package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /cash/username вызван, сервис возвращает 200 OK"
    request {
        method 'POST'
        urlPath($(consumer(regex('/cash/[A-Za-z0-9._-]+')), producer('/cash/username')))
        headers {
            contentType(applicationJson())
        }
        body(
                currency: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('BYN')),
                value:    $(consumer(anyNumber()), producer(123.45)),
                action:   $(consumer(regex('(PUT|GET)')), producer('PUT'))
        )
    }
    response {
        status 200
    }
}
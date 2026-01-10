package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /transfer/username вызван, сервис возвращает 200 OK"
    request {
        method 'POST'
        urlPath($(consumer(regex('/transfer/[A-Za-z0-9._-]+')), producer('/transfer/sourceUsername')))
        headers {
            contentType(applicationJson())
        }
        body(
                action:         $(consumer(regex('[A-Za-z0-9._-]+')), producer('targetUsername')),
                sourceCurrency: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('BYN')),
                targetCurrency: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('CNY')),
                sourceAmount:   $(consumer(anyNumber()), producer(123.45))
        )
    }
    response {
        status 200
    }
}
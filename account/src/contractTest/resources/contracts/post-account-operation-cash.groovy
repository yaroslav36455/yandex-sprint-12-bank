package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /account/{login}/operation/cash вызван, сервис выполняет операцию с кэшем и возвращает статус 200 OK"
    request {
        method 'POST'
        urlPath($(consumer(regex('/account/[A-Za-z0-9._-]+/operation/cash')), producer('/account/username/operation/cash')))
        headers {
            contentType(applicationJson())
        }
        body(
                currency: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('BYN')),
                action:   $(consumer(regex('(PUT|GET)')),             producer('PUT')),
                amount:   $(consumer(regex('^(?:0|[1-9]\\d*)(?:\\.\\d+)?$')), producer(123.45))
        )
    }
    response {
        status 200
    }
}
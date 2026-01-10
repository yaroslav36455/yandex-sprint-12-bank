package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /operations/available/cash вызван, сервис выполняет доступность операции и возвращает ответ и статус 200 OK"
    request {
        method 'POST'
        url '/operations/available/cash'
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(
                currency: 'BYN',
                action:   'PUT',
                amount:   123.45
        )
        bodyMatchers {
            jsonPath('$.currency', byRegex('(RUB|BYN|IRR|CNY|INR)'))
            jsonPath('$.action', byRegex('(PUT|GET)'))
            jsonPath('$.amount', byRegex('\\d+(\\.\\d+)?'))
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body(
                isAvailable: true
        )
        bodyMatchers {
            jsonPath('$.isAvailable', byEquality())
        }
    }
}
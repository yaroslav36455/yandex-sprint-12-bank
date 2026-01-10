package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /operations/available/transfer вызван, сервис выполняет доступность операции и возвращает ответ и статус 200 OK"
    request {
        method 'POST'
        url '/operations/available/transfer'
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(
                sourceCurrency: 'BYN',
                targetCurrency: 'CNY',
                sourceAmount:   123.45
        )
        bodyMatchers {
            jsonPath('$.sourceCurrency', byRegex('(RUB|BYN|IRR|CNY|INR)'))
            jsonPath('$.targetCurrency', byRegex('(RUB|BYN|IRR|CNY|INR)'))
            jsonPath('$.sourceAmount',   byRegex('\\d+(\\.\\d+)?'))
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
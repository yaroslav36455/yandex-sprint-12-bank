package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда GET /api/rates вызван, сервис возвращает список валют и статус 200 OK"
    request {
        method 'GET'
        url '/api/rates'
        headers {
            accept(applicationJson())
        }
    }
    response {
        status 200
        headers { contentType(applicationJson()) }
        body([
                [ title: 'Белорусский Рубль', value: '123.45', name: 'BYN' ],
                [ title: 'Российский Рубль',  value: '98.76', name: 'RUB' ]
        ])
        bodyMatchers {
            jsonPath('$[*].title', byRegex('[\\p{L}\\s-]+'))
            jsonPath('$[*].value', byRegex('\\d+\\.\\d{2}'))
            jsonPath('$[*].name', byRegex('(RUB|BYN|IRR|CNY|INR)'))
        }
    }
}
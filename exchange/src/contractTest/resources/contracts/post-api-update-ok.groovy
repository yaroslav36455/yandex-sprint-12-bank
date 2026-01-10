package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /api/update вызван, сервис возвращает 200 OK"
    request {
        method 'POST'
        url '/api/update'
        headers {
            contentType(applicationJson())
        }
        body([
                [ code: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('BYN')),
                  rate: $(consumer(anyNumber()),                          producer(123.45)) ],
                [ code: $(consumer(regex('(RUB|BYN|IRR|CNY|INR)')), producer('RUB')),
                  rate: $(consumer(anyNumber()),                          producer(98.76)) ]
        ])
        bodyMatchers {
            jsonPath('$[*].code', byRegex('(RUB|BYN|IRR|CNY|INR)'))
            jsonPath('$[*].rate', byRegex('\\d+(\\.\\d+)?'))
        }
    }
    response {
        status 200
    }
}
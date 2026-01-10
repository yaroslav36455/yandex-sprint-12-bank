package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда GET /account/{login} вызван, сервис возвращает все аккаунты пользователя и возвращает статус 200 OK"
    request {
        method 'GET'
        urlPath($(consumer(regex('/account/[A-Za-z0-9._-]+')), producer('/account/username')))
        headers {
            accept(applicationJson())
        }
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                [ id: '1234', createdAt: '2000-01-01T05:12:59.567344', userId: '4567', balance: '2000.00', currency: 'BYN'],
                [ id: '8888', createdAt: '1999-12-09T17:09:15.847234', userId: '4567', balance: '18066.12', currency: 'RUB'],
                [ id: '9876', createdAt: '2018-11-16T22:45:03.656234', userId: '4567', balance: '6788.55', currency: 'CNY'],
        ])
        bodyMatchers {
            jsonPath('$[*].id', byRegex('^(\\d+)$'))
            jsonPath('$[*].createdAt', byRegex('^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6})$'))
            jsonPath('$[*].userId', byRegex('^(\\d+)$'))
            jsonPath('$[*].balance', byRegex('^(?:0|[1-9]\\d*)(?:\\.\\d+)?$'))
            jsonPath('$[*].currency', byRegex('(RUB|BYN|IRR|CNY|INR)'))
        }
    }
}

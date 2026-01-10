package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда GET /user вызван, сервис возвращает всех пользователей и возвращает статус 200 OK"
    request {
        method 'GET'
        urlPath '/user'
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
                [ id: '1234', createdAt: '2000-01-01T05:12:59.567344', login: 'someLogin_1', name: 'Maria', birthDate: '1988-08-12'],
                [ id: '8888', createdAt: '1999-12-09T17:09:15.847234', login: 'someLogin_2', name: 'Zlata', birthDate: '2005-12-08'],
                [ id: '9876', createdAt: '2018-11-16T22:45:03.656234', login: 'someLogin_3', name: 'Yaroslav', birthDate: '1995-09-23']
        ])
        bodyMatchers {
            jsonPath('$[*].id', byRegex('^(\\d+)$'))
            jsonPath('$[*].createdAt', byRegex('^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6})$'))
            jsonPath('$[*].login', byRegex('^([A-Za-z0-9._-]+)$'))
            jsonPath('$[*].name', byRegex('^([A-Za-z\\s-]+)$'))
            jsonPath('$[*].birthDate', byRegex('^(\\d{4}-\\d{2}-\\d{2})$'))
        }
    }
}

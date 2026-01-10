package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /signup вызван, сервис создаёт нового пользователя и возвращает статус 201 CREATED"
    request {
        method 'POST'
        urlPath '/signup'
        headers {
            contentType(applicationJson())
        }
        body(
                login: 'someLogin_1',
                name: 'Maria',
                password: 'some_password1234',
                confirmPassword: 'some_password1234',
                birthDate: [1988, 8, 12])
        bodyMatchers {
            jsonPath('$.login', byRegex('^([A-Za-z0-9._-]+)$'))
            jsonPath('$.name', byRegex('^([A-Za-z\\s-]+)$'))
            jsonPath('$.password', byRegex('^([A-Za-z0-9._-]{6,})$'))
            jsonPath('$.confirmPassword', byRegex('^([A-Za-z0-9._-]{6,})$'))
            jsonPath('$.birthDate[0]', byRegex('\\d{4}'))
            jsonPath('$.birthDate[1]', byRegex('\\d{1,2}'))
            jsonPath('$.birthDate[2]', byRegex('\\d{1,2}'))
        }
    }
    response {
        status 201
    }
}

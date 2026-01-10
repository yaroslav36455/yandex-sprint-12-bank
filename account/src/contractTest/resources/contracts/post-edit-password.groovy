package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Когда POST /user/{login}/editPassword вызван, сервис меняет пароль пользователю и возвращает статус 200 OK"
    request {
        method 'POST'
        urlPath($(consumer(regex('/user/[A-Za-z0-9._-]+/editPassword')), producer('/user/username/editPassword')))
        headers {
            contentType(applicationJson())
        }
        body(
                password: 'some_new_password',
                confirmPassword: 'some_new_password'
        )
    }
    response {
        status 200
    }
}

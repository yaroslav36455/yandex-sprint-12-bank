## Функциональность

#### Микросервисное приложение «Банк» — это приложение с веб-интерфейсом, которое позволяет пользователю (клиенту банка):
* регистрироваться в системе по логину и паролю (заводить аккаунт);
* добавлять счета в различных валютах;
* класть виртуальные деньги на счёт и снимать их;
* переводить деньги между своими счетами с учётом конвертации в различные валюты;
* переводить деньги на другой счёт с учётом конвертации в различные валюты.

#### Приложение состоит из следующих микросервисов:
* фронта (Front UI);
* сервиса аккаунтов (Accounts);
* сервиса обналичивания денег (Cash);
* сервиса перевода денег между счетами одного или двух аккаунтов (Transfer);
* сервиса конвертации валют (Exchange);
* сервиса генерации курсов валют (Exchange Generator);
* сервиса блокировки подозрительных операций (Blocker);
* сервиса уведомлений (Notifications).

#### Приложение работает в Docker контейнерах следующих образов
* yandex-sprint-11-gateway
* yandex-sprint-11-keycloak
* yandex-sprint-11-account
* yandex-sprint-11-blocker
* yandex-sprint-11-cash
* yandex-sprint-11-exchange
* yandex-sprint-11-exchange-generator
* yandex-sprint-11-front-ui
* yandex-sprint-11-notification
* yandex-sprint-11-transfer
* yandex-sprint-11-account-postgres
* yandex-sprint-11-transfer-postgres
* yandex-sprint-11-cash-postgres
* yandex-sprint-11-exchange-postgres

## Требования
* Docker 28.2.x
* Minikube v1.37.0
* Helm v4.0.1

## Сборка Linux

### Linux:
```bash
gradle build
minikube start --cpus=4 --memory=8192
minikube addons enable ingress
kubectl create namespace dev
kubectl create configmap -n dev keycloak-import --from-file=./keycloak/import
helm dependency build ./bank-umbrella-chart
./build.sh
helm install bank-dev ./bank-umbrella-chart -f ./bank-umbrella-chart/dev-values.yaml -n dev
sudo sh -c "echo \"$(minikube ip) gateway.local keycloak.local\" >> /etc/hosts"
```

##### Для обращения в приложение следует использовать url http://gateway.local
##### Для обращения в keycloak следует использовать url http://keycloak.local


### В Keycloak создаются предварительно следующие клиенты:
#### 1. Клиент для межсервисного общения (технический токен)
* Client ID = service-client-id
* Client authentication = ON
* Standard flow = ON
* Service accounts roles = ON
* Создать Client Scope с именем `internal_call`, указав ему в настройках `Include in token scope=ON` и добавить его этому клиенту
* Установить секретный ключ в переменную окружения `YANDEX_SPRINT_11_KEYCLOAK_INTERNAL_CALL_CLIENT_SECRET`.

#### 2. Клиент для создания и управления пользователями (технический токен)
* Client ID = user-management-client-id
* Client authentication = ON
* Standard flow = ON
* Service accounts roles = ON
* Добавить роли в `Service accounts roles` с названиями `manage-users`, `view-users`, `query-users`
* Установить секретный ключ в переменную окружения `YANDEX_SPRINT_11_KEYCLOAK_USER_MANAGEMENT_CLIENT_SECRET`.

#### 3. Пользовательский клиент
* Client ID = user-client-id
* Client authentication = OFF
* Standard flow = ON
* Valid redirect URIs = http://gateway.local/login/oauth2/code/user-token-ac
* Valid post logout redirect URIs = http://gateway.local/
* Создать Client Scope с именем `user`, указав ему в настройках `Include in token scope=ON` и добавить его этому клиенту

## Тесты
```bash
helm lint ./bank-umbrella-chart -f ./bank-umbrella-chart/dev-values.yaml
helm test bank-dev -n dev --logs
```

## Информация
```bash
kubectl get pods,svc,ingress -n dev
kubectl logs -n dev pod/<название пода> --tail=200
kubectl describe node minikube
```
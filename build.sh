#!/bin/bash

eval $(minikube -p minikube docker-env)

docker build -t yandex-sprint-12-account:1.0.0 ./account
docker build -t yandex-sprint-12-blocker:1.0.0 ./blocker
docker build -t yandex-sprint-12-cash:1.0.0 ./cash
docker build -t yandex-sprint-12-exchange:1.0.0 ./exchange
docker build -t yandex-sprint-12-exchange-generator:1.0.0 ./exchange-generator
docker build -t yandex-sprint-12-front-ui:1.0.0 ./front-ui
docker build -t yandex-sprint-12-gateway:1.0.0 ./gateway
docker build -t yandex-sprint-12-notification:1.0.0 ./notification
docker build -t yandex-sprint-12-transfer:1.0.0 ./transfer

eval $(minikube -p minikube docker-env -u)
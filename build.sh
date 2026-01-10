#!/bin/bash

eval $(minikube -p minikube docker-env)

docker build -t yandex-sprint-11-account:1.0.0 ./account
docker build -t yandex-sprint-11-blocker:1.0.0 ./blocker
docker build -t yandex-sprint-11-cash:1.0.0 ./cash
docker build -t yandex-sprint-11-exchange:1.0.0 ./exchange
docker build -t yandex-sprint-11-exchange-generator:1.0.0 ./exchange-generator
docker build -t yandex-sprint-11-front-ui:1.0.0 ./front-ui
docker build -t yandex-sprint-11-gateway:1.0.0 ./gateway
docker build -t yandex-sprint-11-notification:1.0.0 ./notification
docker build -t yandex-sprint-11-transfer:1.0.0 ./transfer

eval $(minikube -p minikube docker-env -u)
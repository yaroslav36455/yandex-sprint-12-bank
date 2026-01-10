CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    sub        VARCHAR(100) NOT NULL,
    login      VARCHAR(100) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    birth_date DATE         NOT NULL,
    UNIQUE (sub)
);

CREATE TABLE IF NOT EXISTS account
(
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP     NOT NULL,
    user_id    BIGINT        NOT NULL,
    balance    NUMERIC(15,2) NOT NULL,
    currency   VARCHAR(20)   NOT NULL,
    UNIQUE (user_id, currency),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS deferred_notification
(
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP   NOT NULL,
    login      VARCHAR(50) NOT NULL,
    message    TEXT        NOT NULL,
    status     VARCHAR(20) NOT NULL
);

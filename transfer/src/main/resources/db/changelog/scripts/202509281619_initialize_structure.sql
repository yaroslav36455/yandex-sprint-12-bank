CREATE TABLE IF NOT EXISTS deferred_notification
(
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP   NOT NULL,
    login      VARCHAR(50) NOT NULL,
    message    TEXT        NOT NULL,
    status     VARCHAR(20) NOT NULL
);

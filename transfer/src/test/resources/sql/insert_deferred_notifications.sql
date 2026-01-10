INSERT INTO deferred_notification (created_at, login, message, status)
VALUES (CURRENT_TIMESTAMP, 'username', 'Операция выполнена успешно', 'CREATED'),
       (CURRENT_TIMESTAMP, 'username-2', 'Операция запрещена', 'CREATED'),
       (CURRENT_TIMESTAMP, 'username-3', 'Ошибка операции: Недостаточно денег на счету', 'CREATED'),
       (CURRENT_TIMESTAMP, 'username', 'Операция выполнена успешно', 'SENT'),
       (CURRENT_TIMESTAMP, 'username-3', 'Операция выполнена успешно', 'SENT');
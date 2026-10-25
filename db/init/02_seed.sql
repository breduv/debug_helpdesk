INSERT INTO departments(name) VALUES
    ('Техническая поддержка'),
    ('Бухгалтерия');

INSERT INTO categories(name) VALUES
    ('Сеть'),
    ('Оборудование'),
    ('Программное обеспечение');

INSERT INTO users(full_name, email, role, department_id) VALUES
    ('Анна Петрова', 'anna@example.com', 'CUSTOMER', 2),
    ('Сергей Иванов', 'sergey@helpdesk.local', 'SUPPORT', 1),
    ('Ольга Смирнова', 'olga@helpdesk.local', 'SUPPORT', 1),
    ('Олег Администратор', 'admin@helpdesk.local', 'ADMIN', 1),
    ('Пётр Соколов', 'petr@example.com', 'CUSTOMER', 2);

INSERT INTO tickets(title, description, status, priority, author_id, assignee_id, category_id) VALUES
    ('Не работает Wi-Fi', 'Ноутбук подключается к сети, но интернет недоступен', 'NEW', 'HIGH', 1, NULL, 1),
    ('Принтер недоступен', 'Сетевой принтер не отвечает на задания печати', 'IN_PROGRESS', 'MEDIUM', 5, 2, 2),
    ('CRM закрывается при запуске', 'После обновления CRM завершается сразу после авторизации', 'RESOLVED', 'CRITICAL', 1, 3, 3);

INSERT INTO ticket_comments(ticket_id, author_id, text) VALUES
    (1, 1, 'Проблема появилась после обновления Windows.'),
    (2, 2, 'Проверьте питание принтера и перезапустите устройство.'),
    (3, 3, 'Переустановлен повреждённый модуль клиента CRM.');

INSERT INTO ticket_status_history(ticket_id, old_status, new_status, changed_by) VALUES
    (1, NULL, 'NEW', 1),
    (2, NULL, 'NEW', 5),
    (2, 'NEW', 'IN_PROGRESS', 2),
    (3, NULL, 'NEW', 1),
    (3, 'NEW', 'IN_PROGRESS', 3),
    (3, 'IN_PROGRESS', 'RESOLVED', 3);

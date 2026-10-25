# Help Desk Debug Lab — Java 17 + PostgreSQL

Учебный проект для практической работы по ООП, JDBC и PostgreSQL.

Проект **собирается и запускается**, но в нём намеренно оставлены функциональные и архитектурные дефекты. Задача студента — развернуть систему, воспроизвести проблемы, локализовать причины, исправить код/SQL и подтвердить результат диагностикой.

## Что используется

- Java 17
- Maven 3.9+
- PostgreSQL 18
- pgJDBC 42.7.14
- Docker Compose — рекомендованный способ развернуть БД
- чистый JDBC, без Spring/Hibernate

## Быстрый запуск

### 1. Запустите PostgreSQL

```bash
docker compose up -d db
```

Проверьте:

```bash
docker compose ps
```

Контейнер `helpdesk-postgres` должен перейти в состояние `healthy`.

### 2. Соберите проект

```bash
mvn clean compile
```

### 3. Запустите Help Desk

```bash
mvn exec:java
```

Можно также открыть проект в IntelliJ IDEA и запустить `edu.helpdesk.Main`.

### 4. Запустите диагностику

```bash
mvn exec:java -Dexec.mainClass=edu.helpdesk.Diagnostics
```

В исходной версии часть проверок должна показать `FAIL`. Это ожидаемо.

## Как сбросить БД

Linux/macOS:

```bash
./scripts/reset-db.sh
```

Windows PowerShell:

```powershell
./scripts/reset-db.ps1
```

Или вручную:

```bash
docker compose down -v
docker compose up -d db
```

## Конфигурация БД

По умолчанию Java использует:

```text
jdbc:postgresql://localhost:5432/helpdesk
user: helpdesk
password: helpdesk
```

При необходимости задайте переменные окружения `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## С чего начинать

1. Не меняйте код до первого запуска.
2. Выполните все пункты из `STUDENT_TASK.md`.
3. Зафиксируйте исходный вывод диагностики.
4. Воспроизводите каждый дефект через меню или диагностику.
5. Исправляйте по одному дефекту и после каждого изменения повторяйте диагностику.
6. После исправления базовой части выполните самостоятельное улучшение проекта.

> Важно: наличие `FAIL` не означает, что проект «не запускается». Это спецификация поведения, которое требуется восстановить.

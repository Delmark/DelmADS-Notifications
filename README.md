# DADS Notifications

MCP-сервер на Spring Boot для отправки уведомлений в Telegram. Поднимается как
[MCP](https://modelcontextprotocol.io/)-сервер (Spring AI MCP Server) и предоставляет
инструменты, которыми MCP-клиент (например, Claude) может отправлять сообщения
пользователям через Telegram-бота.

Проект разрабатывается как часть инфраструктуры пет-проекта **DADs** - Delm Aggregation Data System,
и в будущем послужит как интеграционная точка для отправки уведомлений.

## Стек

- **Java 22**, **Spring Boot 4.0.7** (Gradle, Kotlin DSL)
- **Spring AI MCP Server** (WebMVC)
- **Telebof** — клиент Telegram Bot API
- **PostgreSQL** + **MyBatis**, миграции — **Liquibase**
- **Actuator** + **Micrometer** (Prometheus)
- **Lombok**

## Конфигурация

Все секреты задаются через переменные окружения (файл `.env`, в репозиторий **не**
коммитится — он в `.gitignore`).

| Переменная     | Назначение                |
|----------------|---------------------------|
| `TG_BOT_TOKEN` | токен Telegram-бота       |
| `DB_ADDRESS`   | `host:port` PostgreSQL    |
| `DB_NAME`      | имя базы данных           |
| `DB_USER`      | пользователь БД           |
| `DB_PASSWORD`  | пароль БД                 |

Пример `.env`:

```dotenv
TG_BOT_TOKEN=123456:ABC-DEF...
DB_ADDRESS=localhost:5432
DB_NAME=dads_notifications
DB_USER=postgres
DB_PASSWORD=postgres
```

## Запуск

```bash
./gradlew bootRun     # запустить локально
./gradlew build       # собрать и прогнать тесты
```

По умолчанию в данный момент сервер работает в режиме **MCP stdio** (`spring.ai.mcp.server.stdio=true`):
его запускает MCP-клиент, общение идёт по stdin/stdout.

## Разработка

Рабочий процесс (ветки, коммиты, релизы) описан в [CONTRIBUTING.md](CONTRIBUTING.md).

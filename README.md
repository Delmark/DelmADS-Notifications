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

Все секреты задаются через переменные окружения (`.env` файл)
Необходимо задать токен телеграм бота, указать учётные данные
БД и ввести произвольный Bearer Token который будет использоваться
для авторизации ии-агента при использовании MCP инструментов 
и API эндпойнтов.

| Переменная     | Назначение             |
|----------------|------------------------|
| `TG_BOT_TOKEN` | токен Telegram-бота    |
| `DB_ADDRESS`   | `host:port` PostgreSQL |
| `DB_NAME`      | имя базы данных        |
| `DB_USER`      | пользователь БД        |
| `DB_PASSWORD`  | пароль БД              |
| `X_API_KEY`    | произвольный API ключ  |

Пример `.env`:

```dotenv
TG_BOT_TOKEN=123456:ABC-DEF...
DB_ADDRESS=localhost:5432
DB_NAME=dads_notifications
DB_USER=postgres
DB_PASSWORD=postgres
X_API_KEY=48588redlfkkensavy34885911399330ldsa3m435m6=
```

## Запуск

### Локально (Gradle wrapper)

Для запуска приложения необходим JDK 22 и установленный Gradle.

```bash
./gradlew bootJar

./gradlew bootRun

java -jar build/libs/notifications-0.0.2.jar
```

Сервис стартует на `http://localhost:8080`

### Docker

Сборка образа:
```bash
docker build -t dads-notifications:0.0.2 .
```

Запуск образа
```
docker run --rm -p 8080:8080 \
  --env-file .env \
  -v dads_files:/data/files \
  dads-notifications:0.0.2
```

Том `/data/files` - каталог для загружаемых файлов (`file.upload.directory`),
вынесен наружу, чтобы файлы переживали пересоздание контейнера

## Разработка

Рабочий процесс (ветки, коммиты, релизы) описан в [CONTRIBUTING.md](CONTRIBUTING.md).

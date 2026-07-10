# AGENTS.md — Руководство по репозиторию NodelistJ

## Структура проекта

```
src/main/java/ru/oldzoomer/nodelistj/
├── Nodelist.java                  — Основной класс-обёртка для парсинга и хранения
├── entries/
│   ├── NodelistEntry.java         — Запись нодлиста (record)
│   └── BaseEntry.java             — Базовый класс для записей
├── enums/
│   └── Keywords.java              — Перечисление ключевых слов
└── parser/
    ├── NodelistParser.java        — Парсер нодлиста из InputStream
    └── ParserUtils.java           — Утилиты для предварительной обработки

src/test/java/ru/oldzoomer/nodelistj/
├── parser/
│   ├── NodelistParserTest.java    — Тесты парсера
│   └── ParserUtilsTest.java       — Тесты утилит
└── resources/
    └── nodelist.txt               — Тестовые данные

build.gradle                       — Конфигурация сборки (Gradle)
settings.gradle                    — Имя модуля: nodelistj
```

Пакет: `ru.oldzoomer.nodelistj`

## Команды сборки и тестирования

| Команда                          | Описание                                                       |
|----------------------------------|----------------------------------------------------------------|
| `gradle build`                   | Сборка + все тесты                                             |
| `gradle test`                    | Только запуск тестов                                           |
| `gradle check`                   | Проверка (форматирование, статический анализ, если подключено) |
| `gradle publish`                 | Публикация в Maven Local                                       |
| `gradle publishToGitHubPackages` | Публикация в GitHub Packages                                   |

Требования: **Java 25**. Среда сборки использует Gradle Toolchains — локальная JDK не нужна, Gradle скачает сам.

## Стиль кода и правила именования

- **Отступы**: 4 пробела (Java-стандарт).
- **Именование классов**: PascalCase (`NodelistParser`, `NodelistEntry`).
- **Именование методов**: camelCase (`parseNodelist`, `preprocessLine`).
- **Именование полей**: camelCase, `private final` для immutable-полей.
- **Именование тестов**: метод описывает сценарий, например `parseRealNodelist_producesNonEmptyList`.
- **Тесты**: JUnit 5 (`@Test`, `@DisplayName`). Каждый публичный метод должен иметь покрытие тестом.
- **Javadoc**: минимальный для публичных API (классы, публичные методы, конструкторы).
- **Immutable-дизайн**: класс `Nodelist` — immutable, все поля `final`.

## Рекомендации по VCS

### Коммиты

Следуем **Conventional Commits** (наблюдается по истории):

| Префикс     | Назначение                            |
|-------------|---------------------------------------|
| `feat:`     | Новая функциональность                |
| `fix:`      | Исправление бага                      |
| `refactor:` | Рефакторинг без изменения поведения   |
| `test:`     | Добавление или изменение тестов       |
| `chore:`    | Обновление зависимостей, настройка CI |
| `docs:`     | Обновление документации               |
| `bump:`     | Обновление версии / JDK               |

Пример: `feat: add support for network-level entries`

### Pull Requests

- Создавайте feature-ветки от `main`: `feature/описание`, `fix/описание`.
- Описание PR должно содержать: что изменено, почему, связанные issues.
- Все тесты должны проходить (`gradle test`) перед мерджем.
- Dependabot-PRs мерджатся автоматически при успешных проверках.

## Публикация

Проект публикуется как Maven-артефакт:

```
Group: ru.oldzoomer
Artifact: nodelistj
Version: 2.0.0
```

Для публикации в GitHub Packages нужны переменные окружения:

- `USERNAME` — имя пользователя/организации
- `TOKEN` — personal access token с доступом к Packages

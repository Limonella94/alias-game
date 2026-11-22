# Alias — заметки для работы над проектом

Android-игра Alias на Kotlin + Jetpack Compose. Владелец проекта — новичок в Android,
знает Kotlin. Общение и тексты в приложении — на русском.

## Как устроена работа

- Код пишет Claude и пушит в `main`. Владелец делает Pull в Android Studio и запускает на телефоне
  по Wi-Fi-отладке. Claude собрать Android-проект не может (нет Android SDK и сети к Maven/Google),
  поэтому каждое изменение должно быть максимально аккуратным: проверять импорты, сигнатуры, версии API.
- `core/` — чистый Kotlin. Его можно скомпилировать и прогнать тесты без Android, компилятором из Gradle:
  `java -cp "/opt/gradle/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler ...` (+ заглушка JUnit).
- Коммиты — небольшие, с понятным описанием на русском.

## Архитектура

- **MVVM + однонаправленный поток данных:** экран читает `StateFlow` из ViewModel и вызывает её методы.
- **Правила отдельно от UI:** вся логика игры в `core/engine/GameEngine` (неизменяемый `GameState`,
  этапы `Phase`). Режимы игры = разные `RuleSet`. Таймер живёт в `GameViewModel`.
- **Слова:** `WordRepository` (интерфейс). Сейчас `AssetWordRepository` читает `assets/words/*.json`.
  План: Room + загрузка наборов из публичного репозитория `alias-words` (manifest.json с версиями).
- **DI:** ручной `AppContainer` в `AliasApp`; на экранах — `appContainer()` и `viewModel { ... }`.
- **Навигация:** Navigation Compose с типобезопасными маршрутами (`ui/navigation/AliasNavHost.kt`).
- Один модуль `:app`, но пакеты разложены так, чтобы позже вынести `core` и `data` в отдельные модули.

## Формат набора слов

```json
{
  "id": "ru_base", "version": 1, "language": "ru", "title": "Базовый набор", "audience": "adult",
  "words": [
    {"id": "ru_0001", "text": "Кот", "level": 1, "categories": ["animals"], "ageMin": 6, "image": null}
  ]
}
```

- `id` слова уникален и никогда не меняется (на него будет опираться история «уже было»).
- `level`: 1 — лёгкие, 2 — средние, 3 — сложные.
- Новые поля добавлять только с значением по умолчанию в `WordDto` — старые файлы должны читаться.

## Версии

Зафиксированы в `gradle/libs.versions.toml` (AGP 8.7.3, Kotlin 2.1.0, Compose BOM 2024.12.01,
compileSdk 35, minSdk 26). Не обновлять без необходимости: Claude не может проверить сборку.

## Детский режим (план)

- 3–5 лет: картинка (OpenMoji или свои иллюстрации), без таймера и штрафов, кооперативно, большие кнопки.
- 6–8 лет: картинка + подпись, мягкий таймер.
- Реализация: тот же `GameEngine` с другим `RuleSet` + отдельный экран и детская тема.
- Если публиковать в Google Play — учесть требования Families policy (без лишних SDK и сбора данных).

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
- **CI:** `.github/workflows/build-apk.yml` после каждого push в `main` гоняет тесты, собирает APK
  и выкладывает его в релиз `latest` (`releases/download/latest/alias.apk`). После push проверять
  результат: `gh api repos/Limonella94/alias-game/actions/runs?per_page=1`, логи ошибок —
  `gh api repos/Limonella94/alias-game/actions/jobs/<job_id>/logs`. Push считается готовым, только когда сборка зелёная.
- В KDoc/комментариях не писать `/*` внутри текста (например, `words/*.json`) — Kotlin считает это вложенным комментарием.
- **Подпись:** если в секретах репозитория есть `SIGNING_KEYSTORE_BASE64` + `SIGNING_PASSWORD` (alias `alias`),
  CI подписывает ими; иначе — общим `app/debug.keystore`. Ключ из секретов есть только у владельца; при его появлении
  APK из CI и из Android Studio подписаны по-разному (ставить поверх друг друга нельзя).
- **Обновления в приложении:** `data/update/UpdateManager` читает `releases/download/latest/version.json`
  (кладёт CI), сравнивает `versionCode` с `BuildConfig.VERSION_CODE`, качает APK в cache и открывает установщик.
  Работает только при публичном репозитории. `versionCode` = число коммитов (`git rev-list --count HEAD`),
  поэтому в CI нужен `fetch-depth: 0`.

## Архитектура

- **MVVM + однонаправленный поток данных:** экран читает `StateFlow` из ViewModel и вызывает её методы.
- **Правила отдельно от UI:** вся логика игры в `core/engine/GameEngine` (неизменяемый `GameState`,
  этапы `Phase`). Режимы игры = разные `RuleSet`. Таймер живёт в `GameViewModel`.
- **Слова:** `WordRepository` (интерфейс). Сейчас `AssetWordRepository` читает `assets/words/*.json`.
  План: Room + загрузка наборов из публичного репозитория `alias-words` (manifest.json с версиями).
- **DI:** ручной `AppContainer` в `AliasApp`; на экранах — `appContainer()` и `viewModel { ... }`.
- **Навигация:** Navigation Compose с типобезопасными маршрутами (`ui/navigation/AliasNavHost.kt`).
- **Хранение:** DataStore Preferences (`data/storage/JsonPreference` — значение как JSON). Ключи:
  `setup` (последние настройки, `SetupSnapshot`), `saved_game` (незаконченная игра, `SavedGame` = весь `GameState`),
  `word_history` (id слова → когда выпадало). Модели `core` помечены `@Serializable` — новые поля только с дефолтами,
  иначе старое сохранение не прочитается (тогда оно молча игнорируется).
- **Игроки:** `Team.players` (может быть пустым). Очередь объясняющих — `GameState.explainerIndexByTeam`,
  сыгранные раунды — `GameState.history` (`RoundRecord`, база для статистики).
- **Колода:** `WordDeck(words, lastShown = история)` — сначала невыпадавшие слова, потом самые давние.
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

## Темы (наборы слов)

- Каждая тема — отдельный JSON в `assets/words` (`ru_base`, `ru_harry_potter`, `ru_witcher`).
  Метаданные темы: `title`, `description`, `emoji`, `color` ("#RRGGBB"), `cover` (путь в assets), `order`.
- Новая тема = новый JSON-файл, код менять не нужно. В колоде слова с одинаковым текстом из разных тем не дублируются.

## Этап «визуал» — где что менять

Весь внешний вид собран в нескольких местах, чтобы редизайн не трогал логику:
- `ui/theme/Color.kt` — палитра (светлая/тёмная), `GameColors` — цвета «угадано/пропуск».
- `ui/theme/Type.kt` — шрифты (сюда подключить свой шрифт из `res/font`).
- `ui/components/` — общие элементы: `BigButton`, `WordText`, `PackCover`.
- Обложки тем: положить картинки в `app/src/main/assets/covers/<id>.webp` и прописать `"cover": "covers/<id>.webp"`
  в JSON темы — `PackCover` сам покажет картинку вместо эмодзи. Картинки только свои/с лицензией
  (никакого официального арта «Гарри Поттера», «Ведьмака» и т.п.).
- Звуки: `res/raw/sfx_guessed.wav`, `sfx_skipped.wav`, `sfx_tick.wav`, `sfx_time_up.wav` — заменить файлами с теми же именами.
  Текущие сгенерированы синтезом (скрипт можно повторить: синус + огибающая, 22050 Гц, моно).
- Иконка приложения: `res/drawable/ic_launcher_foreground.xml`, фон — `res/values/colors.xml`.

## Версии

Зафиксированы в `gradle/libs.versions.toml` (AGP 8.7.3, Kotlin 2.1.0, Compose BOM 2024.12.01,
compileSdk 35, minSdk 26). Не обновлять без необходимости: Claude не может проверить сборку.

## Детский режим (план)

- 3–5 лет: картинка (OpenMoji или свои иллюстрации), без таймера и штрафов, кооперативно, большие кнопки.
- 6–8 лет: картинка + подпись, мягкий таймер.
- Реализация: тот же `GameEngine` с другим `RuleSet` + отдельный экран и детская тема.
- Если публиковать в Google Play — учесть требования Families policy (без лишних SDK и сбора данных).

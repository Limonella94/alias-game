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
  Релиз `latest` НЕ пересоздавать (удаление тега делает новый релиз черновиком) — только `gh release upload --clobber`.
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

## Визуал (стиль «мрачное фэнтези»)

Приложение всегда тёмное. Всё оформление собрано в нескольких местах:
- `ui/theme/Color.kt` — палитра: графит (`Graphite*`), лунное серебро (`MoonSilver`), туман (`FogGray`),
  янтарь фонаря (`Amber*`), «стекло» (`Glass`, `GlassStrong`, `GlassBorder`), `GameColors`.
- `ui/theme/Type.kt` — `TitleFont` (заголовки, сейчас системный Serif) и `BodyFont` (сейчас SansSerif).
  План: Playfair Display + Manrope из `res/font` — поменять только эти две переменные.
- `ui/components/Atmosphere.kt` — `ScreenBackground` (картинка + затемнение) и `GlassCard` (матовое стекло,
  `highlighted` = янтарная рамка). `BigButton` — янтарная кнопка со свечением, `GlassButton` — второстепенная.
- Картинки: `res/drawable-nodpi/` — `bg_home`, `bg_game` (настройки, темы, игра), `bg_kids` (детский режим),
  `card_play`, `card_kids` (карточки главного экрана), WebP. Иконка — `ic_launcher_foreground.webp`
  (432 px, символ в центре) + фон `ic_launcher_background` в `values/colors.xml`.
- Обложки тем — `assets/covers/<id>.webp` (512 px), путь в поле `cover` JSON темы. Без узнаваемых персонажей.
- Звуки — `res/raw/sfx_*.wav`, заменить файлами с теми же именами.
- Детские картинки к словам: промпты в `kids_prompts.csv` (у владельца), файлы `ru_kids_NNNN` → поле `image`.

## Версии

Зафиксированы в `gradle/libs.versions.toml` (AGP 8.7.3, Kotlin 2.1.0, Compose BOM 2024.12.01,
compileSdk 35, minSdk 26). Не обновлять без необходимости: Claude не может проверить сборку.

## Детский режим

- Вход: `feature/kids/KidsScreen` — выбор «Малыши 3–5» / «Детки 5–6» (все вместе или две команды).
- Настройки готовые: `core/model/KidsPresets` (дети ничего не настраивают). `GameConfig.mode = KIDS`.
- Слова: тема `ru_kids` (`audience: "kids"` — в обычной игре не показывается). У слова поле `emoji` — картинка,
  уровень 1 — малыши, 2 — детки. Тест `WordPacksTest` требует эмодзи у каждого детского слова.
- Малыши: отдельный экран `KidsLittleScreen` (без таймера, огромные кнопки, итог «Мы угадали N»).
- Детки: общий `GameScreen`, карточка рисуется через `ui/components/WordPicture` (эмодзи + подпись).
- Визуал: позже заменить эмодзи на свои картинки — поле `image` у слова + показ в `WordPicture`.
- Если публиковать в Google Play — учесть требования Families policy (без лишних SDK и сбора данных).

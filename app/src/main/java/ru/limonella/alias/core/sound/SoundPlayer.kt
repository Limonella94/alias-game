package ru.limonella.alias.core.sound

/** Игровые звуки. Файлы лежат в res/raw (sfx_*.wav) — на этапе дизайна их можно просто заменить. */
enum class GameSound {
    GUESSED,
    SKIPPED,
    TICK,
    TIME_UP,
}

interface SoundPlayer {
    fun play(sound: GameSound)
}

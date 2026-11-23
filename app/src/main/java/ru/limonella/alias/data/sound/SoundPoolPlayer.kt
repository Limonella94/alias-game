package ru.limonella.alias.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ru.limonella.alias.R
import ru.limonella.alias.core.sound.GameSound
import ru.limonella.alias.core.sound.SoundPlayer

/** Короткие звуки через SoundPool: загружаются один раз, играются без задержки. */
class SoundPoolPlayer(context: Context) : SoundPlayer {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds: Map<GameSound, Int> = mapOf(
        GameSound.GUESSED to pool.load(context, R.raw.sfx_guessed, 1),
        GameSound.SKIPPED to pool.load(context, R.raw.sfx_skipped, 1),
        GameSound.TICK to pool.load(context, R.raw.sfx_tick, 1),
        GameSound.TIME_UP to pool.load(context, R.raw.sfx_time_up, 1),
    )

    override fun play(sound: GameSound) {
        val id = soundIds[sound] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }
}

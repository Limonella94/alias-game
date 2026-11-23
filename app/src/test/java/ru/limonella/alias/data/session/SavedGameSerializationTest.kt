package ru.limonella.alias.data.session

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.limonella.alias.core.engine.GameEngine
import ru.limonella.alias.core.engine.WordDeck
import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter
import ru.limonella.alias.data.settings.SetupSnapshot
import ru.limonella.alias.data.settings.TeamSetup
import kotlin.random.Random

/** Сохранённая игра и настройки должны читаться обратно без потерь. */
class SavedGameSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun gameInProgressSurvivesRoundTrip() {
        val words = (1..10).map { Word(id = "w$it", text = "слово$it", level = 1, packId = "p") }
        val config = GameConfig(
            teams = listOf(Team(0, "Котики", listOf("Маша", "Петя")), Team(1, "Ёжики")),
            rules = RuleSet.Classic.copy(lastWord = LastWordRule.OWN_TEAM),
            wordFilter = WordFilter(levels = setOf(1, 2), packIds = setOf("p")),
        )
        val engine = GameEngine(config, WordDeck(words, Random(1)))
        engine.startRound(); engine.markGuessed(); engine.markSkipped(); engine.timeUp()
        engine.resolveLastWord(0); engine.confirmRound()
        engine.startRound(); engine.markGuessed()

        val saved = SavedGame(engine.state, secondsLeft = 42, savedAt = 123L)
        val text = json.encodeToString(SavedGame.serializer(), saved)
        val decoded = json.decodeFromString(SavedGame.serializer(), text)

        assertEquals(saved, decoded)
    }

    @Test
    fun setupSnapshotSurvivesRoundTrip() {
        val snapshot = SetupSnapshot(
            teams = listOf(TeamSetup("Котики", listOf("Маша")), TeamSetup("Ёжики")),
            roundSeconds = 90,
            pointsToWin = 30,
            skipPenalty = false,
            lastWord = LastWordRule.NONE,
            levels = setOf(1, 3),
            selectedPackIds = setOf("ru_base", "ru_witcher"),
            soundEnabled = false,
        )
        val text = json.encodeToString(SetupSnapshot.serializer(), snapshot)
        assertEquals(snapshot, json.decodeFromString(SetupSnapshot.serializer(), text))
    }
}

package ru.limonella.alias.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.limonella.alias.core.model.GameConfig
import ru.limonella.alias.core.model.LastWordRule
import ru.limonella.alias.core.model.RuleSet
import ru.limonella.alias.core.model.Team
import ru.limonella.alias.core.model.Word
import ru.limonella.alias.core.model.WordFilter
import kotlin.random.Random

class GameEngineTest {

    private val words = (1..10).map { Word(id = "w$it", text = "слово$it", level = 1) }
    private val teamA = Team(0, "A")
    private val teamB = Team(1, "B")

    private fun engine(rules: RuleSet = RuleSet.Classic) = GameEngine(
        GameConfig(listOf(teamA, teamB), rules, WordFilter()),
        WordDeck(words, Random(42)),
    )

    @Test
    fun guessedAndSkippedAreScored() {
        val e = engine()
        e.startRound()
        e.markGuessed()
        e.markGuessed()
        e.markSkipped()
        e.timeUp()
        e.resolveLastWord(null)
        e.confirmRound()

        assertEquals(1, e.state.scores[teamA.id])
        assertEquals(1, e.state.currentTeamIndex)
        assertEquals(Phase.READY, e.state.phase)
    }

    @Test
    fun lastWordCanGoToAnotherTeam() {
        val e = engine()
        e.startRound()
        e.timeUp()
        assertEquals(Phase.LAST_WORD, e.state.phase)
        e.resolveLastWord(teamB.id)
        e.confirmRound()

        assertEquals(0, e.state.scores[teamA.id])
        assertEquals(1, e.state.scores[teamB.id])
    }

    @Test
    fun reviewToggleChangesScore() {
        val e = engine()
        e.startRound()
        e.markGuessed()
        e.timeUp()
        e.resolveLastWord(null)
        e.toggleResult(0)
        assertEquals(-1, e.state.roundPoints)
        e.confirmRound()
        assertEquals(-1, e.state.scores[teamA.id])
    }

    @Test
    fun noLastWordModeGoesStraightToReview() {
        val e = engine(RuleSet.Classic.copy(lastWord = LastWordRule.NONE))
        e.startRound()
        e.timeUp()
        assertEquals(Phase.REVIEW, e.state.phase)
    }

    @Test
    fun winnerOnlyAfterFullCycle() {
        val e = engine(RuleSet.Classic.copy(pointsToWin = 2, lastWord = LastWordRule.NONE))
        // Команда A набирает 2 — но B ещё не ходила.
        e.startRound(); e.markGuessed(); e.markGuessed(); e.timeUp(); e.confirmRound()
        assertEquals(Phase.READY, e.state.phase)
        // Команда B ничего не набрала — круг завершён, A победила.
        e.startRound(); e.timeUp(); e.confirmRound()
        assertEquals(Phase.FINISHED, e.state.phase)
        assertEquals(listOf(teamA), e.state.winners)
    }

    @Test
    fun tieContinuesGame() {
        val e = engine(RuleSet.Classic.copy(pointsToWin = 1, lastWord = LastWordRule.NONE))
        e.startRound(); e.markGuessed(); e.timeUp(); e.confirmRound()
        e.startRound(); e.markGuessed(); e.timeUp(); e.confirmRound()
        assertEquals(Phase.READY, e.state.phase)
        assertEquals(2, e.state.roundNumber)
        assertTrue(e.state.winners.isEmpty())
    }

    @Test
    fun actionsInWrongPhaseAreIgnored() {
        val e = engine()
        e.markGuessed() // ещё не начали
        e.confirmRound()
        assertEquals(Phase.READY, e.state.phase)
        assertTrue(e.state.roundResults.isEmpty())
    }

    @Test
    fun ownTeamLastWordRejectsOtherTeams() {
        val e = engine(RuleSet.Classic.copy(lastWord = LastWordRule.OWN_TEAM))
        e.startRound()
        e.timeUp()
        assertEquals(Phase.LAST_WORD, e.state.phase)
        e.resolveLastWord(teamB.id) // чужая команда — не засчитывается
        assertEquals(Phase.LAST_WORD, e.state.phase)
        e.resolveLastWord(teamA.id)
        e.confirmRound()
        assertEquals(1, e.state.scores[teamA.id])
    }

    @Test
    fun duplicateTextsAreNotRepeatedInDeck() {
        val deck = WordDeck(
            listOf(Word("a", "Сова", 1), Word("b", "сова", 1), Word("c", "Кот", 1)),
            Random(1),
        )
        val drawn = List(2) { deck.next().text.lowercase() }.toSet()
        assertEquals(setOf("сова", "кот"), drawn)
    }

    @Test
    fun explainersRotateWithinTeam() {
        val a = Team(0, "A", players = listOf("Маша", "Петя"))
        val b = Team(1, "B", players = listOf("Оля"))
        val e = GameEngine(
            GameConfig(listOf(a, b), RuleSet.Classic.copy(lastWord = LastWordRule.NONE, pointsToWin = null)),
            WordDeck(words, Random(3)),
        )
        val explainers = mutableListOf<String?>()
        repeat(4) {
            explainers += e.state.currentExplainer
            e.startRound(); e.timeUp(); e.confirmRound()
        }
        assertEquals(listOf("Маша", "Оля", "Петя", "Оля"), explainers)
        assertEquals("Маша", e.state.currentExplainer)
    }

    @Test
    fun teamWithoutPlayersHasNoExplainer() {
        val e = engine()
        assertEquals(null, e.state.currentExplainer)
    }

    @Test
    fun roundHistoryIsRecorded() {
        val e = engine(RuleSet.Classic.copy(lastWord = LastWordRule.NONE))
        e.startRound(); e.markGuessed(); e.markGuessed(); e.markSkipped(); e.timeUp(); e.confirmRound()
        val record = e.state.history.single()
        assertEquals(teamA.id, record.teamId)
        assertEquals(2, record.guessed)
        assertEquals(1, record.skipped)
        assertEquals(1, record.points)
    }

    @Test
    fun restoredEngineContinuesFromState() {
        val e = engine()
        e.startRound(); e.markGuessed()
        val saved = e.state
        val restored = GameEngine.restore(saved, WordDeck(words, Random(9)))
        assertEquals(saved, restored.state)
        restored.timeUp(); restored.resolveLastWord(null); restored.confirmRound()
        assertEquals(1, restored.state.scores[teamA.id])
    }

    @Test
    fun deckGivesUnseenWordsFirst() {
        val seenAll = words.drop(2).associate { it.id to 1000L }
        val deck = WordDeck(words, Random(5), lastShown = seenAll)
        val firstTwo = setOf(deck.next().id, deck.next().id)
        assertEquals(setOf("w1", "w2"), firstTwo)
        // затем — самые давние из уже показанных
        val older = words.drop(2).associate { it.id to if (it.id == "w7") 1L else 1000L }
        val deck2 = WordDeck(words, Random(5), lastShown = older)
        deck2.next(); deck2.next()
        assertEquals("w7", deck2.next().id)
    }
}

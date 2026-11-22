package ru.limonella.alias.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.limonella.alias.core.model.GameConfig
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
        val e = engine(RuleSet.Classic.copy(lastWordForAll = false))
        e.startRound()
        e.timeUp()
        assertEquals(Phase.REVIEW, e.state.phase)
    }

    @Test
    fun winnerOnlyAfterFullCycle() {
        val e = engine(RuleSet.Classic.copy(pointsToWin = 2, lastWordForAll = false))
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
        val e = engine(RuleSet.Classic.copy(pointsToWin = 1, lastWordForAll = false))
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
}

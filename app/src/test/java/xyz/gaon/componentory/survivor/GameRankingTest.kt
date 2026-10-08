package xyz.gaon.componentory.survivor

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GameRankingTest {
    private class Fake(
        var owner: String? = "one",
        var fails: Boolean = false,
        var switches: Boolean = false,
    ) : GameRankingClient {
        val scores = mutableListOf<Long>()

        override suspend fun profile() = owner?.let { GameProfile(it, it) }

        override suspend fun submitFor(profileId: String, leaderboard: String, score: Long) {
            if (switches) owner = "two"
            if (owner != profileId) throw GameProfileChanged()
            if (fails) throw java.io.IOException("Offline fixture")
            scores += score
        }
    }

    @Test
    fun failedScoresStayQueuedAndAReattemptCanSucceed() = runBlocking {
        val client = Fake(fails = true)
        var saved = GameSubmission("run", "v1", 500, "one")
        GameRanking.retry(listOf(saved), client, "board", "v1", 1000) { saved = it }
        assertEquals(SubmissionStatus.QUEUED, saved.status)
        assertEquals(1, saved.attempts)
        assertEquals(1000L, saved.lastAttemptEpochMillis)
        client.fails = false
        GameRanking.retry(listOf(saved), client, "board", "v1", 1001) { saved = it }
        assertTrue(client.scores.isEmpty())
        GameRanking.retry(listOf(saved), client, "board", "v1", 3000) { saved = it }
        assertEquals(listOf(500L), client.scores)
        assertEquals(SubmissionStatus.SENT, saved.status)
        GameRanking.retry(listOf(saved), client, "board", "v1", 10000) { saved = it }
        assertEquals(1, client.scores.size)
    }

    @Test
    fun otherProfilesUnboundScoresAndOldRulesAreNeverAutoSubmitted() = runBlocking {
        val client = Fake()
        val wrong = GameSubmission("wrong", "v1", 1, "two")
        val unowned = GameSubmission("offline", "v1", 2, null)
        val old = GameSubmission("old", "v0", 3, "one")
        GameRanking.retry(listOf(wrong, unowned, old), client, "board", "v1", 1000) {
            fail("Unexpected update")
        }
        assertTrue(client.scores.isEmpty())
        client.owner = null
        GameRanking.retry(
            listOf(GameSubmission("signedout", "v1", 4, "one")),
            client,
            "board",
            "v1",
            1000,
        ) {
            fail("Unexpected update")
        }
        assertTrue(client.scores.isEmpty())
    }

    @Test
    fun aProfileChangeAtSubmissionTimeKeepsTheScorePending() = runBlocking {
        val client = Fake(switches = true)
        var saved = GameSubmission("run", "v1", 500, "one")
        GameRanking.retry(listOf(saved), client, "board", "v1", 1000) { saved = it }
        assertTrue(client.scores.isEmpty())
        assertEquals(SubmissionStatus.QUEUED, saved.status)
        assertEquals("one", saved.profileId)
    }

    @Test
    fun anUnconfiguredLeaderboardCannotConsumeAPendingScore() = runBlocking {
        val client = Fake()
        GameRanking.retry(listOf(GameSubmission("run", "v1", 500, "one")), client, "", "v1", 1000) {
            fail("Unexpected update")
        }
        assertTrue(client.scores.isEmpty())
    }
}

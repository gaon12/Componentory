package xyz.gaon.componentory.survivor

import kotlinx.coroutines.CancellationException

internal data class GameProfile(val id: String, val name: String)

internal interface GameRankingClient {
    suspend fun profile(): GameProfile?

    suspend fun submitFor(profileId: String, leaderboard: String, score: Long)
}

internal class GameProfileChanged : IllegalStateException("The current game profile changed")

internal object GameRanking {
    fun eligible(
        s: GameSubmission,
        ruleset: String,
        profileId: String?,
        now: Long,
        force: Boolean = false,
    ): Boolean {
        val delay = minOf(3600000L, 1000L shl minOf(s.attempts, 12))
        return s.status == SubmissionStatus.QUEUED &&
            s.ruleset == ruleset &&
            profileId != null &&
            s.profileId == profileId &&
            (force ||
                s.lastAttemptEpochMillis == 0L ||
                now < s.lastAttemptEpochMillis ||
                now - s.lastAttemptEpochMillis >= delay)
    }

    suspend fun retry(
        submissions: List<GameSubmission>,
        client: GameRankingClient,
        leaderboard: String,
        ruleset: String,
        now: Long,
        force: Boolean = false,
        persist: (GameSubmission) -> Unit,
    ): Int {
        var failures = 0
        if (leaderboard.isBlank()) return 0
        for (s in submissions) {
            if (s.status != SubmissionStatus.QUEUED || s.ruleset != ruleset || s.profileId == null)
                continue
            val current = client.profile()
            if (!eligible(s, ruleset, current?.id, now, force)) continue
            val attempt = s.copy(attempts = s.attempts + 1, lastAttemptEpochMillis = now)
            persist(attempt)
            try {
                client.submitFor(requireNotNull(s.profileId), leaderboard, s.score)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failed: Exception) {
                failures++
                continue
            }
            persist(attempt.copy(status = SubmissionStatus.SENT))
        }
        return failures
    }
}

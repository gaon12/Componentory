package xyz.gaon.componentory.survivor

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import xyz.gaon.componentory.R

internal class GamePlayClient(private val activity: Activity) : GameRankingClient {
    val enabled = configured(activity)
    val leaderboard = activity.getString(R.string.game_leaderboard_id)

    override suspend fun profile(): GameProfile? =
        withTimeoutOrNull(5000) {
            if (
                !enabled ||
                    !PlayGames.getGamesSignInClient(activity)
                        .isAuthenticated()
                        .awaitResult()
                        .isAuthenticated
            )
                return@withTimeoutOrNull null
            val player = PlayGames.getPlayersClient(activity).currentPlayer.awaitResult()
            GameProfile(player.playerId, player.displayName)
        }

    suspend fun connect(): GameProfile? {
        if (!enabled) return null
        if (!PlayGames.getGamesSignInClient(activity).signIn().awaitResult().isAuthenticated)
            return null
        return profile()
    }

    override suspend fun submitFor(profileId: String, leaderboard: String, score: Long) {
        // Read the current SDK profile immediately before a submission, never trust a saved UI
        // label.
        if (profile()?.id != profileId) throw GameProfileChanged()
        val submission =
            withContext(Dispatchers.Main.immediate) {
                if (!activity.hasWindowFocus()) throw GameProfileChanged()
                PlayGames.getLeaderboardsClient(activity).submitScoreImmediate(leaderboard, score)
            }
        withTimeout(15000) { submission.awaitResult() }
    }

    suspend fun intent(period: Int): Intent {
        check(profile() != null)
        require(
            period in
                setOf(
                    LeaderboardVariant.TIME_SPAN_DAILY,
                    LeaderboardVariant.TIME_SPAN_WEEKLY,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                )
        )
        return withTimeout(10000) {
            PlayGames.getLeaderboardsClient(activity)
                .getLeaderboardIntent(leaderboard, period, LeaderboardVariant.COLLECTION_PUBLIC)
                .awaitResult()
        }
    }

    companion object {
        fun configured(context: Context) =
            context.getString(R.string.game_services_project_id) != "0" &&
                context.getString(R.string.game_leaderboard_id).isNotBlank() &&
                context.getString(R.string.game_leaderboard_ruleset) == GameCatalog.RULESET
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}

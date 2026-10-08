package xyz.gaon.componentory.survivor

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (GamePlayClient.configured(this)) PlayGamesSdk.initialize(this)
    }
}

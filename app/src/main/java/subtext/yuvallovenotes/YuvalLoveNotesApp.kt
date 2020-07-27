package subtext.yuvallovenotes

import android.app.Application
import com.backendless.Backendless

class YuvalLoveNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
    }

    companion object {
        const val LOG_TAG = "YuvTag"
    }
}
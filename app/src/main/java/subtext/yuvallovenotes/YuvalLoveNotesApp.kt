package subtext.yuvallovenotes

import android.app.Application
import com.backendless.Backendless

class YuvalLoveNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backendless.initApp(this, getString(R.string.ln_backendless_app_id), getString(R.string.ln_backendless_android_api_key))
    }
}
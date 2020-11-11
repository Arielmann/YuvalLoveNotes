package subtext.yuvallovenotes

import android.app.Application
import android.content.Context
import subtext.yuvallovenotes.crossapplication.service_locator.repositoryModule
import subtext.yuvallovenotes.crossapplication.service_locator.viewModelModule
import com.backendless.Backendless
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class YuvalLoveNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        setupKoin()
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
    }

    companion object {
        const val LOG_TAG = "YuvTag"
        lateinit var context: Context
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@YuvalLoveNotesApp)
            androidLogger()
            modules(viewModelModule, repositoryModule)
        }
    }
}
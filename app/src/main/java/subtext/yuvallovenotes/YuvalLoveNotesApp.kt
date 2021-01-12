package subtext.yuvallovenotes

import android.app.Application
import android.content.Context
import android.util.Log
import subtext.yuvallovenotes.crossapplication.service_locator.repositoryModule
import subtext.yuvallovenotes.crossapplication.service_locator.viewModelModule
import com.backendless.Backendless
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import subtext.yuvallovenotes.crossapplication.database.LoveLocalDatabase
import subtext.yuvallovenotes.crossapplication.service_locator.networkModule
import subtext.yuvallovenotes.crossapplication.service_locator.sharedPrefsModule

class YuvalLoveNotesApp : Application() {

    companion object {
        val APP_TAG = YuvalLoveNotesApp::class.simpleName
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        setupKoin()
//        LoveLocalDatabase.getDatabase() //Setting up for the first time
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@YuvalLoveNotesApp)
            androidLogger()
            modules(viewModelModule, sharedPrefsModule, repositoryModule, networkModule)
        }
    }
}
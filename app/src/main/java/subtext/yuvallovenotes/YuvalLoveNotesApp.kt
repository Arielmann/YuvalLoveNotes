package subtext.yuvallovenotes

import android.app.Application
import android.content.Context
import subtext.yuvallovenotes.crossapplication.service_locator.repositoryModule
import subtext.yuvallovenotes.crossapplication.service_locator.viewModelModule
import com.backendless.Backendless
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import subtext.yuvallovenotes.crossapplication.database.LoveLocalDatabase
import subtext.yuvallovenotes.crossapplication.service_locator.sharedPrefsModule

class YuvalLoveNotesApp : Application() {

    companion object {
        val APP_TAG = YuvalLoveNotesApp::class.simpleName
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        LoveLocalDatabase.getDatabase() //Setting up for the first time
        setupKoin()
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
        StartAppSDK.init(this, getString(R.string.id_startapp), false);
        StartAppAd.disableSplash()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@YuvalLoveNotesApp)
            androidLogger()
            modules(viewModelModule, sharedPrefsModule, repositoryModule)
        }
    }
}
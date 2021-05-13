package subtext.yuvallovenotes

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.backendless.Backendless
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.users.Gender
import subtext.yuvallovenotes.crossapplication.service_locator.networkModule
import subtext.yuvallovenotes.crossapplication.service_locator.repositoryModule
import subtext.yuvallovenotes.crossapplication.service_locator.sharedPrefsModule
import subtext.yuvallovenotes.crossapplication.service_locator.viewModelModule


class YuvalLoveNotesApp : Application() {

    companion object {
        val APP_TAG = YuvalLoveNotesApp::class.simpleName
        @SuppressLint("StaticFieldLeak") //It's the app's context. It should move around freely and collected when the app gets terminated
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        setupKoin()
        setupAds()
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
    }

    private fun setupAds() {
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(listOf(getString(R.string.device_test_ads_id))).build())
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@YuvalLoveNotesApp)
            androidLogger()
            modules(viewModelModule, sharedPrefsModule, repositoryModule, networkModule)
        }
    }
}
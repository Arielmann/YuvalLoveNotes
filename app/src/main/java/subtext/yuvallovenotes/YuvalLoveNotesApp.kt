package subtext.yuvallovenotes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.backendless.Backendless
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.sentry.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.crossapplication.service_locator.networkModule
import subtext.yuvallovenotes.crossapplication.service_locator.repositoryModule
import subtext.yuvallovenotes.crossapplication.service_locator.sharedPrefsModule
import subtext.yuvallovenotes.crossapplication.service_locator.viewModelModule
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import java.lang.IllegalArgumentException


class YuvalLoveNotesApp : Application() {

    companion object {
        val APP_TAG = YuvalLoveNotesApp::class.simpleName
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        setupKoin()
        setupAds()
        Backendless.initApp(this, BuildConfig.BACKENDLESS_APP_ID, BuildConfig.BACKENDLESS_ANDROID_API_KEY)
        requestLoveLettersFromServer()
    }

    private fun requestLoveLettersFromServer() {
        val prefs: SharedPreferences = get(SharedPreferences::class.java)
        val isFetchingCompletedBefore = prefs.getBoolean(getString(R.string.pref_key_server_letters_downloaded_after_app_installed), false)

        if (isFetchingCompletedBefore) {
            Log.d(APP_TAG, "Requesting initial database download")
            val loveItemsViewModel = get(LoveItemsViewModel::class.java)
            loveItemsViewModel.requestLoveLettersFromServer()
        } else {
            Log.d(APP_TAG, "Initial database download is not required")
        }

    }

    //Todo: cleanup
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
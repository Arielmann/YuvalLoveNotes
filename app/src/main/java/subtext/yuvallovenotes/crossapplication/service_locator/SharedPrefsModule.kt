package subtext.yuvallovenotes.crossapplication.service_locator
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val sharedPrefsModule = module {
    single { provideProfileSharedPreferences(androidApplication())}
}

fun provideProfileSharedPreferences(app: Application): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

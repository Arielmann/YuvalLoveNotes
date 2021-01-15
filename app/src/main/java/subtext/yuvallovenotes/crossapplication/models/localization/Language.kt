package subtext.yuvallovenotes.crossapplication.models.localization

import android.util.Log.d
import java.util.*

enum class Language(var tableFieldName: String) {

    ENGLISH, HEBREW;

    constructor() : this("") {
        tableFieldName = inferFieldName()
    }


    private fun inferFieldName(): String {
        return name.toLowerCase(Locale.getDefault())
    }
}

private var localeToLanguageMap: Map<String, Language> = hashMapOf(
        "us" to Language.ENGLISH,
        "en" to Language.ENGLISH,
        "iw_IL" to Language.HEBREW,
).withDefault { Language.ENGLISH }

/**
 * Returns a language based on device default locale
 */
private val TAG: String = Language::class.simpleName!!
fun inferLanguageFromLocale(): Language {
    val locale = Locale.getDefault().toString()
    d(TAG, "Device locale: $locale")
    return localeToLanguageMap[Locale.getDefault().toString()] ?: Language.ENGLISH
}

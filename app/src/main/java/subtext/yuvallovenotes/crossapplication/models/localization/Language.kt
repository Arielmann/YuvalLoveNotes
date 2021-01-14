package subtext.yuvallovenotes.crossapplication.models.localization

import java.util.*

enum class Language(var fieldName: String) {

    ENGLISH, HEBREW;

    constructor() : this("") {
        fieldName = inferDisplayName()
    }


    private fun inferDisplayName(): String {
        return name.toLowerCase(Locale.getDefault())
    }
}

private var localeToLanguageMap: Map<String, Language> = hashMapOf(
        "en" to Language.ENGLISH,
        "iw-il" to Language.HEBREW,
).withDefault { Language.ENGLISH }

/**
 * Returns a language based on device default locale
 */
fun inferLanguageFromLocale(): Language {
    return localeToLanguageMap[Locale.getDefault().toString()] ?: Language.ENGLISH
}

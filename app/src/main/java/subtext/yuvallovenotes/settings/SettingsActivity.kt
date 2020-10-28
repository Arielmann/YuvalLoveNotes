package subtext.yuvallovenotes.settings

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import subtext.yuvallovenotes.R

internal class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        val numberOfLoveLettersEditText = findViewById<R.id.>()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val numberOfLovePhrasesEditTextPref: EditTextPreference? = findPreference(getString(R.string.pref_key_number_of_love_phrases)) as EditTextPreference?
            numberOfLovePhrasesEditTextPref?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            val phoneNumberEditTextPref: EditTextPreference? = findPreference(getString(R.string.pref_key_target_phone_number)) as EditTextPreference?
            phoneNumberEditTextPref?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_PHONE
            }
        }
    }
}
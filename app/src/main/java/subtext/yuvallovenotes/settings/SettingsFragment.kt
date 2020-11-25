package subtext.yuvallovenotes.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log.d
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils

internal class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private val TAG = SettingsFragment::class.simpleName
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val phoneNumberPref: Preference? = findPreference(getString(R.string.pref_key_target_phone_number))
        phoneNumberPref?.let { pref ->

            pref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener{
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    TODO("Not yet implemented")
                }
            }

            phoneNumberPref.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    d(TAG, "user changed required phone number")
                   /* if (LoveUtils.isPhoneNumberValid(countryCode, localNumber)) {
                        sharedPrefs.edit().putString(resources.getString(R.string.pref_key_target_region_number), countryCode).apply()
                        sharedPrefs.edit().putString(resources.getString(R.string.pref_key_target_phone_number), countryCode.plus(localNumber)).apply()
                        d(TAG, "Number changed and saved")
                        return true
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
                        return false
                    }
                    TODO("Not yet implemented")*/
                }
            }
        }
    }
}
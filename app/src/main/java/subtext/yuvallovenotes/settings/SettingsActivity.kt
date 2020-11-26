package subtext.yuvallovenotes.settings

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.dialog_settings_target_phone_number.view.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTheme(R.style.BlankScreen)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val phoneNumberPref: Preference? = findPreference(getString(R.string.pref_key_full_target_phone_number))
            phoneNumberPref?.let { pref ->

                //Show dialog
                pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_settings_target_phone_number, null)
                    val mBuilder = AlertDialog.Builder(requireContext()).setView(mDialogView)
                    val mAlertDialog = mBuilder.create()
                    mAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    val currentRegionNumber = sharedPrefs.getString(getString(R.string.pref_key_phone_region_number), LoveUtils.getDeviceDefaultCountryCode())
                    mDialogView.enterPhoneRegionDialogInputEditText.setText(currentRegionNumber)
                    val currentLocalPhoneNumber = sharedPrefs.getString(getString(R.string.pref_key_local_phone_number), "")
                    mDialogView.enterPhoneDialogLocalNumberInputEditText.setText(currentLocalPhoneNumber)
                    mAlertDialog.show()

                    //Set click listener to check and save only a valid number
                    mDialogView.dialogConfirmBtn.setOnClickListener {
                        val newRegionNumber = mDialogView.enterPhoneRegionDialogInputEditText.text.toString()
                        val newLocalPhoneNumber = mDialogView.enterPhoneDialogLocalNumberInputEditText.text.toString()
                        if (LoveUtils.isPhoneNumberValid(newRegionNumber, newLocalPhoneNumber)) {
                            mAlertDialog.dismiss()
                            val newFullNumber = newRegionNumber.plus(newLocalPhoneNumber)
                            sharedPrefs.edit().putString(resources.getString(R.string.pref_key_phone_region_number), newRegionNumber).apply()
                            sharedPrefs.edit().putString(resources.getString(R.string.pref_key_local_phone_number), newLocalPhoneNumber).apply()
                            sharedPrefs.edit().putString(resources.getString(R.string.pref_key_full_target_phone_number), newFullNumber).apply()
                        } else {
                            Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
                        }
                    }

                    mDialogView.dialogCancelBtn.setOnClickListener {
                        mAlertDialog.dismiss()
                    }
                    false
                }
            }
        }
    }
}
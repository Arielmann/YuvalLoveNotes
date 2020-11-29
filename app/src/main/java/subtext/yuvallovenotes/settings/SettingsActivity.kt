package subtext.yuvallovenotes.settings

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.dialog_settings_target_phone_number.view.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.BlankScreen)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settingsContainer, SettingsFragment())
                    .commit()
        }

        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.settingsToolBar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_24);
        binding.settingsToolBar.setNavigationOnClickListener { backBtn ->
            finish() //Todo: look for a way to implement navigation?
        }
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
package subtext.yuvallovenotes.login.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.UnVerifiedLoveLettersUser
import subtext.yuvallovenotes.crossapplication.utils.*
import subtext.yuvallovenotes.databinding.FragmentEnterLoverPhoneNumberBinding
import subtext.yuvallovenotes.login.network.UserRegistrationCallback
import subtext.yuvallovenotes.login.viewmodel.LoginViewModel


class EnterLoverPhoneNumberFragment : Fragment() {

    companion object {
        private val TAG: String = EnterLoverPhoneNumberFragment::class.simpleName!!
        const val PICK_CONTACT = 1
        const val READ_CONTACTS_PERMISSION_REQUEST_CODE = 2099
    }

    private lateinit var binding: FragmentEnterLoverPhoneNumberBinding
    private lateinit var sharedPrefs: SharedPreferences
    private val loginViewModel = get<LoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnterLoverPhoneNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setOnDoneButtonClickListener()
        setPhoneNumberEditTexts()
        setPickNumberFromUserContactsFeature()
        binding.loversLocalPhoneNumberInputEditText.requestFocus()
        LoveUtils.setupFragmentDefaultToolbar(this, binding.enterLoverPhoneNumberToolBar)
    }

    private fun setPickNumberFromUserContactsFeature() {

        binding.loversChooseFromContactsBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_REQUEST_CODE)
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_CONTACT -> {

                if (data == null) {
                    e(TAG, "no data found")
                    return
                }

                if (resultCode == Activity.RESULT_OK) {
                    PhoneNumberUtil.getInstance().getPhoneNumberFromUserContactChoice(requireContext(), data) { countryCode, nationalNumber ->
                        if (countryCode != null && countryCode.isNotBlank()) {
                            binding.loversPhoneNumberRegionInputEditText.setText(countryCode)
                        }else{
                            binding.loversPhoneNumberRegionInputEditText.setText(PhoneNumberUtil.getInstance().getDeviceDefaultCountryCode())
                        }
                        binding.loversLocalPhoneNumberInputEditText.setText(nationalNumber)
                    }
                }
            }
        }
    }

    /*todo: 1. not being called from activity.
            2. handle never ask again chosen by user*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_CONTACTS_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(intent, PICK_CONTACT)
                }

                return
            }
            else -> {
            } // Ignore all other requests.
        }
    }

    private fun setPhoneNumberEditTexts() {
        loginViewModel.requestUserPhoneNumber { regionNumber, localNumber ->
            binding.loversPhoneNumberRegionInputEditText.setText(regionNumber)
            binding.loversLocalPhoneNumberInputEditText.setText(localNumber)
        }
    }

    private fun setOnDoneButtonClickListener() {

        val callback = object : UserRegistrationCallback {
            override fun onSuccess() {
                d(TAG, "User registered successfully")
                findNavController().popBackStack(R.id.enterUserNameFragment, false)
                findNavController().navigate(EnterUserNameFragmentDirections.navigateToLetterGenerator())
            }

            override fun onError(error: String) {
                binding.enterLoverPhoneNumberProgressBar.hide()
                binding.root.animate().alpha(1f).setDuration(100).start()
                Toast.makeText(YuvalLoveNotesApp.context, error, Toast.LENGTH_LONG).show()
            }

        }

        binding.loverNumberDoneBtn.setOnClickListener {

            binding.root.animate().alpha(0.5f).setDuration(200).start()
            binding.enterLoverPhoneNumberProgressBar.show()
            val userName = loginViewModel.getUserName()
            val loverNickName = loginViewModel.getLoverNickName()
            val regionNumber = binding.loversPhoneNumberRegionInputEditText.text.toString()
            val localNumber = binding.loversLocalPhoneNumberInputEditText.text.toString()
            val phone = LoveLettersUser.Phone(regionNumber, localNumber)
            val user = UnVerifiedLoveLettersUser(userName, loverNickName, phone)
            loginViewModel.requestLogin(user, callback)


           /* val regionNumber = binding.loversPhoneNumberRegionInputEditText.text.toString()
            val localNumber = binding.loversLocalPhoneNumberInputEditText.text.toString()
            if (PhoneNumberUtil.getInstance().isPhoneNumberValid(regionNumber, localNumber)) {
                sharedPrefs.edit().putString(getString(R.string.pref_key_phone_region_number), regionNumber).apply()
                sharedPrefs.edit().putString(getString(R.string.pref_key_local_phone_number), localNumber).apply()
                sharedPrefs.edit().putString(getString(R.string.pref_key_full_target_phone_number), regionNumber.plus(localNumber)).apply()
                sharedPrefs.edit().putBoolean(getString(R.string.pref_key_is_login_process_completed), true).apply()
                findNavController().popBackStack(R.id.enterUserNameFragment, false)
                findNavController().navigate(EnterUserNameFragmentDirections.navigateToLetterGenerator())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
            }*/
        }
    }

}



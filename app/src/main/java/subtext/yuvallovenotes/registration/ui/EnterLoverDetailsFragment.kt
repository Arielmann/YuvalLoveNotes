package subtext.yuvallovenotes.registration.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
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
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.crossapplication.utils.getDeviceDefaultCountryCode
import subtext.yuvallovenotes.crossapplication.utils.getPhoneNumberFromUserContactChoice
import subtext.yuvallovenotes.databinding.FragmentEnterLoverDetailsBinding
import subtext.yuvallovenotes.registration.network.UserRegistrationCallback
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel

class EnterLoverDetailsFragment : Fragment() {

    companion object {
        private val TAG = EnterLoverDetailsFragment::class.simpleName
    }

    private lateinit var binding : FragmentEnterLoverDetailsBinding
    private lateinit var sharedPrefs : SharedPreferences
    private val registrationViewModel = get<RegistrationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnterLoverDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.loverNameInputEditText.requestFocus()
        setOnDoneButtonClickListener()
        setPhoneNumberEditTexts()
        setPickNumberFromUserContactsFeature()
        LoveUtils.setupFragmentDefaultToolbar(this, binding.loverNameToolBar)
        setupLoverNameEditText()
    }

    private fun setOnDoneButtonClickListener() {

        val callback = object : UserRegistrationCallback {
            override fun onSuccess() {
                Log.d(TAG, "User registered successfully")
                findNavController().popBackStack(R.id.enterUserDetailsFragment, false)
                findNavController().navigate(EnterUserDetailsFragmentDirections.navigateToLetterGenerator())
            }

            override fun onError(error: String) {
                binding.loverPhoneNumberProgressBar.hide()
                binding.root.animate().alpha(1f).setDuration(100).start()
                Toast.makeText(YuvalLoveNotesApp.context, error, Toast.LENGTH_LONG).show()
            }

        }

 /*       binding.loverPhoneNumberDoneBtn.setOnClickListener {
            if (binding.loverNameInputEditText.text.isNotBlank()) {
                sharedPrefs.edit().putString(resources.getString(R.string.pref_key_lover_name), binding.loverNameInputEditText.text.toString()).apply()
                findNavController().navigate(EnterUserDetailsFragmentDirections.navigateToLoverPhoneNumber())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_no_lover_name_inserted), Toast.LENGTH_LONG).show()
            }
        }*/

        binding.loverPhoneNumberDoneBtn.setOnClickListener {

            binding.root.animate().alpha(0.5f).setDuration(200).start()
            binding.loverPhoneNumberProgressBar.show()
            val loverNickName = binding.loverNameInputEditText.text.toString()
            val loverRegionNumber = binding.loversPhoneNumberRegionInputEditText.text.toString()
            val loverLocalNumber = binding.loversLocalPhoneNumberInputEditText.text.toString()
            val loverPhone = LoveLettersUser.Phone(loverRegionNumber, loverLocalNumber)
            val user = registrationViewModel.getUserFromSharedPrefsData()
            user.loverPhone = loverPhone
            user.loverNickName = loverNickName
            registrationViewModel.requestRegistration(user, callback)

        }
    }

    private fun setupLoverNameEditText() {
        binding.loverNameInputEditText.requestFocus()
        val loverName = sharedPrefs.getString(resources.getString(R.string.pref_key_lover_nickname), "")
        binding.loverNameInputEditText.setText(loverName)
    }

    private fun setPickNumberFromUserContactsFeature() {

        binding.loversChooseFromContactsBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), EnterLoverPhoneNumberFragment.READ_CONTACTS_PERMISSION_REQUEST_CODE)
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, EnterLoverPhoneNumberFragment.PICK_CONTACT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            EnterLoverPhoneNumberFragment.PICK_CONTACT -> {

                if (data == null) {
                    Log.e(TAG, "no data found")
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
            EnterLoverPhoneNumberFragment.READ_CONTACTS_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(intent, EnterLoverPhoneNumberFragment.PICK_CONTACT)
                }

                return
            }
            else -> {
            } // Ignore all other requests.
        }
    }

    private fun setPhoneNumberEditTexts() {
        registrationViewModel.requestUserPhoneNumber { regionNumber, localNumber ->
            binding.loversPhoneNumberRegionInputEditText.setText(regionNumber)
            binding.loversLocalPhoneNumberInputEditText.setText(localNumber)
        }
    }

}
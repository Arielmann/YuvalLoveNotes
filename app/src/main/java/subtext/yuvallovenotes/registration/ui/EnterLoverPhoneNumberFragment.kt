package subtext.yuvallovenotes.registration.ui

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
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.utils.*
import subtext.yuvallovenotes.databinding.FragmentEnterLoverPhoneNumberBinding
import subtext.yuvallovenotes.registration.network.UserRegistrationCallback
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel


class EnterLoverPhoneNumberFragment : Fragment() {

    companion object {
        private val TAG: String = EnterLoverPhoneNumberFragment::class.simpleName!!
        const val PICK_CONTACT = 1
        const val READ_CONTACTS_PERMISSION_REQUEST_CODE = 2099
    }

    private lateinit var binding: FragmentEnterLoverPhoneNumberBinding
    private lateinit var sharedPrefs: SharedPreferences
    private val loginViewModel = get<RegistrationViewModel>()

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
        LoveUtils.setupFragmentDefaultToolbar(this, binding.enterLoverPhoneNumberToolBar)
        binding.loversLocalPhoneNumberInputEditText.requestFocus()
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
                findNavController().popBackStack(R.id.enterUserDetailsFragment, false)
                findNavController().navigate(EnterUserDetailsFragmentDirections.navigateToLetterGenerator())
            }

            override fun onError(error: String) {
                binding.loverPhoneNumberProgressBar.hide()
                binding.root.animate().alpha(1f).setDuration(100).start()
                Toast.makeText(YuvalLoveNotesApp.context, error, Toast.LENGTH_LONG).show()
            }

        }

        binding.loverPhoneNumberDoneBtn.setOnClickListener {

            binding.root.animate().alpha(0.5f).setDuration(200).start()
            binding.loverPhoneNumberProgressBar.show()
            val loverRegionNumber = binding.loversPhoneNumberRegionInputEditText.text.toString()
            val loverLocalNumber = binding.loversLocalPhoneNumberInputEditText.text.toString()
            val loverPhone = LoveLettersUser.Phone(loverRegionNumber, loverLocalNumber)
            val user = loginViewModel.getUserFromSharedPrefsData()
            user.loverPhone = loverPhone
            loginViewModel.requestRegistration(user, callback)

        }
    }

}



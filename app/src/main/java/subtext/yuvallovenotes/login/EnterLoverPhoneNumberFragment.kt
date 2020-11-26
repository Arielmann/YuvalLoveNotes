package subtext.yuvallovenotes.login

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentEnterLoverPhoneNumberBinding


class EnterLoverPhoneNumberFragment : Fragment() {

    companion object {
        private val TAG: String = EnterLoverPhoneNumberFragment::class.simpleName!!
    }

    lateinit var binding: FragmentEnterLoverPhoneNumberBinding
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnterLoverPhoneNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setOnDoneButtonClickListener()
        setPhoneRegionNumberEditText()
        binding.loversLocalPhoneNumberInputEditText.requestFocus()
        setBackButtonClickListener()
    }

    private fun setPhoneRegionNumberEditText() {
        val regionNumber = sharedPrefs.getString(resources.getString(R.string.pref_key_phone_region_number).takeUnless { it.isBlank() }, LoveUtils.getDeviceDefaultCountryCode())
        binding.loversPhoneNumberRegionInputEditText.setText(regionNumber)
    }

    private fun setBackButtonClickListener() {
        binding.enterLoverNumberImageContainerCL.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnDoneButtonClickListener() {
        binding.loverNumberDoneBtn.setOnClickListener {

            val regionNumber = binding.loversPhoneNumberRegionInputEditText.text.toString()
            val localNumber = binding.loversLocalPhoneNumberInputEditText.text.toString()
            if (LoveUtils.isPhoneNumberValid(regionNumber, localNumber)) {
                sharedPrefs.edit().putString(resources.getString(R.string.pref_key_phone_region_number), regionNumber).apply()
                sharedPrefs.edit().putString(resources.getString(R.string.pref_key_local_phone_number), localNumber).apply()
                sharedPrefs.edit().putString(resources.getString(R.string.pref_key_full_target_phone_number), regionNumber.plus(localNumber)).apply()
                findNavController().popBackStack(R.id.enterUserNameFragment, false)
                findNavController().navigate(EnterUserNameFragmentDirections.navigateToLetterGenerator())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
            }
        }
    }
}

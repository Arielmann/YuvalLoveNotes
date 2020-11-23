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
import com.google.i18n.phonenumbers.PhoneNumberUtil
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNumberBinding
import java.lang.NumberFormatException


class EnterLoverNumberFragment : Fragment() {

    companion object {
        private val TAG: String = EnterLoverNumberFragment::class.simpleName!!
    }

    lateinit var binding: FragmentEnterLoverNumberBinding
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnterLoverNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setOnDoneButtonClickListener()
        setPhoneNumberEditText()
        binding.loversPhoneNumberInputEditText.requestFocus()
        setBackButtonClickListener()
    }

    private fun setPhoneNumberEditText() {
        val regionNumber = sharedPrefs.getString(resources.getString(R.string.pref_key_target_region_number).takeUnless { it.isBlank() }, "+" + LoveUtils.getCountryCode().toString())
        binding.loversPhoneNumberRegionInputEditText.setText(regionNumber)
    }

    private fun setBackButtonClickListener() {
        binding.enterLoverNumberImageContainerCL.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnDoneButtonClickListener() {
        binding.loverNumberDoneBtn.setOnClickListener {

            try {
                val countryCode = binding.loversPhoneNumberRegionInputEditText.text.toString().replace("+", "")
                val localNumber = binding.loversPhoneNumberInputEditText.text.toString()
                if (LoveUtils.isPhoneNumberValid(countryCode.toInt(), localNumber)) {
                    sharedPrefs.edit().putString(resources.getString(R.string.pref_key_target_region_number), "+".plus(countryCode)).apply()
                    sharedPrefs.edit().putString(resources.getString(R.string.pref_key_target_phone_number), "+".plus(countryCode).plus(localNumber)).apply()
                    findNavController().navigate(EnterLoverNumberFragmentDirections.actionEnterNumberFragToEnterLoveGeneratorFrag())
                } else {
                    Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), resources.getString(R.string.error_invalid_lover_number_inserted), Toast.LENGTH_LONG).show()
            }
        }
    }

}
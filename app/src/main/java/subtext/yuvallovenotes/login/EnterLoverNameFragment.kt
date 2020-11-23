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
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNameBinding

class EnterLoverNameFragment : Fragment() {

    lateinit var binding : FragmentEnterLoverNameBinding
    lateinit var sharedPrefs : SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnterLoverNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setOnDoneButtonClickListener()
        setBackButtonClickListener()
        setupLoverNameEditText()
    }

    private fun setBackButtonClickListener() {
        binding.enterLoverNameImageContainerCL.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnDoneButtonClickListener() {
        binding.loverNameDoneBtn.setOnClickListener {
            if (!binding.loverNameInputEditText.text.isBlank()) {
                sharedPrefs.edit().putString(resources.getString(R.string.pref_key_lover_name), binding.loverNameInputEditText.text.toString()).apply()
                findNavController().navigate(EnterLoverNameFragmentDirections.navigateToLoverNumberFrag())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_no_lover_name_inserted), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupLoverNameEditText() {
        binding.loverNameInputEditText.requestFocus()
        val loverName =  sharedPrefs.getString(resources.getString(R.string.pref_key_lover_name), "")
        binding.loverNameInputEditText.setText(loverName)
    }

}
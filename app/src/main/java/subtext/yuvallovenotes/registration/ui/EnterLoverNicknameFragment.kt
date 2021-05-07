package subtext.yuvallovenotes.registration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNicknameBinding
import subtext.yuvallovenotes.registration.ui.viewbinding.EnterLoveNicknameBinding
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel

class EnterLoverNicknameFragment : Fragment() {

    companion object {
        private val TAG = EnterLoverNicknameFragment::class.simpleName
    }

    private lateinit var binding: EnterLoveNicknameBinding
    private val registrationViewModel = get<RegistrationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LoveUtils.getLayoutsProvider(inflater, container, false).enterLoveNicknameBinding
        return binding.root()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loverNicknameInputEditText().requestFocus()
        setOnDoneButtonClickListener()
        setupLoverNameEditText()
        binding.loverNicknameBackBtn().setOnClickListener { findNavController().popBackStack() }
        binding.loverNicknameBackButtonContainingCL().setOnClickListener { findNavController().popBackStack() }
    }

    private fun setOnDoneButtonClickListener() {
        binding.loverNicknameContinueBtn().setOnClickListener {
            val loverNickName = binding.loverNicknameInputEditText().text.toString()
            val user = registrationViewModel.getUserFromSharedPrefsData()
            if (registrationViewModel.saveLoverNickname(loverNickName)) {
                user.loverNickName = loverNickName
                findNavController().navigate(EnterLoverNicknameFragmentDirections.navigateToLoverPhoneNumber())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_no_lover_nickname_inserted), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupLoverNameEditText() {
        binding.loverNicknameInputEditText().requestFocus()
        val loverName = registrationViewModel.loverNickname
        binding.loverNicknameInputEditText().setText(loverName)
    }
}

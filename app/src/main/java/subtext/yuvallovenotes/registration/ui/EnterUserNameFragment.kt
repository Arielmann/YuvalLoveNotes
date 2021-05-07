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
import subtext.yuvallovenotes.registration.ui.viewbinding.EnterUserNameBinding
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel

class EnterUserNameFragment : Fragment() {

    lateinit var binding : EnterUserNameBinding
    private val registrationViewModel = get<RegistrationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LoveUtils.getLayoutsProvider(inflater, container, false).enterUserNameBinding
        return binding.root()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userNameInputEditText().requestFocus()
        setupUserNameEditText()
        setOnDoneButtonClickListener()
    }

    private fun setOnDoneButtonClickListener() {
        binding.userNameContinueBtn().setOnClickListener {
            if (binding.userNameInputEditText().text.isNotBlank()) {
                registrationViewModel.onUserNameSubmitted(binding.userNameInputEditText().text.toString())
                findNavController().navigate(EnterUserNameFragmentDirections.navigateToUserGender())
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_no_user_name_inserted), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUserNameEditText() {
        binding.userNameInputEditText().requestFocus()
        val userName = registrationViewModel.userName
        binding.userNameInputEditText().setText(userName)
    }
}
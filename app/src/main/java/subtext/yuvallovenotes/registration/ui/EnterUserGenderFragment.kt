package subtext.yuvallovenotes.registration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.users.Gender
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentEnterUserGenderBinding
import subtext.yuvallovenotes.registration.ui.viewbinding.EnterUserGenderBinding
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel

class EnterUserGenderFragment : Fragment() {

    private lateinit var binding: EnterUserGenderBinding
    private val registrationViewModel = get<RegistrationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LoveUtils.getLayoutsProvider(inflater, container, false).enterUserGenderBinding
        return binding.root()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setOnDoneButtonClickListener()
        startDataObservation()
    }

    private fun startDataObservation() {
        registrationViewModel.loverGender
    }

    private fun setOnClickListeners() {

        binding.chooseUserGenderBackBtn().setOnClickListener { findNavController().popBackStack() }

        binding.userGenderWomanAvatarIV().setOnClickListener {
            registrationViewModel.userGender = Gender.WOMAN
            registrationViewModel.loverGender = Gender.MAN
            binding.userGenderWomanAvatarIV().setImageResource(R.drawable.avatar_woman_selected)
            binding.userGenderManAvatarIV().setImageResource(R.drawable.avatar_man_deep_blue_bg)
        }

        binding.userGenderManAvatarIV().setOnClickListener {
            registrationViewModel.userGender = Gender.MAN
            registrationViewModel.loverGender = Gender.WOMAN
            binding.userGenderManAvatarIV().setImageResource(R.drawable.avatar_man_selected)
            binding.userGenderWomanAvatarIV().setImageResource(R.drawable.avatar_woman_deep_blue_bg)
        }
    }

    private fun setOnDoneButtonClickListener() {
        binding.chooseUserGenderContinueBtn().setOnClickListener {
            findNavController().navigate(R.id.navigate_to_lover_name)
        }
    }

}
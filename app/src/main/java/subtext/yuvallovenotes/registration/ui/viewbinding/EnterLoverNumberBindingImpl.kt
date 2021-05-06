package subtext.yuvallovenotes.registration.ui.viewbinding

import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterLoverPhoneNumberBinding

class EnterLoverNumberBindingImpl {
    fun enterLoverNumberTV() : TextView
    fun enterLoverPhoneNumberBackgroundDecorationsCL() : ConstraintLayout
    fun enterLoverPhoneNumberMainCL() : ConstraintLayout
    fun enterPhoneDialogHyphenTV() : TextView
    fun loverNumberBackButtonContainingCL() : ConstraintLayout
    fun loverNumberBackBtn() : Button
    fun loverNumberProgressBar() : ProgressBar
    fun loverNumberChooseFromContactsBtn() : Button
    fun loverPhoneNumberDoneBtn() : Button
    fun loversPhoneNumberRegionInputEditText() : EditText
    fun loversLocalPhoneNumberInputEditText() : EditText
}

class hi{
    val binding = FragmentEnterLoverPhoneNumberBinding.inflate(inflater, container, false)
    fun hello(){
        binding.enterLoverNumberTV
        binding.enterLoverPhoneNumberBackgroundDecorationsCL
        binding.enterLoverPhoneNumberMainCL
        binding.enterPhoneDialogHyphenTV
        binding.loverNumberBackButtonContainingCL
        binding.loverNumberBackBtn
        binding.loverNumberProgressBar
        binding.loverNumberChooseFromContactsBtn
        binding.loverPhoneNumberDoneBtn
        binding.loversPhoneNumberRegionInputEditText
        binding.loversLocalPhoneNumberInputEditText
    }

}
package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterUserNameBinding

interface EnterUserNameBinding {
    fun userNameTitleIV() : ImageView
    fun userNameInputEditText() : EditText
    fun enterUserNameTV() : TextView
    fun lowerPositioningView() : View
    fun userNameAppDescriptionTV() : TextView
    fun userNameBackgroundDecorations() : ImageView
    fun userNameBackgroundDecorationsCL() : ConstraintLayout
    fun userNameBackgroundDecorationsDotsAndStars() : ImageView
    fun userNameContinueBtn() : Button
    fun userNameTitleShadowIV() : ImageView
    fun userNameUpperIV() : ImageView
}

class hi{
    val binding = FragmentEnterUserNameBinding.inflate(inflater, container, false)
    fun hello(){
        binding.userNameTitleIV
        binding.userNameInputEditText
        binding.enterUserNameTV
        binding.lowerPositioningView
        binding.userNameAppDescriptionTV
        binding.userNameBackgroundDecorations
        binding.userNameBackgroundDecorationsCL
        binding.userNameBackgroundDecorationsDotsAndStars
        binding.userNameContinueBtn
        binding.userNameTitleShadowIV
        binding.userNameUpperIV
    }

}
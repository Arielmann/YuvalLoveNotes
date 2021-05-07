package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNicknameBinding
import subtext.yuvallovenotes.databinding.FragmentEnterUserGenderBinding
import subtext.yuvallovenotes.databinding.FragmentEnterUserNameBinding

class EnterUserNameBindingImpl(val inflater: LayoutInflater, @Nullable val container: ViewGroup?, val attachToRoot: Boolean) : EnterUserNameBinding {

    val binding = FragmentEnterUserNameBinding.inflate(inflater, container, false)

    override fun userNameTitleIV(): ImageView {
        return binding.userNameTitleIV
    }

    override fun userNameInputEditText(): EditText {
        return binding.userNameInputEditText
    }

    override fun enterUserNameTV(): TextView {
        return binding.enterUserNameTV
    }

    override fun lowerPositioningView(): View {
        return binding.lowerPositioningView
    }

    override fun userNameAppDescriptionTV(): TextView {
        return binding.userNameAppDescriptionTV
    }

    override fun userNameBackgroundDecorations(): ImageView {
        return binding.userNameBackgroundDecorations
    }

    override fun userNameBackgroundDecorationsCL(): ConstraintLayout {
        return binding.userNameBackgroundDecorationsCL
    }

    override fun userNameTitleCL(): ConstraintLayout {
        return binding.userNameTitleCL
    }

    override fun userNameBackgroundDecorationsDotsAndStars(): ImageView {
        return binding.userNameBackgroundDecorationsDotsAndStars
    }

    override fun userNameContinueBtn(): Button {
        return binding.userNameContinueBtn
    }

    override fun userNameTitleShadowIV(): ImageView {
        return binding.userNameTitleShadowIV
    }

    override fun userNameUpperIV(): ImageView {
        return binding.userNameUpperIV
    }

    override fun root(): View {
        return binding.root
    }

}

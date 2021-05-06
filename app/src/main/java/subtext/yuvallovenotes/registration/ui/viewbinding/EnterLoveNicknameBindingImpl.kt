package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNicknameBinding
import subtext.yuvallovenotes.databinding.FragmentEnterUserNameBinding

class EnterLoveNicknameBindingImpl(val inflater: LayoutInflater, @Nullable val container: ViewGroup, val attachToRoot: Boolean) : EnterLoveNicknameBinding {

    val binding = FragmentEnterLoverNicknameBinding.inflate(inflater, container, false)

    override fun loverNicknameBackButtonContainingCL(): ConstraintLayout {
        return binding.loverNicknameBackButtonContainingCL
    }

    override fun enterLoverNicknameTV(): TextView {
        return binding.enterLoverNicknameTV

    }

    override fun loverDetailsTitleIv(): TextView {
        return binding.loverDetailsTitleTV

    }

    override fun loverNicknameAppDescriptionTV(): TextView {
        return binding.loverNicknameAppDescriptionTV

    }

    override fun loverNicknameBackBtn(): ImageButton {
        return binding.loverNicknameBackBtn

    }

    override fun loverNicknameBackgroundDecorations(): ImageView {

        return binding.loverNicknameBackgroundDecorations
    }


    override fun loverNicknameBackgroundDecorationsCL(): ConstraintLayout {
        return binding.loverNicknameBackgroundDecorationsCL

    }

    override fun loverNicknameBackgroundDecorationsDotsAndStars(): ImageView {
        return binding.loverNicknameBackgroundDecorationsDotsAndStars

    }

    override fun loverNicknameContinueBtn(): Button {
        return binding.loverNicknameContinueBtn

    }

    override fun loverNicknameInputEditText(): EditText {
        return binding.loverNicknameInputEditText

    }

    override fun loverNicknameUpperImageView(): ImageView {
        return binding.loverNicknameUpperImageView

    }

    override fun middlePositioningView(): View {
        return binding.loverNicknameUpperImageView
    }

}

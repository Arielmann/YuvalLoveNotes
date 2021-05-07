package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
import subtext.yuvallovenotes.databinding.FragmentEnterLoverPhoneNumberBinding

class EnterLoverPhoneNumberBindingImpl(val inflater: LayoutInflater, @Nullable val container: ViewGroup?, val attachToRoot: Boolean) : EnterLoverPhoneNumberBinding {

    val binding = FragmentEnterLoverPhoneNumberBinding.inflate(inflater, container, false)

    override fun chooseUserGenderTitleTV(): TextView {
        return binding.loverNumberRememberTitleTV
    }

    override fun chooseUserGenderTitleShadowTV(): TextView {
        return binding.loverNumberRememberTitleShadowTV
    }

    override fun enterLoverNumberTV(): TextView {
        return binding.enterLoverNumberTV
    }

    override fun enterLoverPhoneNumberBackgroundDecorationsCL(): ConstraintLayout {
        return binding.enterLoverPhoneNumberBackgroundDecorationsCL

    }

    override fun enterLoverPhoneNumberMainCL(): ConstraintLayout {
        return binding.enterLoverPhoneNumberMainCL

    }

    override fun enterPhoneDialogHyphenTV(): TextView {
        return binding.enterPhoneDialogHyphenTV

    }

    override fun loverNumberBackButtonContainingCL(): ConstraintLayout {
        return binding.loverNumberBackButtonContainingCL

    }

    override fun loverNumberBackBtn(): ImageButton {

        return binding.loverNumberBackBtn
    }

    override fun loverNumberProgressBar(): ContentLoadingProgressBar {
        return binding.loverNumberProgressBar
    }


    override fun loverNumberChooseFromContactsBtn(): Button {
        return binding.loverNumberChooseFromContactsBtn

    }

    override fun loverPhoneNumberDoneBtn(): Button {
        return binding.loverPhoneNumberDoneBtn

    }

    override fun loversPhoneNumberRegionInputEditText(): EditText {
        return binding.loversPhoneNumberRegionInputEditText

    }

    override fun loversLocalPhoneNumberInputEditText(): EditText {
        return binding.loversLocalPhoneNumberInputEditText
    }

    override fun root(): View {
        return binding.root
    }
}
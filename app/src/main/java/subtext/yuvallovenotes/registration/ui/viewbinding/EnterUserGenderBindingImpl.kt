package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNicknameBinding
import subtext.yuvallovenotes.databinding.FragmentEnterUserGenderBinding

class EnterUserGenderBindingImpl(val inflater: LayoutInflater, @Nullable val container: ViewGroup, val attachToRoot: Boolean) : EnterUserGenderBinding {

    val binding = FragmentEnterUserGenderBinding.inflate(inflater, container, false)

    override fun chooseUserGenderTitleIv(): TextView {
        return binding.chooseUserGenderTitleTV

    }

    override fun chooseUserGenderTV(): TextView {
        return binding.chooseUserGenderTV
    }

    override fun chooseUserGenderBackBtn(): ImageButton {
        return binding.chooseUserGenderBackBtn
    }

    override fun userGenderManAvatarIV(): ImageView {
        return binding.userGenderManAvatarIV

    }

    override fun chooseUserGenderContinueBtn(): Button {
        return binding.chooseUserGenderContinueBtn

    }

    override fun userGenderWomanAvatarIV(): ImageView {
        return binding.userGenderWomanAvatarIV

    }


    override fun chooseUserGenderAppDescriptionTV(): TextView {
        return binding.chooseUserGenderAppDescriptionTV

    }

    override fun chooseUserGenderBackButtonContainingCL(): ConstraintLayout {
        return binding.chooseUserGenderBackButtonContainingCL

    }

    override fun chooseUserGenderBackgroundDecorations(): ImageView {
        return binding.chooseUserGenderBackgroundDecorations

    }

    override fun chooseUserGenderBackgroundDecorationsCL(): ConstraintLayout {
        return binding.chooseUserGenderBackgroundDecorationsCL

    }

    override fun chooseUserGenderBackgroundDecorationsDotsAndStars(): ImageView {
        return binding.chooseUserGenderBackgroundDecorationsDotsAndStars

    }

    override fun chooseUserGenderUpperImageView(): ImageView {
        return binding.chooseUserGenderUpperImageView

    }

    override fun middlePositioningView(): View {
        return binding.middlePositioningView

    }


}

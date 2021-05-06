package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterUserGenderBinding

interface EnterUserGenderBinding {
    fun chooseUserGenderTitleIv(): TextView
    fun chooseUserGenderTV(): TextView
    fun chooseUserGenderBackBtn(): ImageButton
    fun userGenderManAvatarIV(): ImageView
    fun chooseUserGenderContinueBtn(): Button
    fun userGenderWomanAvatarIV(): ImageView
    fun chooseUserGenderAppDescriptionTV(): TextView
    fun chooseUserGenderBackButtonContainingCL(): ConstraintLayout
    fun chooseUserGenderBackgroundDecorations(): ImageView
    fun chooseUserGenderBackgroundDecorationsCL(): ConstraintLayout
    fun chooseUserGenderBackgroundDecorationsDotsAndStars(): ImageView
    fun middlePositioningView(): View
    fun chooseUserGenderUpperImageView(): ImageView
}
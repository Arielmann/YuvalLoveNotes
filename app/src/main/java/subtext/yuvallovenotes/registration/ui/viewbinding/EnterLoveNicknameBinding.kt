package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.databinding.FragmentEnterLoverNicknameBinding

interface EnterLoveNicknameBinding {
    fun loverNicknameBackButtonContainingCL(): ConstraintLayout
    fun enterLoverNicknameTV(): TextView
    fun loverDetailsTitleIv(): TextView
    fun loverNicknameAppDescriptionTV(): TextView
    fun loverNicknameBackBtn(): ImageButton
    fun loverNicknameBackgroundDecorations(): ImageView
    fun loverNicknameBackgroundDecorationsCL(): ConstraintLayout
    fun loverNicknameBackgroundDecorationsDotsAndStars(): ImageView
    fun loverNicknameContinueBtn(): Button
    fun loverNicknameInputEditText(): EditText
    fun loverNicknameUpperImageView(): ImageView
    fun middlePositioningView(): View
}

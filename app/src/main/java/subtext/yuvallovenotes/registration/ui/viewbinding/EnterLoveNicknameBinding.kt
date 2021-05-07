package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.crossapplication.viewbinding.Binding

interface EnterLoveNicknameBinding : Binding {
    fun loverNicknameBackButtonContainingCL(): ConstraintLayout
    fun enterLoverNicknameTV(): TextView
    fun loverDetailsTitleTV(): TextView
    fun loverDetailsTitleShadowTV(): TextView
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

package subtext.yuvallovenotes.registration.ui.viewbinding

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import subtext.yuvallovenotes.crossapplication.viewbinding.Binding

interface EnterUserNameBinding : Binding {
    fun userNameTitleIV() : ImageView
    fun userNameTitleShadowIV() : ImageView
    fun userNameInputEditText() : EditText
    fun enterUserNameTV() : TextView
    fun lowerPositioningView() : View
    fun userNameAppDescriptionTV() : TextView
    fun userNameBackgroundDecorations() : ImageView
    fun userNameBackgroundDecorationsCL() : ConstraintLayout
    fun userNameTitleCL() : ConstraintLayout
    fun userNameBackgroundDecorationsDotsAndStars() : ImageView
    fun userNameContinueBtn() : Button
    fun userNameUpperIV() : ImageView
}

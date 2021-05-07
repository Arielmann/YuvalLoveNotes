package subtext.yuvallovenotes.registration.ui.viewbinding

import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
import subtext.yuvallovenotes.crossapplication.viewbinding.Binding

interface EnterLoverPhoneNumberBinding : Binding {
    fun chooseUserGenderTitleTV() : TextView
    fun chooseUserGenderTitleShadowTV() : TextView
    fun enterLoverNumberTV() : TextView
    fun enterLoverPhoneNumberBackgroundDecorationsCL() : ConstraintLayout
    fun enterLoverPhoneNumberMainCL() : ConstraintLayout
    fun enterPhoneDialogHyphenTV() : TextView
    fun loverNumberBackButtonContainingCL() : ConstraintLayout
    fun loverNumberBackBtn() : ImageButton
    fun loverNumberProgressBar() : ContentLoadingProgressBar
    fun loverNumberChooseFromContactsBtn() : Button
    fun loverPhoneNumberDoneBtn() : Button
    fun loversPhoneNumberRegionInputEditText() : EditText
    fun loversLocalPhoneNumberInputEditText() : EditText
}

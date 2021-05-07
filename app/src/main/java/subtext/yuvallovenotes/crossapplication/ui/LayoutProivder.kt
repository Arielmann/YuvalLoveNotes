package subtext.yuvallovenotes.crossapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Nullable
import subtext.yuvallovenotes.registration.ui.viewbinding.*

class LayoutProvider private constructor () {

    companion object {

        fun default(inflater: LayoutInflater, @Nullable container: ViewGroup?, attachToRoot: Boolean): LayoutProvider {
            val provider = LayoutProvider()
            provider.enterUserNameBinding = EnterUserNameBindingImpl(inflater, container, attachToRoot)
            provider.enterUserGenderBinding = EnterUserGenderBindingImpl(inflater, container, attachToRoot)
            provider.enterLoveNicknameBinding = EnterLoveNicknameBindingImpl(inflater, container, attachToRoot)
            provider.enterLoverPhoneNumberBinding = EnterLoverPhoneNumberBindingImpl(inflater, container, attachToRoot)
            return provider

        }

        fun height800Screen(inflater: LayoutInflater, @Nullable container: ViewGroup?, attachToRoot: Boolean): LayoutProvider {
            val provider = LayoutProvider()
            provider.enterUserNameBinding = EnterUserName600hBindingImpl(inflater, container, attachToRoot)
            provider.enterUserGenderBinding = EnterUserGender600hBindingImpl(inflater, container, attachToRoot)
            provider.enterLoveNicknameBinding = EnterLoveNickname600hBindingImpl(inflater, container, attachToRoot)
            provider.enterLoverPhoneNumberBinding = EnterLoverPhoneNumber600hBindingImpl(inflater, container, attachToRoot)
            return provider
        }
    }

    lateinit var enterUserNameBinding: EnterUserNameBinding
    lateinit var enterUserGenderBinding : EnterUserGenderBinding
    lateinit var enterLoveNicknameBinding : EnterLoveNicknameBinding
    lateinit var enterLoverPhoneNumberBinding : EnterLoverPhoneNumberBinding
}

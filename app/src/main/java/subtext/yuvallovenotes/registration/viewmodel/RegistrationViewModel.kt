package subtext.yuvallovenotes.registration.viewmodel

import android.content.SharedPreferences
import android.util.Log.*
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.backendless.push.DeviceRegistrationResult
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.koin.java.KoinJavaComponent
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.users.UnRegisteredLoveLettersUser
import subtext.yuvallovenotes.crossapplication.network.NetworkCallback
import subtext.yuvallovenotes.crossapplication.utils.getDeviceDefaultCountryCode
import subtext.yuvallovenotes.crossapplication.utils.isPhoneNumberValid
import subtext.yuvallovenotes.registration.network.RegistrationRepository
import subtext.yuvallovenotes.registration.network.UserRegistrationCallback
import java.util.*


class RegistrationViewModel : ViewModel() {

    companion object {
        private val TAG = RegistrationViewModel::class.simpleName
    }

    private val repository: RegistrationRepository = KoinJavaComponent.get(RegistrationRepository::class.java)
    private val prefs: SharedPreferences = KoinJavaComponent.get(SharedPreferences::class.java)

    /**
     * Request a registration, i.e, if the input data is valid, register the user.
     * @param user An user details object that are not yet verified to be in the data base.
     * @param callback Callback to call when the process succeeds or fails
     */
    fun requestRegistration(user: UnRegisteredLoveLettersUser, callback: UserRegistrationCallback) {

//        onSuccess.invoke() - Use when you want to quick-skip registration process
        val userNetworkRegistrationRequestExecutor = UserNetworkRegistrationExecutor(user, callback)
        if (userInputValidation(user, callback)) {
            userNetworkRegistrationRequestExecutor.execute()
        }
    }

    /**
     * Returns true if the input data is valid
     * @param user An user details object.
     * @param callback Callback to call when an error occurred
     */
    private fun userInputValidation(user: LoveLettersUser, callback: UserRegistrationCallback): Boolean {
        d(TAG, "Validating user input")
        var isValid = false
        if (allFieldsHaveText(user, callback)) {
            d(TAG, "Fields are not blank")
            if (isPhoneNumberValid(user.loverPhone, callback)) {
                isValid = true
            }
        }
        return isValid
    }

    /**
     * Helper class for the user registraion process.
     *
     * The registration is made out of two parallel steps.
     * 1. Register the user
     * 2. Register the device to the push notifications service
     */
    private inner class UserNetworkRegistrationExecutor(val user: UnRegisteredLoveLettersUser, callback: UserRegistrationCallback) {

        /**
         * Starts the registration process
         */
        fun execute() {
            prefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_registered_in_server), false).apply()
            repository.registerUser(user, registerUserCallback)
            val locale = Locale.getDefault().toString()
            val pushNotificationChannels = mutableListOf(locale)
            if(locale != YuvalLoveNotesApp.context.getString(R.string.default_country_code)){
                /*If user is not Israeli, put him in a general english channel for messages*/
                pushNotificationChannels.add("general_english")
            }
            repository.registerToPushNotificationsService(pushNotificationChannels, registerNotificationsCallback)
        }

        private val registerUserCallback = object : NetworkCallback<LoveLettersUser> {

            override fun onSuccess(response: LoveLettersUser) {
                d(TAG, "User registration successful")
                val context = YuvalLoveNotesApp.context
                saveUserDataToPrefs(response)
                prefs.edit().putBoolean(context.getString(R.string.pref_key_user_registered_in_server), true).apply()
                val isDeviceRegistered = prefs.getBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), false)
                if (isDeviceRegistered) {
                    d(TAG, "User registration callback: Successful user and device registration on server")
                    callback.onSuccess()
                } else {
                    d(TAG, "User registration callback: Waiting for device registration to notifications")
                }
            }

            override fun onFailure(message: String) {
                e(TAG, "User registration failure: $message")
                callback.onError(message)
            }
        }

        private val registerNotificationsCallback = object : NetworkCallback<DeviceRegistrationResult> {

            override fun onSuccess(response: DeviceRegistrationResult) {
                d(TAG, "Device registered to notifications")
                val context = YuvalLoveNotesApp.context
                prefs.edit().putBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), true).apply()
                val isUserRegistered = prefs.getBoolean(context.getString(R.string.pref_key_user_registered_in_server), false)
                if (isUserRegistered) {
                    d(TAG, "Notifications registration callback: Successful device and user registration on server")
                    callback.onSuccess()
                } else {
                    d(TAG, "Notifications registration callback: Waiting for user registration")
                }
            }

            override fun onFailure(message: String) {
                e(TAG, "Device registration failure: $message")
                callback.onError(message)
            }
        }

    }

    private fun isPhoneNumberValid(phone: LoveLettersUser.Phone, callback: UserRegistrationCallback): Boolean {
        val defaultError = YuvalLoveNotesApp.context.getString(R.string.error_invalid_lover_number_inserted)
        if (PhoneNumberUtil.getInstance().isPhoneNumberValid(phone.regionNumber, phone.localNumber)) {
            return true
        }
        callback.onError(defaultError)
        return false
    }

    private fun allFieldsHaveText(user: LoveLettersUser, callback: UserRegistrationCallback): Boolean {
        val context = YuvalLoveNotesApp.context

        if (user.userName.isBlank()) {
            callback.onError(context.getString(R.string.error_no_user_name_inserted))
            return false
        }

        if (user.loverNickName.isBlank()) {
            callback.onError(context.getString(R.string.error_no_lover_nickname_inserted))
            return false
        }

        if (user.loverPhone.isBlank()) {
            callback.onError(context.getString(R.string.error_invalid_lover_number_inserted))
            return false
        }

        return true
    }

    fun requestUserPhoneNumber(onCompletion: (regionNumber: String, localNumber: String) -> Unit) {
        val context = YuvalLoveNotesApp.context
        val defaultRegion = PhoneNumberUtil.getInstance().getDeviceDefaultCountryCode()
        val region = prefs.getString(context.getString(R.string.pref_key_lover_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val local = prefs.getString(context.getString(R.string.pref_key_lover_local_phone_number), "")!!
        onCompletion.invoke(region, local)
    }


    private fun saveUserDataToPrefs(user: LoveLettersUser) {
        d(TAG, "Saving user data to shared prefs: $user") //todo: remove user details logging
        val context = YuvalLoveNotesApp.context
        prefs.edit {
            putString(context.getString(R.string.pref_key_user_name), user.randomIdentifier)
            putString(context.getString(R.string.pref_key_user_name), user.userName)
            putString(context.getString(R.string.pref_key_user_phone_region_number), user.userPhone.regionNumber)
            putString(context.getString(R.string.pref_key_user_local_phone_number), user.userPhone.localNumber)
            putString(context.getString(R.string.pref_key_user_full_target_phone_number), user.userPhone.fullNumber)
            putString(context.getString(R.string.pref_key_lover_nickname), user.loverNickName)
            putString(context.getString(R.string.pref_key_lover_phone_region_number), user.loverPhone.regionNumber)
            putString(context.getString(R.string.pref_key_lover_local_phone_number), user.loverPhone.localNumber)
            putString(context.getString(R.string.pref_key_lover_full_target_phone_number), user.loverPhone.fullNumber)
        }
    }

    @Suppress("UnnecessaryVariable")
    fun getUserFromSharedPrefsData(): UnRegisteredLoveLettersUser {
        val context = YuvalLoveNotesApp.context

        val userName = prefs.getString(context.getString(R.string.pref_key_user_name).takeUnless { it.isBlank() }, "")!!
        val loverNickName = prefs.getString(context.getString(R.string.pref_key_lover_nickname).takeUnless { it.isBlank() }, "")!!

        val defaultRegion = PhoneNumberUtil.getInstance().getDeviceDefaultCountryCode()
        val loverRegionNumber = prefs.getString(context.getString(R.string.pref_key_lover_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val loverLocalNumber = prefs.getString(context.getString(R.string.pref_key_lover_local_phone_number), "")!!
        val loverPhone = LoveLettersUser.Phone(loverRegionNumber, loverLocalNumber)

        val userRegionNumber = prefs.getString(context.getString(R.string.pref_key_user_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val userLocalNumber = prefs.getString(context.getString(R.string.pref_key_user_local_phone_number), "")!!
        val userPhone = LoveLettersUser.Phone(userRegionNumber, userLocalNumber)

        val result = UnRegisteredLoveLettersUser(userName, loverNickName, userPhone, loverPhone)
        return result
    }

    fun saveLoverNickname(loverNickName: String): Boolean {
        return if (loverNickName.isNotBlank()) {
            prefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), loverNickName).apply()
            true
        } else {
            w(TAG, "Empty lover nickname inserted")
            false
        }
    }

}
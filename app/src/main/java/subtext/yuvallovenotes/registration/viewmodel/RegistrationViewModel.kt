package subtext.yuvallovenotes.registration.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log.*
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.backendless.push.DeviceRegistrationResult
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.localization.inferLanguageFromLocale
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.users.Gender
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.users.UnRegisteredLoveLettersUser
import subtext.yuvallovenotes.crossapplication.network.NetworkCallback
import subtext.yuvallovenotes.crossapplication.utils.getDeviceDefaultCountryCode
import subtext.yuvallovenotes.crossapplication.utils.isPhoneNumberValid
import subtext.yuvallovenotes.registration.network.RegistrationRepository
import subtext.yuvallovenotes.registration.network.AppRegistrationCallback
import java.util.*


class RegistrationViewModel : ViewModel() {

    companion object {
        private val TAG = RegistrationViewModel::class.simpleName
    }

    private var displayDeviceRegistrationErrorToUser: Boolean = true
    private var displayRequestLoveLettersErrorToUser: Boolean = true
    private val registrationRepository: RegistrationRepository = KoinJavaComponent.get(RegistrationRepository::class.java)
    private val loveItemsRepository: LoveItemsRepository = LoveItemsRepository() // In order to save time, server data fetching is done during the registration process
    private val sharedPrefs: SharedPreferences = KoinJavaComponent.get(SharedPreferences::class.java)
    private var appRegistrationCallback: AppRegistrationCallback? = null

    var userName: String = ""
        get() = sharedPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_name), "")!!
        private set(value) {
            if (value.isNotEmpty()) {
                sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_name), value).apply()
            }
            field = value
        }

    /**
     * Gender of this user. Only settable upon registration.
     * Upon setting, all letters will be deleted to allow new ones to be
     * downloaded according to the new gender.
     */
    var userGender: Gender = Gender.MAN
        set(value) {
            sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_gender), value.name).apply()
            sharedPrefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_default_letters_downloaded), false).apply()
            CoroutineScope(Dispatchers.IO).launch {
                loveItemsRepository.deleteAllLoveLetters()
            }
            field = value
        }

    var loverNickname: String = ""
        get() = sharedPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), "")!!
        private set(value) {
            if (value.isNotEmpty()) {
                sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), value).apply()
            }
            field = value
        }

    /**
     * Gender of this lover. Only settable upon registration.
     * Upon setting, all letters will be deleted to allow new ones to be
     * downloaded according to the new gender.
     */
    var loverGender: Gender = Gender.WOMAN
        set(value) {
            sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_gender), value.name).apply()
            sharedPrefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_default_letters_downloaded), false).apply() //
            CoroutineScope(Dispatchers.IO).launch {
                loveItemsRepository.deleteAllLoveLetters()
            }
            field = value
        }


    /**
     * Request a registration, i.e, if the input data is valid, register the user.
     *
     * The registration is made out of three parallel steps.
     * 1. Register the user
     * 2. Register the device to the push notifications service
     * 3. Download letters database from server
     *
     * Note that database download and device registration are silently executed during
     * the registration process before user asks top register so most chances that they won't be required.
     *
     * @param user An user details object that are not yet verified to be in the data base.
     * @param registrationCallback Callback to call when the process succeeds or fails
     */
    fun requestRegistration(user: UnRegisteredLoveLettersUser, registrationCallback: AppRegistrationCallback) {
        appRegistrationCallback = registrationCallback
        if (userInputValidation(user, registrationCallback)) {
            requestUserRegistration(user)
            requestDeviceRegistration()
            requestLoveLettersFromServer()
        }
    }

    /**
     * Returns true if the input data is valid
     * @param user An user details object.
     * @param callbackApp Callback to call when an error occurred
     */
    private fun userInputValidation(user: LoveLettersUser, callbackApp: AppRegistrationCallback): Boolean {
        d(TAG, "Validating user input")
        var isValid = false
        if (allFieldsHaveText(user, callbackApp)) {
            d(TAG, "Fields are not blank")
            if (isPhoneNumberValid(user.loverPhone, callbackApp)) {
                isValid = true
            }
        }
        return isValid
    }

    private fun requestUserRegistration(user: UnRegisteredLoveLettersUser) {
        val isUserRegistered = sharedPrefs.getBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_registered_in_server), false)
        if (!isUserRegistered) {
            registrationRepository.registerUser(user, registerUserCallback)
        } else {
            if (allRegistrationProcessesFinished()) {
                d(TAG, "User already registered. All registration processes completed")
                appRegistrationCallback?.onSuccess()
            } else {
                d(TAG, "User registration callback: Waiting for other registration processes to complete")
            }
        }
    }

    private val registerUserCallback = object : NetworkCallback<LoveLettersUser> {

        override fun onSuccess(response: LoveLettersUser) {
            d(TAG, "User registration successful")
            val context = YuvalLoveNotesApp.context
            sharedPrefs.edit().putBoolean(context.getString(R.string.pref_key_user_registered_in_server), true).apply()
            saveUserDataToPrefs(response)
            if (allRegistrationProcessesFinished()) {
                d(TAG, "User registration callback: All registration processes completed")
                appRegistrationCallback?.onSuccess()
            } else {
                d(TAG, "User registration callback: Waiting for other registration processes to complete")
            }
        }

        override fun onFailure(message: String) {
            e(TAG, "User registration failure: $message")
            val msgForDisplay = YuvalLoveNotesApp.context.getString(R.string.error_default_registration_failure)
            appRegistrationCallback?.onError(msgForDisplay)
        }
    }

    private val downloadDefaultLettersCallback = object : NetworkCallback<MutableList<LoveLetter>> {

        override fun onSuccess(response: MutableList<LoveLetter>) {
            viewModelScope.launch(Dispatchers.IO) {
                prepareLetterListForUsage(response)
                loveItemsRepository.insertAllLoveLetters(response)
            }

            sharedPrefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_default_letters_downloaded), true).apply()

            if (allRegistrationProcessesFinished()) {
                d(TAG, "Notifications registration callback: All registration processes completed")
                appRegistrationCallback?.onSuccess()
            } else {
                d(TAG, "Notifications registration callback: Waiting for other registration processes to complete")
            }
        }

        override fun onFailure(message: String) {
            if(displayRequestLoveLettersErrorToUser) {
                e(TAG, "Error while downloading letters from server: $message")
                appRegistrationCallback?.onError(YuvalLoveNotesApp.context.getString(R.string.error_default_registration_failure))
            }
        }

        private fun prepareLetterListForUsage(letters: MutableList<LoveLetter>) {
            val loverNickname = sharedPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), YuvalLoveNotesApp.context.getString(R.string.lover_nickname_fallback))
            letters.forEach {
                if (it.autoInsertLoverNicknameAsOpener) {
                    it.text = loverNickname + "," + it.text
                    it.autoInsertLoverNicknameAsOpener = false
                }
            }
        }
    }

    fun requestDeviceRegistration(displayDeviceRegistrationErrorToUser: Boolean = true) {
        this.displayDeviceRegistrationErrorToUser = displayDeviceRegistrationErrorToUser
        val isDeviceRegistered = sharedPrefs.getBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_device_registered_to_push_notifications), false)
        if (!isDeviceRegistered) {
            val locale = Locale.getDefault().toString()
            val pushNotificationChannels = mutableListOf(locale)
            if (locale != YuvalLoveNotesApp.context.getString(R.string.default_country_code)) {
                /*If user is not Israeli, put him in a general english channel for messages*/
                pushNotificationChannels.add("general_english")
            }

            registrationRepository.registerToPushNotificationsService(pushNotificationChannels, deviceRegisterNotificationsCallback)
        }
    }

    private val deviceRegisterNotificationsCallback = object : NetworkCallback<DeviceRegistrationResult> {

        override fun onSuccess(response: DeviceRegistrationResult) {
            d(TAG, "Device registered to notifications")
            val context = YuvalLoveNotesApp.context
            sharedPrefs.edit().putBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), true).apply()
            if (allRegistrationProcessesFinished()) {
                d(TAG, "Device registration callback: All registration processes completed")
                appRegistrationCallback?.onSuccess()
            } else {
                d(TAG, "Device registration callback: Waiting for other registration processes to complete")
            }
        }

        override fun onFailure(message: String) {
            e(TAG, "Device registration failure: $message")
            if(displayDeviceRegistrationErrorToUser) {
                appRegistrationCallback?.onError(YuvalLoveNotesApp.context.getString(R.string.error_default_registration_failure))
            }
        }
    }

    fun requestLoveLettersFromServer(displayRequestLoveLettersErrorToUser: Boolean = true) {
        this.displayRequestLoveLettersErrorToUser = displayRequestLoveLettersErrorToUser
        val isLettersFetchingCompleted = sharedPrefs.getBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_default_letters_downloaded), false)

        if (!isLettersFetchingCompleted) {
            d(TAG, "Requesting initial database download")
            loveItemsRepository.fetchLettersFromServer(getUserFromSharedPrefsData(), inferLanguageFromLocale(), 0, downloadDefaultLettersCallback)
        }
    }

    private fun allRegistrationProcessesFinished(): Boolean {
        val context = YuvalLoveNotesApp.context
        val isUserRegistered = sharedPrefs.getBoolean(context.getString(R.string.pref_key_user_registered_in_server), false)
        val isDeviceRegistered = sharedPrefs.getBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), false)
        val dataBaseDownloaded = sharedPrefs.getBoolean(context.getString(R.string.pref_key_default_letters_downloaded), false)

        return isUserRegistered && isDeviceRegistered && dataBaseDownloaded
    }

    private fun isPhoneNumberValid(phone: LoveLettersUser.Phone, callbackApp: AppRegistrationCallback): Boolean {
        val defaultError = YuvalLoveNotesApp.context.getString(R.string.error_invalid_lover_number_inserted)
        if (PhoneNumberUtil.getInstance().isPhoneNumberValid(phone.regionNumber, phone.localNumber)) {
            return true
        }
        callbackApp.onError(defaultError)
        return false
    }

    private fun allFieldsHaveText(user: LoveLettersUser, callbackApp: AppRegistrationCallback): Boolean {
        val context = YuvalLoveNotesApp.context

        if (user.userName.isBlank()) {
            callbackApp.onError(context.getString(R.string.error_no_user_name_inserted))
            return false
        }

        if (user.loverNickName.isBlank()) {
            callbackApp.onError(context.getString(R.string.error_no_lover_nickname_inserted))
            return false
        }

        if (user.loverPhone.isBlank()) {
            callbackApp.onError(context.getString(R.string.error_invalid_lover_number_inserted))
            return false
        }

        return true
    }

    fun requestUserPhoneNumber(onCompletion: (regionNumber: String, localNumber: String) -> Unit) {
        val context = YuvalLoveNotesApp.context
        val defaultRegion = PhoneNumberUtil.getInstance().getDeviceDefaultCountryCode()
        val region = sharedPrefs.getString(context.getString(R.string.pref_key_lover_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val local = sharedPrefs.getString(context.getString(R.string.pref_key_lover_local_phone_number), "")!!
        onCompletion.invoke(region, local)
    }


    private fun saveUserDataToPrefs(user: LoveLettersUser) {
        d(TAG, "Saving user data to shared prefs: $user") //todo: remove user details logging
        val context = YuvalLoveNotesApp.context
        sharedPrefs.edit {
            putString(context.getString(R.string.pref_key_user_random_identifier), user.randomIdentifier)
            putString(context.getString(R.string.pref_key_user_name), user.userName)
            putString(context.getString(R.string.pref_key_user_gender), user.userGender.name)
            putString(context.getString(R.string.pref_key_user_phone_region_number), user.userPhone.regionNumber)
            putString(context.getString(R.string.pref_key_user_local_phone_number), user.userPhone.localNumber)
            putString(context.getString(R.string.pref_key_user_full_target_phone_number), user.userPhone.fullNumber)
            putString(context.getString(R.string.pref_key_lover_nickname), user.loverNickName)
            putString(context.getString(R.string.pref_key_lover_gender), user.loverGender.name)
            putString(context.getString(R.string.pref_key_lover_phone_region_number), user.loverPhone.regionNumber)
            putString(context.getString(R.string.pref_key_lover_local_phone_number), user.loverPhone.localNumber)
            putString(context.getString(R.string.pref_key_lover_full_target_phone_number), user.loverPhone.fullNumber)
        }
    }

    @Suppress("UnnecessaryVariable")
    fun getUserFromSharedPrefsData(): UnRegisteredLoveLettersUser {
        val context = YuvalLoveNotesApp.context

        val userName = sharedPrefs.getString(context.getString(R.string.pref_key_user_name).takeUnless { it.isBlank() }, "")!!
        val userGender = Gender.valueOf(sharedPrefs.getString(context.getString(R.string.pref_key_user_gender).takeUnless { it.isBlank() }, "MAN")!!)

        val loverNickName = sharedPrefs.getString(context.getString(R.string.pref_key_lover_nickname).takeUnless { it.isBlank() }, "")!!
        val loverGender = Gender.valueOf(sharedPrefs.getString(context.getString(R.string.pref_key_lover_gender).takeUnless { it.isBlank() }, "WOMAN")!!)

        val defaultRegion = PhoneNumberUtil.getInstance().getDeviceDefaultCountryCode()
        val loverRegionNumber = sharedPrefs.getString(context.getString(R.string.pref_key_lover_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val loverLocalNumber = sharedPrefs.getString(context.getString(R.string.pref_key_lover_local_phone_number), "")!!
        val loverPhone = LoveLettersUser.Phone(loverRegionNumber, loverLocalNumber)

        val userRegionNumber = sharedPrefs.getString(context.getString(R.string.pref_key_user_phone_region_number).takeUnless { it.isBlank() }, defaultRegion)!!
        val userLocalNumber = sharedPrefs.getString(context.getString(R.string.pref_key_user_local_phone_number), "")!!
        val userPhone = LoveLettersUser.Phone(userRegionNumber, userLocalNumber)

        val result = UnRegisteredLoveLettersUser(userName, userGender, loverNickName, loverGender, userPhone, loverPhone)
        return result
    }

    //todo: Logic is currently in the fragment livedata for informing the results
    fun saveLoverNickname(loverNickName: String): Boolean {
        return if (loverNickName.isNotBlank()) {
            this.loverNickname = loverNickName
            true
        } else {
            w(TAG, "Empty lover nickname inserted")
            false
        }
    }

    //todo: validate input and use livedata (logic currently in the caller)
    fun onUserNameSubmitted(name: String) {
        this.userName = name
    }

}
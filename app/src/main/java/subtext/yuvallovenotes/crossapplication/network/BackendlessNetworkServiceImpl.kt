package subtext.yuvallovenotes.crossapplication.network

import android.util.Log.*
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.backendless.push.DeviceRegistrationResult
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.localization.inferLanguageFromLocale
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.users.UnRegisteredLoveLettersUser


object BackendlessNetworkServiceImpl : UserRegistrationNetworkService, LoveLettersNetworkService {

    private const val DEFAULT_PAGE_SIZE: Int = 100
    private val TAG: String? = BackendlessNetworkServiceImpl::class.simpleName

    /**
     * Register the user to the system. If the user already exists, the process succeeds automatically.
     * @param user The user to be registered
     * @param callback A callback to for notify the caller about the operation status
     */
    override fun registerUser(user: UnRegisteredLoveLettersUser, callback: NetworkCallback<LoveLettersUser>) {

        Backendless.UserService.register(user.toBackendlessUser(), object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(response: BackendlessUser?) {
                if (response != null) {
                    d(TAG, "Successful user registration.")
                    callback.onSuccess(LoveLettersUser(response))
                } else {
                    e(TAG, "Error in backendless user registration. Null user retrieved from server")
                    val networkError = YuvalLoveNotesApp.context.getString(R.string.error_login_default_network_failure)
                    callback.onFailure(networkError)
                }
            }

            override fun handleFault(fault: BackendlessFault?) {
                e(TAG, "Error in backendless user registration: $fault")
                val networkError = YuvalLoveNotesApp.context.getString(R.string.error_login_default_network_failure)
                callback.onFailure(networkError)
            }
        })
    }

    override fun registerDeviceToPushNotificationsService(channels: List<String>?, callback: NetworkCallback<DeviceRegistrationResult>?) {

        var finalChannels = mutableListOf<String>()

        if (channels.isNullOrEmpty()) {
            finalChannels.add("default")
        } else {
            finalChannels = channels.toMutableList()
        }

        Backendless.Messaging.registerDevice(finalChannels, object : AsyncCallback<DeviceRegistrationResult?> {
            override fun handleResponse(response: DeviceRegistrationResult?) {
                d(TAG, "Successfully registered device to push notifications service")
                if (response != null) {
                    callback?.onSuccess(response)
                } else {
                    e(TAG, "Error while registering device to push notifications service: operation succeeded with null callback")
                    val networkError = YuvalLoveNotesApp.context.getString(R.string.error_login_default_network_failure)
                    callback?.onFailure(networkError)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                e(TAG, "Error while registering device to push notifications service: $fault")
                //  val networkError = YuvalLoveNotesApp.context.getString(R.string.error_login_default_network_failure)
                //  callback?.onFailure(networkError)
            }
        })
    }

    /**
     * Actual implementation of Backendless data fetching api for getting Love letters
     */
    private fun fetchRandomLoveLetters(tableObjectCount: Int, language: Language, callback: NetworkCallback<MutableList<LoveLetter>>) {
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = "language = ${language.fieldName}"
        queryBuilder.setPageSize(DEFAULT_PAGE_SIZE)
        var possibleOffset = 0
        if (tableObjectCount > 100) {
            possibleOffset = tableObjectCount - 100
        }
        queryBuilder.setOffset((0..possibleOffset).random())
        Backendless.Data.of(LoveLetter::class.java).find(queryBuilder, object : AsyncCallback<MutableList<LoveLetter>> {

            override fun handleResponse(response: MutableList<LoveLetter>?) {
                i(TAG, "Love letters fetched")
                if (response.isNullOrEmpty()) {
                    if (language != Language.ENGLISH) {
                        fetchRandomLoveLetters(tableObjectCount, Language.ENGLISH, callback)
                    }
                } else {
                    callback.onSuccess(response)
                }
            }

            override fun handleFault(backendlessFault: BackendlessFault) {
                i(TAG, "Love letters fetch error: ${backendlessFault.message}")
            }
        })
    }

    /**
     * Returns 100 random letters of a specified language from Backendless database or all letters if there are less than 100.
     */
    override fun requestRandomLoveLetters(callback: NetworkCallback<MutableList<LoveLetter>>) {
        fetchLoveLetterTableObjectCount { count ->
            fetchRandomLoveLetters(count, inferLanguageFromLocale(), callback)
        }
    }

    private fun fetchLoveLetterTableObjectCount(onSuccess: (count: Int) -> Unit) {
        Backendless.Data.of(LoveLetter::class.java).getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int) {
                i(TAG, "total objects in the LoveLetter table - $count")
                onSuccess.invoke(count)
            }

            override fun handleFault(backendlessFault: BackendlessFault) {
                i(TAG, "table object count fetch error: ${backendlessFault.message}")
            }
        })
    }
}

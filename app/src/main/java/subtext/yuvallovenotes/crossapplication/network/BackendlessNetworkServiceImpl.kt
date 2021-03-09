package subtext.yuvallovenotes.crossapplication.network

import android.content.SharedPreferences
import android.util.Log.*
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.backendless.push.DeviceRegistrationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.users.UnRegisteredLoveLettersUser


object BackendlessNetworkServiceImpl : UserRegistrationNetworkService, LoveLettersNetworkService {

    private const val DEFAULT_PAGE_SIZE: Int = 100
    private val TAG: String? = BackendlessNetworkServiceImpl::class.simpleName
    private val prefs: SharedPreferences = get(SharedPreferences::class.java)

    /**
     * Register the user to the system. If the user already exists, the process succeeds automatically.
     * @param user The user to be registered
     * @param callback A callback for notify the caller about the operation status
     */
    override fun registerUser(user: UnRegisteredLoveLettersUser, callback: NetworkCallback<LoveLettersUser>) {
        d(TAG, "Registering user")
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
        d(TAG, "Registering device")
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
     * Making a network call for fetching all [LoveLetter] objects found in Backendless server. Results or error
     * are returned with a designated [NetworkCallback]
     *
     * @param language The language of the required letters
     * @param offset Integer of the first index in the database where the search will begin from
     * @param callback A callback to be invoked with list of results or an error message
     */
    override fun fetchLetters(language: Language, offset: Int, callback: NetworkCallback<MutableList<LoveLetter>>) {
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = "language = '${language.tableFieldName}'"
        queryBuilder.setPageSize(DEFAULT_PAGE_SIZE)
        queryBuilder.setOffset(offset)
        d(TAG, "Executing love letters fetch request")
        Backendless.Data.of(LoveLetter::class.java).find(queryBuilder, object : AsyncCallback<MutableList<LoveLetter>> {

            override fun handleResponse(response: MutableList<LoveLetter>?) {
                d(TAG, "love letters response: $response")
                if (!response.isNullOrEmpty()) {
                    i(TAG, "${response.size} Love letters fetched from backendless server")
                    callback.onSuccess(response)
                    if (response.size == DEFAULT_PAGE_SIZE) {
                        //Get next 100 items
                        fetchLetters(language, offset + 100, callback)
                    } else {
                        prefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_default_letters_downloaded), true).apply()
                    }
                } else {
                    if (language != Language.ENGLISH) {
                        fetchLetters(Language.ENGLISH, 0, callback)
                    }
                }
            }

            override fun handleFault(backendlessFault: BackendlessFault) {
                e(TAG, "Letters fetch request failure: ${backendlessFault}")
                callback.onFailure(backendlessFault.toString())
            }
        })
    }

    override fun uploadLetters(letters: List<LoveLetter>) {
        GlobalScope.launch(Dispatchers.IO) {
            Backendless.Data.of(LoveLetter::class.java).create(letters, object : AsyncCallback<List<String>?> {

                override fun handleResponse(response: List<String>?) {
                    i(TAG, "Objects have been saved")
                }

                override fun handleFault(fault: BackendlessFault) {
                    i(TAG, "Server reported an error $fault")
                }

            })
        }
    }
}

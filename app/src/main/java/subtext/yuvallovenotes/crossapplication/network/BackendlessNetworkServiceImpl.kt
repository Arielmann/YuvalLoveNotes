package subtext.yuvallovenotes.crossapplication.network

import android.util.Log.*
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.push.DeviceRegistrationResult
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.UnRegisteredLoveLettersUser


object BackendlessNetworkServiceImpl : LoveLettersNetworkService {

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
}

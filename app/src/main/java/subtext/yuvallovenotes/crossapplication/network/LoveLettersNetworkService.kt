package subtext.yuvallovenotes.crossapplication.network

import com.backendless.push.DeviceRegistrationResult
import subtext.yuvallovenotes.crossapplication.models.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.UnRegisteredLoveLettersUser

interface LoveLettersNetworkService {

    fun registerUser(user: UnRegisteredLoveLettersUser, callback: NetworkCallback<LoveLettersUser>)
    fun registerDeviceToPushNotificationsService(channels: List<String>?, callback: NetworkCallback<DeviceRegistrationResult>?)
}

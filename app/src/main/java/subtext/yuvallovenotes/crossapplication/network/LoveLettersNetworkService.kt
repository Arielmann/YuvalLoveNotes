package subtext.yuvallovenotes.crossapplication.network

import com.backendless.push.DeviceRegistrationResult
import subtext.yuvallovenotes.crossapplication.models.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.UnVerifiedLoveLettersUser

interface LoveLettersNetworkService {

    fun registerUser(user: UnVerifiedLoveLettersUser, callback: NetworkCallback<LoveLettersUser>)
    fun registerDeviceToPushNotificationsService(channels: List<String>?, callback: NetworkCallback<DeviceRegistrationResult>?)
}

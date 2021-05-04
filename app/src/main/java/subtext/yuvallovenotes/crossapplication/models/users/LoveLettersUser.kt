package subtext.yuvallovenotes.crossapplication.models.users

import com.backendless.BackendlessUser
import java.util.*

open class LoveLettersUser() {

    companion object {
        private val TAG: String = LoveLettersUser::class.simpleName!!
    }

    constructor(name: String, userGender: Gender, loverNickname: String, loverGender: Gender, userPhone: Phone, loverPhone: Phone) : this() {
        this.userName = name
        this.userGender = userGender
        this.loverNickName = loverNickname
        this.loverGender = loverGender
        this.randomIdentifier = UUID.randomUUID().toString()
        this.loverPhone = loverPhone
        this.userPhone = userPhone
    }

    /**
     * Constructs a new user from a backendless user
     */
    constructor(response: BackendlessUser) : this() {
        this.userName = response.properties[PropertyKey.USER_NAME.tablePropertyKey] as String
        this.userGender = Gender.valueOf(response.properties[PropertyKey.USER_GENDER.tablePropertyKey] as String)
        this.loverNickName = response.properties[PropertyKey.LOVER_NICK_NAME.tablePropertyKey] as String
        this.loverGender = Gender.valueOf(response.properties[PropertyKey.USER_GENDER.tablePropertyKey] as String)
        this.randomIdentifier = response.properties[PropertyKey.RANDOM_IDENTIFIER.tablePropertyKey] as String
        val loverPhoneRegionNumber = response.properties[PropertyKey.LOVER_PHONE_REGION_NUMBER.tablePropertyKey] as String
        val loverPhoneLocalNumber = response.properties[PropertyKey.LOVER_PHONE_LOCAL_NUMBER.tablePropertyKey] as String
        this.loverPhone = Phone(loverPhoneRegionNumber, loverPhoneLocalNumber)
    }

    var randomIdentifier: String? = null
        set(value) {
            if (value != null) {
                properties[PropertyKey.RANDOM_IDENTIFIER] = value
            }
            field = value
        }

    /** The properties for this class are associated with a properties map.
     * This is used with a user object defined in third party library, such as backendless, that might be using maps to transmit its own properties.
     */
    val properties: MutableMap<PropertyKey, Any> = mutableMapOf()

    var userName: String = ""
        set(value) {
            properties[PropertyKey.USER_NAME] = value
            field = value
        }

    var userGender: Gender = Gender.MAN
        set(value) {
            properties[PropertyKey.USER_GENDER] = value
            field = value
        }

    var loverNickName: String = ""
        set(value) {
            properties[PropertyKey.LOVER_NICK_NAME] = value
            field = value
        }

    var loverGender: Gender = Gender.WOMAN
        set(value) {
            properties[PropertyKey.LOVER_GENDER] = value
            field = value
        }

    var loverPhone: Phone = Phone("0", "0")
        set(value) {
            properties[PropertyKey.LOVER_PHONE_REGION_NUMBER] = value.regionNumber
            properties[PropertyKey.LOVER_PHONE_LOCAL_NUMBER] = value.localNumber
            properties[PropertyKey.LOVER_PHONE_FULL_NUMBER] = value.fullNumber
            field = value
        }

    var userPhone: Phone = Phone("0", "0")
        set(value) {
            properties[PropertyKey.USER_PHONE_REGION_NUMBER] = value.regionNumber
            properties[PropertyKey.USER_PHONE_LOCAL_NUMBER] = value.localNumber
            properties[PropertyKey.USER_PHONE_FULL_NUMBER] = value.fullNumber
            field = value
        }

    data class Phone(val regionNumber: String, val localNumber: String) {
        val fullNumber = regionNumber + localNumber

        fun isNotBlank(): Boolean {
            return fullNumber.isNotBlank()
        }

        fun isBlank(): Boolean {
            return fullNumber.isBlank()
        }
    }

    /**
     * Creates a backendless user object based on the data provided by the [LoveLettersUser]
     */
    fun toBackendlessUser(): BackendlessUser {
        val backendlessUser = BackendlessUser()
        backendlessUser.password = userName
        for ((key, value) in properties) {
            backendlessUser.setProperty(key.tablePropertyKey, value)
        }
        return backendlessUser
    }

    enum class PropertyKey(val tablePropertyKey: String) {
        USER_NAME("userName"),
        USER_GENDER("userGender"),
        USER_PHONE_REGION_NUMBER("userPhoneRegionNumber"),
        USER_PHONE_LOCAL_NUMBER("userPhoneLocalNumber"),
        USER_PHONE_FULL_NUMBER("userPhoneFullNumber"),
        RANDOM_IDENTIFIER("randomIdentifier"),
        LOVER_NICK_NAME("loverNickname"),
        LOVER_GENDER("loverGender"),
        LOVER_PHONE_REGION_NUMBER("loverPhoneRegionNumber"),
        LOVER_PHONE_LOCAL_NUMBER("loverPhoneLocalNumber"),
        LOVER_PHONE_FULL_NUMBER("loverPhoneFullNumber"),
    }
}

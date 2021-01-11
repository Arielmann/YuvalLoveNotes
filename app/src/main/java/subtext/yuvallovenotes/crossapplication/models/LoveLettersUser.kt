package subtext.yuvallovenotes.crossapplication.models

import com.backendless.BackendlessUser
import java.util.*

open class LoveLettersUser() {

    companion object {
        const val DUMMY_EMAIL: String = "dummy@dummy.com"
        private val TAG: String = LoveLettersUser::class.simpleName!!
    }

    constructor(name: String, loverNickname: String, email: String = "", phone: Phone) : this() {
        this.userName = name
        this.loverNickName = loverNickname
        this.randomIdentifier = UUID.randomUUID().toString()
        this.email = email
        this.loverPhone = phone
    }

    /**
     * Constructs a new user from a backendless user
     */
    constructor(response: BackendlessUser) : this() {
        this.userName = response.properties[PropertyKey.USER_NAME.tablePropertyKey] as String
        this.loverNickName = response.properties[PropertyKey.LOVER_NICK_NAME.tablePropertyKey] as String
        this.email = response.properties[PropertyKey.EMAIL.tablePropertyKey] as String
        this.randomIdentifier = response.properties[PropertyKey.RANDOM_IDENTIFIER.tablePropertyKey] as String
        val loverPhoneRegionNumber = response.properties[PropertyKey.PHONE_REGION_NUMBER.tablePropertyKey] as String
        val loverPhoneLocalNumber = response.properties[PropertyKey.PHONE_LOCAL_NUMBER.tablePropertyKey] as String
        this.loverPhone = Phone(loverPhoneRegionNumber, loverPhoneLocalNumber)
    }

    private var randomIdentifier: String? = null
        set(value) {
            if(value != null) {
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

    var loverNickName: String = ""
        set(value) {
            properties[PropertyKey.LOVER_NICK_NAME] = value
            field = value
        }

    var email: String = DUMMY_EMAIL
        set(value) {
            properties[PropertyKey.EMAIL] = value
            field = value
        }

    var loverPhone: Phone = Phone("0", "0")
        set(value) {
            properties[PropertyKey.PHONE_REGION_NUMBER] = value.regionNumber
            properties[PropertyKey.PHONE_LOCAL_NUMBER] = value.localNumber
            properties[PropertyKey.PHONE_FULL_NUMBER] = value.fullNumber
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
        EMAIL("email"),
        RANDOM_IDENTIFIER("randomIdentifier"),
        LOVER_NICK_NAME("loverNickname"),
        PHONE_REGION_NUMBER("loverPhoneRegionNumber"),
        PHONE_LOCAL_NUMBER("loverPhoneLocalNumber"),
        PHONE_FULL_NUMBER("loverPhoneFullNumber"),
    }
}

package subtext.yuvallovenotes.crossapplication.models

import com.backendless.BackendlessUser

open class LoveLettersUser() {

    companion object {
        const val DUMMY_EMAIL: String = "dummy@dummy.com"
        private val TAG: String = LoveLettersUser::class.simpleName!!
    }

    constructor(name: String, loverNickname: String, email: String = "", phone: Phone) : this() {
        this.userName = name
        this.loverNickName = loverNickname
        this.email = email
        this.phone = phone
    }

    /**
     * Constructs a new user from a backendless user
     */
    constructor(response: BackendlessUser) : this() {
        this.objectId = response.properties[PropertyKey.OBJECT_ID.tablePropertyKey] as String
        this.userName = response.properties[PropertyKey.USER_NAME.tablePropertyKey] as String
        this.loverNickName = response.properties[PropertyKey.LOVER_NICK_NAME.tablePropertyKey] as String
        val phoneRegionNumber = response.properties[PropertyKey.PHONE_LOCAL_NUMBER.tablePropertyKey] as String
        val phoneLocalNumber = response.properties[PropertyKey.PHONE_LOCAL_NUMBER.tablePropertyKey] as String
        this.phone = Phone(phoneRegionNumber, phoneLocalNumber)
    }

    var objectId: String? = null
        set(value) {
            if (value != null) {
                properties[PropertyKey.OBJECT_ID] = value
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

    var phone: Phone = Phone("0", "0")
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
        OBJECT_ID("objectId"),
        USER_NAME("userName"),
        EMAIL("email"),
        LOVER_NICK_NAME("loverNickName"),
        PHONE_REGION_NUMBER("phoneRegionNumber"),
        PHONE_LOCAL_NUMBER("phoneLocalNumber"),
        PHONE_FULL_NUMBER("phoneFullNumber"),
    }
}

package subtext.yuvallovenotes.crossapplication.models.users

/**
 * Class for defining a user of the Awareness app.
 */
//Todo: check if can create as class members outside of constructor
class UnRegisteredLoveLettersUser(userName: String,
        userGender: Gender,
        loverNickName: String,
        loverGender: Gender,
        userPhone: Phone,
        loverPhone: Phone) : LoveLettersUser(userName, userGender, loverNickName, loverGender, userPhone, loverPhone) {

    companion object {
        private val TAG: String = UnRegisteredLoveLettersUser::class.simpleName!!
    }
}

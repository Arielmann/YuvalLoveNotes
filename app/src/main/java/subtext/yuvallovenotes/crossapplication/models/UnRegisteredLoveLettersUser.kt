package subtext.yuvallovenotes.crossapplication.models

/**
 * Class for defining a user of the Awareness app.
 */
class UnRegisteredLoveLettersUser(userName: String, loverNickName: String, userPhone: Phone, loverPhone: Phone) : LoveLettersUser(userName, loverNickName, userPhone, loverPhone) {

    companion object {
        private val TAG: String = UnRegisteredLoveLettersUser::class.simpleName!!
    }
}

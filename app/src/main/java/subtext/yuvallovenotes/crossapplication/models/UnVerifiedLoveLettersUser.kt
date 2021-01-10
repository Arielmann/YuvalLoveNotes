package subtext.yuvallovenotes.crossapplication.models

/**
 * Class for defining a user of the Awareness app.
 */
class UnVerifiedLoveLettersUser(name: String, loverNickName: String, phone: Phone) : LoveLettersUser(name, loverNickName, DUMMY_EMAIL, phone) {

    companion object {
        private val TAG: String = UnVerifiedLoveLettersUser::class.simpleName!!
    }
}

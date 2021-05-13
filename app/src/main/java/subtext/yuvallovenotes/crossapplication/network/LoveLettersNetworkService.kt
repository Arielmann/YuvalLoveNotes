package subtext.yuvallovenotes.crossapplication.network

import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.models.users.UnRegisteredLoveLettersUser

interface LoveLettersNetworkService {

    fun fetchLetters(user: LoveLettersUser, language: Language, pagingCall: Boolean, offset : Int = 0, callback: NetworkCallback<MutableList<LoveLetter>>)
    fun uploadLetters(letters: List<LoveLetter>)

}

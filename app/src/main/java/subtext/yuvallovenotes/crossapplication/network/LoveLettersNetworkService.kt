package subtext.yuvallovenotes.crossapplication.network

import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter

interface LoveLettersNetworkService {

    fun requestLoveLetters(language: Language, offset : Int = 0, callback: NetworkCallback<MutableList<LoveLetter>>)
}

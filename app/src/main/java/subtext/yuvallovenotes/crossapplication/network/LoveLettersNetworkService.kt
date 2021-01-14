package subtext.yuvallovenotes.crossapplication.network

import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter

interface LoveLettersNetworkService {

    fun requestRandomLoveLetters(callback: NetworkCallback<MutableList<LoveLetter>>)
}

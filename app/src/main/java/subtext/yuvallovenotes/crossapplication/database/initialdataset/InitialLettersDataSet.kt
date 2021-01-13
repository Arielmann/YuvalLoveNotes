package subtext.yuvallovenotes.crossapplication.database.initialdataset

import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveClosure
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveOpener
import subtext.yuvallovenotes.crossapplication.models.loveitems.LovePhrase

interface InitialLettersDataSet {

    fun getOpeners() : MutableList<LoveOpener>
    fun getPhrases() : MutableList<LovePhrase>
    fun getClosures() : MutableList<LoveClosure>

}

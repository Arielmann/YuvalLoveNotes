package subtext.yuvallovenotes.crossapplication.database.initialdataset

import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter

interface InitialLettersDataSet {

    fun getLetters(): MutableList<LoveLetter>
}

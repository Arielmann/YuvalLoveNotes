package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import subtext.yuvallovenotes.backendless.LoveNotesBackendless
import subtext.yuvallovenotes.loveletters.LoveItem

class PageViewModel : ViewModel() {

    lateinit var loveNotesBackendless : LoveNotesBackendless
    var loveItems : MutableList<LoveItem> = mutableListOf()

    fun setLoveNotesBackendless(context: Context?) {
        loveNotesBackendless = LoveNotesBackendless(context)
    }
}
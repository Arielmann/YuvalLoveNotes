package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModel
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.backendless.LoveNotesBackendless
import subtext.yuvallovenotes.loveletters.LoveItem

class PageViewModel : ViewModel() {

    lateinit var loveNotesBackendless : LoveNotesBackendless
    var loveItems : MutableList<LoveItem> = mutableListOf()

    val onButtonsTouchListener: View.OnTouchListener = View.OnTouchListener { view: View, motionEvent: MotionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
//                println("${YuvalLoveNotesApp.LOG_TAG} Action Down")
                view.alpha = 0.5f
            }
            MotionEvent.ACTION_UP -> {
//                println("${YuvalLoveNotesApp.LOG_TAG} Action Up")
                view.alpha = 1f
            }

            MotionEvent.ACTION_CANCEL -> {
                view.alpha = 1f
            }
        }
        false
    }

    fun setLoveNotesBackendless(context: Context?) {
        loveNotesBackendless = LoveNotesBackendless(context)
    }
}
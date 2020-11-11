package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModel
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.backendless.LoveNotesBackendless
import subtext.yuvallovenotes.loveletters.LoveItem

class LoveViewModel(application: YuvalLoveNotesApp) : ViewModel() {

    var loveNotesBackendless: LoveNotesBackendless = LoveNotesBackendless(application)
    var loveItems: MutableList<LoveItem> = mutableListOf()

    @SuppressLint("ClickableViewAccessibility")
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
}
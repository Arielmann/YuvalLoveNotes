package subtext.yuvallovenotes.crossapplication.logic.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d

class AwarenessAlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = AwarenessAlarmReceiver::class.simpleName!!
    }

    override fun onReceive(context: Context, intent: Intent) {
        d(TAG, "Alarm received. action: ".plus(intent.action))
    }
}
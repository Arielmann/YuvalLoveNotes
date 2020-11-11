package subtext.yuvallovenotes.crossapplication.logic.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d
import java.util.*

class AwarenessBootReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = AwarenessBootReceiver::class.simpleName!!
    }

    override fun onReceive(context: Context, intent: Intent) {
        d(TAG, "Boot received. action: ".plus(intent.action))
      /*  AwarenessAlarm.values().forEach { alarm ->
            if (alarm.isActive) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = alarm.triggerTime
                Utils.setAlarmAndCancelAllPreviousWithSameData(context, Utils.getDailyAlarmTimeMilliseconds(calendar), AwarenessPendingIntentData.VIBRATION_SERVICE_AUTO_RESTART)
            }
        }*/
    }
}
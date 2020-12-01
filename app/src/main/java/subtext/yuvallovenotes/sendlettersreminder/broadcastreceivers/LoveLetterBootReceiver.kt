package subtext.yuvallovenotes.sendlettersreminder.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d
import subtext.yuvallovenotes.sendlettersreminder.LoveLetterAlarm
import java.util.*

class LoveLetterBootReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = LoveLetterBootReceiver::class.simpleName!!
    }

    override fun onReceive(context: Context, intent: Intent) {
        d(TAG, "Boot received. action: ".plus(intent.action))
        LoveLetterAlarm.values().forEach { alarm ->
            if (alarm.isActive) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = alarm.triggerTime
                if (!alarm.isTriggerTimeExpired()) {
                    alarm.setAlarmAndCancelAllPreviousWithSameData(context, calendar)
                } else {
                    d(TAG, "trigger time expired for ${alarm.name}. Retroactive alarm trigger is activated".plus(intent.action))
                    val currentTimeCal = Calendar.getInstance()
                    currentTimeCal.timeInMillis = currentTimeCal.timeInMillis + 1000
                    alarm.setAlarmAndCancelAllPreviousWithSameData(context, currentTimeCal)
                }
            }
        }
    }
}
package subtext.yuvallovenotes.crossapplication.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d
import subtext.yuvallovenotes.crossapplication.alarms.LoveLetterAlarm
import subtext.yuvallovenotes.crossapplication.notifications.LoveLetterNotificationManager
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
                    d(TAG, "resetting alarmed named ${alarm.name}")
                    alarm.setAlarmAndCancelAllPreviousWithSameData(context, calendar)
                } else {
                    d(TAG, "trigger time expired for ${alarm.name}. Retroactive alarm trigger is activated")
                    if (LoveLetterNotificationManager.isShowNotificationAllowed()) {
                        d(TAG, "Displaying notification with the expired date and time")
                        LoveLetterNotificationManager.displaySendLetterReminderNotification(context)
                    }
                    val currentTimeCal = Calendar.getInstance()
                    currentTimeCal.timeInMillis = currentTimeCal.timeInMillis + 1000
                    alarm.setAlarmAndCancelAllPreviousWithSameData(context, currentTimeCal)
                }
            } else {
                d(TAG, "Alarmed named ${alarm.name} is inactive")
            }
        }
    }
}
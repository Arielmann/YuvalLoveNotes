package subtext.yuvallovenotes.crossapplication.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log.d
import androidx.preference.PreferenceManager
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.alarms.LoveLetterAlarm
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.notifications.LoveLetterNotificationManager


class LoveLettersAlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = LoveLettersAlarmReceiver::class.simpleName!!
    }

    override fun onReceive(context: Context, intent: Intent) {

        d(TAG, "Alarm received. action: ".plus(intent.action))

        when (intent.action) {

            LoveLetterAlarm.SEND_LETTER_REMINDER.name -> {
                val sendReminderAlarm = LoveLetterAlarm.SEND_LETTER_REMINDER
//                val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                if(LoveLetterNotificationManager.isShowNotificationAllowed()) {
                    LoveLetterNotificationManager.displaySendLetterReminderNotification(context)
                }
                sendReminderAlarm.setAlarmAndCancelAllPreviousWithSameData(context, sendReminderAlarm.getDefaultActivationCalendar())
            }
        }
    }
}
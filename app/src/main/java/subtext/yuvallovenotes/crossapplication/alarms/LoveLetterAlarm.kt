package subtext.yuvallovenotes.crossapplication.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.util.Log.d
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.broadcastreceivers.LoveLettersAlarmReceiver
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import java.util.*

/**
 * An active enum used for defining and setting different alarms required by the Love Letter app.
 * Client can access the details of different alarms required by the app, and activate or cancel them.
 */
enum class LoveLetterAlarm(private val isActiveKey: String, protected val triggerTimeKey: String, private val requestCode: Int) : DefaultActivationTime {

    SEND_LETTER_REMINDER(YuvalLoveNotesApp.context.getString(R.string.pref_key_is_feature_active_send_letter_reminder),
            YuvalLoveNotesApp.context.getString(R.string.pref_key_trigger_time_send_letter_reminder), 1020) {
        override fun getDefaultActivationCalendar(): Calendar {

            val defaultCal = defaultTriggerTime(YuvalLoveNotesApp.context.resources)
            if (defaultCal.timeInMillis < System.currentTimeMillis()) {
                defaultCal.add(Calendar.DAY_OF_YEAR, 7)
                d(TAG, "in $name, the requested alarm trigger time is earlier than current time. Setting it for the same hour a week forward")
            }
            return defaultCal
//            return dummyCalendar(YuvalLoveNotesApp.context.resources)
        }
    };

    companion object {
        private val TAG = LoveLetterAlarm::class.simpleName

        /**
         * Returns the default trigger time for all alarms
         * The default trigger time may change according to device locale
         */
        private fun defaultTriggerTime(res: Resources): Calendar {
            val calendar = Calendar.getInstance()
            val hourOfDay = res.getInteger(R.integer.default_alarm_clock_hour_of_day)
            val minute = res.getInteger(R.integer.default_alarm_clock_minute)
            var dayOfWeek = Calendar.FRIDAY

            //Set alarm day for thursday in Israel
            if (LoveUtils.getDeviceCountryCode() == YuvalLoveNotesApp.context.getString(R.string.default_country_code)) {
                dayOfWeek = Calendar.THURSDAY
            }

            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 22)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

        private fun dummyCalendar(res: Resources): Calendar {
            val calendar = Calendar.getInstance()
            val hourOfDay = res.getInteger(R.integer.default_alarm_clock_hour_of_day)
            val minute = res.getInteger(R.integer.default_alarm_clock_minute)
            calendar.set(Calendar.HOUR_OF_DAY, 17)
            calendar.set(Calendar.MINUTE, 41)
            calendar.add(Calendar.MINUTE, 1)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
//            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 15)
            d(TAG, "setting dummy calendar")
//            calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

    }

    private val sharedPrefs: SharedPreferences = get(SharedPreferences::class.java)

    /**
     * true if the alarm is currently active. Default value is true
     */
    var isActive
        get() = sharedPrefs.getBoolean(isActiveKey, true)
        set(value) {
            sharedPrefs.edit().putBoolean(isActiveKey, value).apply()
        }

    /**
     * Alarm trigger time read-only property.
     * return -1 if alarm is not set
     */
    val triggerTime: Long
        get() = sharedPrefs.getLong(triggerTimeKey, -1)

    /**
     * Setting the alarm for the specified enum constant.
     * All previous alarm sets for this constant will be canceled.
     * Client may pass a calendar with the required trigger time. If he doesn't, the system
     * will set the trigger time for the default time specified in this app (currently thursday 17:00 p.m)
     */
    fun setAlarmAndCancelAllPreviousWithSameData(context: Context, calendar: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, LoveLettersAlarmReceiver::class.java)
        val pendingIntent = generateAwarenessBroadcastPendingIntent(context, alarmIntent)
        sharedPrefs.edit().putBoolean(isActiveKey, true).apply()
        sharedPrefs.edit().putLong(triggerTimeKey, calendar.timeInMillis).apply()
        cancelAlarmForIntentWithData(context) //If any alarm was set for this specific alarm data, cancel it
        when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent), pendingIntent)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
            }

            else -> {
                alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
            }
        }
        d(TAG, "$name alarm is set to $calendar")
    }

    /**
     * Helper method to check if the current alarm time has passed.
     * Used for checking if alarm should be auto triggered and reset after reboot.
     */
    fun isTriggerTimeExpired(): Boolean {
        return System.currentTimeMillis() > triggerTime
    }

    private fun cancelAlarmForIntentWithData(context: Context) {
        val alarmIntent = Intent(context, LoveLettersAlarmReceiver::class.java)
        val pendingIntent = generateAwarenessBroadcastPendingIntent(context, alarmIntent)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun generateAwarenessBroadcastPendingIntent(context: Context, targetIntent: Intent): PendingIntent? {
        targetIntent.action = name
        return PendingIntent.getBroadcast(context, requestCode, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}

private interface DefaultActivationTime {
    fun getDefaultActivationCalendar(): Calendar
}



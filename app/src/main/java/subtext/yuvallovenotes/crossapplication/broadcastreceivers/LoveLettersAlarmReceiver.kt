package subtext.yuvallovenotes.crossapplication.broadcastreceivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log.d
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import subtext.yuvallovenotes.MainActivity
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.alarms.LoveLetterAlarm


class LoveLettersAlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = LoveLettersAlarmReceiver::class.simpleName!!
        private const val LETTER_REMINDER_CHANNEL_ID = "letterReminderChannelID" //currently constant
        private const val LETTER_REMINDER_CHANNEL_NAME = "letterReminder"
    }

    override fun onReceive(context: Context, intent: Intent) {

        d(TAG, "Alarm received. action: ".plus(intent.action))

        when (intent.action) {

            LoveLetterAlarm.SEND_LETTER_REMINDER.name -> {
                val sendReminderAlarm = LoveLetterAlarm.SEND_LETTER_REMINDER
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                val notificationTitle = context.getString(R.string.notification_title_send_letter_reminder)
                val notificationMessage = context.getString(R.string.notification_message_send_letter_reminder)
                notificationManager.notify(5000, createNotification(context, LETTER_REMINDER_CHANNEL_ID, LETTER_REMINDER_CHANNEL_NAME, notificationTitle, notificationMessage))
                sendReminderAlarm.setAlarmAndCancelAllPreviousWithSameData(context, sendReminderAlarm.getDefaultActivationCalendar())
            }
        }
    }

    private fun createNotification(context: Context, channelID: String, channelName: String, title: String, message: String): Notification {

        d(TAG, "Creating notification for channel $channelName")
        createNotificationChannel(context, channelID, channelName)

        val pendingIntent = NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.main_navigation)
                .setDestination(R.id.letterGeneratorFragment)
                .createPendingIntent()

        return NotificationCompat.Builder(context, channelID)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setColor(Color.WHITE)
                .setSmallIcon(R.drawable.ic_letter_send)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTicker(title)
                .setContentIntent(pendingIntent)
                .build()
    }

    private fun createNotificationChannel(context: Context, channelID: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificationChannel)
        }
    }
}
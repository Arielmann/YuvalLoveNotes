package subtext.yuvallovenotes.crossapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import subtext.yuvallovenotes.MainActivity
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp

object LoveLetterNotificationManager {

    private val TAG: String? = LoveLetterNotificationManager::class.simpleName

    fun displaySendLetterReminderNotification(context: Context) {

        val channelID = "letterReminderChannelID" //currently constant
        val channelName = "letterReminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = context.getString(R.string.notification_title_send_letter_reminder)
        val message = context.getString(R.string.notification_message_send_letter_reminder)

        Log.d(TAG, "Creating notification for channel $channelName")
        createNotificationChannel(context, channelID, channelName)

        val pendingIntent = NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.main_navigation)
                .setDestination(R.id.letterGeneratorFragment)
                .createPendingIntent()

        val notification = NotificationCompat.Builder(context, channelID)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setColor(Color.WHITE)
                .setSmallIcon(R.drawable.ic_baseline_favorite_white_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTicker(title)
                .setContentIntent(pendingIntent)
                .build()

        notificationManager.notify(5000, notification)
    }

    private fun createNotificationChannel(context: Context, channelID: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificationChannel)
        }
    }

    fun isShowNotificationAllowed() : Boolean{
        val context = YuvalLoveNotesApp.context
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getBoolean(context.getString(R.string.pref_key_is_login_process_completed), false)
    }
}
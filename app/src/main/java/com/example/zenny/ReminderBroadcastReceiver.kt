package com.example.zenny

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val openAppIntent = Intent(context, activity_home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, "HYDRATION_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_drop)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Don't forget to drink some water. Stay healthy!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 300, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)


        val prefs = context.getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
        val remindersEnabled = prefs.getBoolean("reminders_enabled", true)

        val interval = intent.getLongExtra("EXTRA_INTERVAL", 0)
        if (interval > 0 && remindersEnabled) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("EXTRA_INTERVAL", interval)
            }


            val pendingIntent = PendingIntent.getBroadcast(
                context, HydrationFragment.Companion.REQUEST_CODE_ALARM, nextIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val triggerTime = System.currentTimeMillis() + interval

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                return
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}

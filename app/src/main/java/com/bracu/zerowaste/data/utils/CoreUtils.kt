package com.bracu.zerowaste.data.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bracu.zerowaste.R
import com.bracu.zerowaste.ui.activities.MainActivity
import java.util.Objects

inline fun <reified T : Activity> Activity.goto(
    block: Intent.() -> Unit = {},
    clearStack: Boolean = false
) {
    if (clearStack) {
        finishAffinity()
    }
    startActivity(Intent(this, T::class.java).apply(block))
}

fun Context.isConnectedToInternet(): Boolean {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}

fun Context.showNotification(title: String, content: String, icon: Int) {
    val soundUri = Uri.parse("android.resource://$packageName/${R.raw.notification_tone}")
    val notificationBuilder = NotificationCompat.Builder(this, "ZeroWasteNotification")
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(icon)
        .setSound(soundUri)
        .setAutoCancel(true)
    val staffNoticeIntent = Intent(this, MainActivity::class.java)
    staffNoticeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    val pendingIntent =
        PendingIntent.getActivity(this, 2024, staffNoticeIntent, PendingIntent.FLAG_IMMUTABLE)
    notificationBuilder.setContentIntent(pendingIntent)
    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationUniqueID = System.currentTimeMillis().toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            "ZeroWasteNotification",
            "ZeroWasteNotification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableLights(false)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        notificationChannel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            audioAttributes
        )
        notificationChannel.setSound(soundUri, audioAttributes)
        Objects.requireNonNull(notificationManager)
            .createNotificationChannel(notificationChannel)
    }
    Objects.requireNonNull(notificationManager)
        .notify(notificationUniqueID, notificationBuilder.build())
}
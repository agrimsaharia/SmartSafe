package com.example.smartsafe.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartsafe.R
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object
    {
        private const val TAG = "MyFirebaseMessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Body: ${it.body}")

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val id = getString(R.string.alerts_notification_channel_id)
            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(this, id)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true)

            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val editor = sharedPref.edit()
        editor.putString(getString(R.string.app_token_key), token)
        editor.apply()

        val arduinoId = sharedPref.getString(getString(R.string.arduino_id_key), null)
        if (arduinoId != null)
        {
            FirebaseDatabase.getInstance().reference.child("arduinoID").child(arduinoId).setValue(token)
        }
    }

}

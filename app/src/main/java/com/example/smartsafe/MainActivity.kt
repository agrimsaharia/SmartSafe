package com.example.smartsafe

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var database: DatabaseReference
    private lateinit var statusTextView: TextView

    private lateinit var mAuth : FirebaseAuth
    private var user : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        if (user == null) launchLoginActivity()

        createNotificationChannel()

        // Get reference to the status TextView
        statusTextView = findViewById(R.id.tv_status)

        // Get reference to the Firebase database
        database = FirebaseDatabase.getInstance().reference

        // Listen for changes in the "status" child
        val statusListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                statusTextView.text = value
                Log.d(TAG, "Status changed to: $value")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "statusListener:onCancelled", databaseError.toException())
            }
        }
        database.child("status").addValueEventListener(statusListener)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout)
        {
            mAuth.signOut()
            launchLoginActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNotificationChannel() {
        // Notification channel is required for Android Oreo and above
        // Create the NotificationChannel.
        val name = getString(R.string.alerts_notification_channel_name)
        val id = getString(R.string.alerts_notification_channel_id)
        val descriptionText = getString(R.string.alerts_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun launchLoginActivity()
    {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

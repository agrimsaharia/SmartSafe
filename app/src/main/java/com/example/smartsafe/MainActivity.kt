package com.example.smartsafe

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var database: DatabaseReference
    private lateinit var statusTextView: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private var arduinoID : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        mAuth = FirebaseAuth.getInstance()
        mAuth.currentUser?.let { user = it } ?: run {
            launchLoginActivity()
        }

        database = FirebaseDatabase.getInstance().reference
        arduinoID = fetchPreference(getString(R.string.arduino_id_key)) ?:
            database.child("Users").child(user.uid).child("ArduinoID").get().toString()

        // TODO: Alert user to update arduinoID if not found and update UI
//        if (arduinoID == "")
//        {
//            updateArduinoIDFirebase()
//        }

        database = database.child("Arduino").child(arduinoID)

        // Get reference to the status TextView
        statusTextView = findViewById(R.id.tv_status)

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

    private fun fetchPreference(key: String) : String? {
        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getString(key, null)
    }

    private fun updateArduinoIDFirebase() {
        database.child("Users").child(user.uid).child("ArduinoID").setValue(arduinoID)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_pin -> {
                launchSetPINDialog()
            }
            R.id.logout -> {
                mAuth.signOut()
                launchLoginActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchSetPINDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pin_set_dialog)

        val inputEditText = dialog.findViewById<EditText>(R.id.et_lock_pin)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel)
        val submitButton = dialog.findViewById<Button>(R.id.submit)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        submitButton.setOnClickListener {
            val lockPIN = inputEditText.text.toString().toInt()
            setLockPIN(lockPIN)
            Log.i(TAG, "received pin : $lockPIN")
            Toast.makeText(baseContext, "PIN Successfully set", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setLockPIN(lockPIN: Int) {
        database.child("PIN").setValue(lockPIN)
    }


    private fun createNotificationChannel() {
        val name = getString(R.string.alerts_notification_channel_name)
        val id = getString(R.string.alerts_notification_channel_id)
        val descriptionText = getString(R.string.alerts_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = descriptionText

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun launchLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}

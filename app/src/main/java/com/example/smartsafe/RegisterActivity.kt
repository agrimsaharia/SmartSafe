package com.example.smartsafe

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    companion object
    {
        private const val TAG = "Register Activity"
    }

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var arduinoIDEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById<EditText>(R.id.email_edit_text)
        passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        arduinoIDEditText = findViewById<EditText>(R.id.etArduinoID)
        registerButton = findViewById(R.id.register_button)

        database = FirebaseDatabase.getInstance().reference

        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val arduinoID = arduinoIDEditText.text.toString()

            // Validate user input
            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(arduinoID)) {
                arduinoIDEditText.error = "Arduino ID is required"
                return@setOnClickListener
            }
            if (!linkArduinoID(arduinoID))
            {
                Toast.makeText(this, "Unable to link device at the moment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val uid = task.result.user?.uid ?: ""
                        assignIDtoUser(uid, arduinoID)
                        Toast.makeText(baseContext, "Successfully Registered!",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun assignIDtoUser(uid: String, arduinoID: String) {
        database.child("Users").child(uid).child("ArduinoID").setValue(arduinoID)
    }


    private fun linkArduinoID(arduinoID: String) : Boolean {
        // TODO: Check the ownership of arduino deviceID

        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putString(getString(R.string.arduino_id_key), arduinoID)
        editor.apply()

        val deviceToken = sharedPref.getString(getString(R.string.app_token_key), null)
        if (deviceToken != null)
        {
            database.child("arduinoID").child(arduinoID).setValue(deviceToken)
            return true
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if(currentUser != null){
            finish()
        }
    }
}
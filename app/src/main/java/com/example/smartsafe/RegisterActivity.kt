package com.example.smartsafe

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    companion object
    {
        const val TAG = "Register Activity"
    }

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById<EditText>(R.id.email_edit_text)
        passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        registerButton = findViewById(R.id.register_button)

        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validate user input
            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if(currentUser != null){
            finish()
        }
    }
}
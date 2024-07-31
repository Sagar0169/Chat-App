package com.example.chatapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chatapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible: Boolean = false


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Full-screen immersive mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Adjust status bar items color for API 23 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
        }

        // Adjust status bar appearance with WindowInsetsControllerCompat for better control
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = true
        }


        // Add TextWatcher to email and password fields
        binding?.etEmail?.addTextChangedListener(textWatcher)
        binding?.etPassword?.addTextChangedListener(textWatcher)
        binding?.btnLogin?.isEnabled = false // Initially disabled


        binding?.btnLogin?.setOnClickListener {
            val email = binding?.etEmail?.text.toString().trim()
            val password = binding?.etPassword?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                logIn(email, password)
            }
        }

        binding?.tvForgotPassword?.setOnClickListener{
            val intent = Intent(this@Login,SignUP::class.java)
            startActivity(intent)

        }

        val currUser = auth.currentUser
        if (currUser != null) {
            // User is already logged in, redirect to MainActivity
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle eye icon click for password visibility
        binding?.ivTogglePassword?.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Invalid credentials
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding?.etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding?.ivTogglePassword?.setImageResource(R.drawable.ic_eye_off) // change to eye icon
        } else {
            binding?.etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding?.ivTogglePassword?.setImageResource(R.drawable.ic_eye) // change to eye off icon
        }
        binding?.etPassword?.setSelection(binding?.etPassword?.text?.length ?: 0)  // Move cursor to end of text
        isPasswordVisible = !isPasswordVisible
    }

    // TextWatcher to monitor text changes
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = binding?.etEmail?.text.toString().trim()
            val password = binding?.etPassword?.text.toString().trim()
            binding?.btnLogin?.isEnabled = email.isNotEmpty() && password.isNotEmpty()

            binding?.btnLogin?.setTextColor(
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    resources.getColor(R.color.white) // Change to green when fields are filled
                } else {
                    resources.getColor(R.color.dark_gray) // Default color
                }
            )
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}

package com.example.chatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chatapp.databinding.ActivitySignUpBinding

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUP : AppCompatActivity() {
    private var binding: ActivitySignUpBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var Token:String
    private lateinit var mDbRef: DatabaseReference
    private var isPasswordVisible: Boolean = false
    private var isConfirmPasswordVisible: Boolean = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = true
        }

        binding?.etEmail?.addTextChangedListener(textWatcher)
        binding?.etPassword?.addTextChangedListener(textWatcher)
        binding?.etConfirmPass?.addTextChangedListener(textWatcher)
        binding?.etName?.addTextChangedListener(textWatcher)
        binding?.btnCreate?.isEnabled = false


        binding?.btnCreate?.setOnClickListener {
            val email = binding?.etEmail?.text.toString().trim()
            val password = binding?.etPassword?.text.toString().trim()
            val confirmPassword = binding?.etConfirmPass?.text.toString().trim()
            val name = binding?.etName?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show()
            } else {
                signUp(email, password,confirmPassword,name)
            }
        }


        binding?.ivTogglePassword?.setOnClickListener {
            togglePasswordVisibility()
        }

        binding?.ivToggleConfirmPassword?.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }






        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("Failed", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Token = token
            Log.d("Token", Token)
            Toast.makeText(this, Token, Toast.LENGTH_SHORT).show()
        })

    }

    private fun signUp(email: String, password: String, confirmPassword: String,name: String) {
        // Validate email and password
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed with sign-up if all validations pass
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {   task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                startActivity(Intent(this@SignUP, SetupProfileActivity::class.java)
                    .putExtra("DeviceToken", Token)
                    .putExtra("Name", name)
                )
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Error occurred: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Simple email validation
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding?.etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding?.ivTogglePassword?.setImageResource(R.drawable.ic_eye_off) // change to eye off icon
        } else {
            binding?.etPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding?.ivTogglePassword?.setImageResource(R.drawable.ic_eye) // change to eye icon
        }
        binding?.etPassword?.setSelection(binding?.etPassword?.text?.length ?: 0)  // Move cursor to end of text
        isPasswordVisible = !isPasswordVisible
    }

    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            binding?.etConfirmPass?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding?.ivToggleConfirmPassword?.setImageResource(R.drawable.ic_eye_off) // change to eye off icon
        } else {
            binding?.etConfirmPass?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding?.ivToggleConfirmPassword?.setImageResource(R.drawable.ic_eye) // change to eye icon
        }
        binding?.etConfirmPass?.setSelection(binding?.etConfirmPass?.text?.length ?: 0)  // Move cursor to end of text
        isConfirmPasswordVisible = !isConfirmPasswordVisible
    }

    // TextWatcher to monitor text changes
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = binding?.etEmail?.text.toString().trim()
            val password = binding?.etPassword?.text.toString().trim()
            val confirmPassword = binding?.etConfirmPass?.text.toString().trim()
            val name = binding?.etName?.text.toString().trim()

            binding?.btnCreate?.isEnabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && name.isNotEmpty()

            binding?.btnCreate?.setTextColor(
                if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && name.isNotEmpty()) {
                    resources.getColor(R.color.white) // Change to white when fields are filled
                } else {
                    resources.getColor(R.color.dark_gray) // Default color
                }
            )
        }

        override fun afterTextChanged(s: Editable?) {}
    }

}
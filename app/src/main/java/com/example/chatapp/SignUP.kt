package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
        binding?.buttonSignup?.setOnClickListener {
            signUp(binding?.edtEmail?.text.toString(), binding?.edtPassword?.text.toString())
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

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
//                addUserToDatabase(
//                    binding?.edtName?.text.toString(), email, auth.currentUser?.uid
//                )
                startActivity(Intent(this@SignUP, SetupProfileActivity::class.java).putExtra("DeviceToken",Token))

                finish()

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "error occured", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun addUserToDatabase(name: String, email: String, uid: String?) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        if (uid != null) {
//            mDbRef.child("user").child(uid).setValue(User(name,Token,name, email, uid))
        }
    }
}
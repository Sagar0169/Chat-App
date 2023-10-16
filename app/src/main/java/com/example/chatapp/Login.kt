package com.example.chatapp

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatapp.databinding.ActivityLoginBinding

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.buttonSignup?.setOnClickListener {
            startActivity(Intent(this, SignUP::class.java))
            finish()
        }
        binding?.buttonLogin?.setOnClickListener {
            logIn(binding?.edtEmail?.text.toString(), binding?.edtPassword?.text.toString())

        }
//        if(auth!!.currentUser!=null){
//            val intent = Intent(this@Login,MainActivity::class.java)
//           startActivity(intent)
//            finish()
//        }
//        else{
//            val intent = Intent(this@Login,SignUP::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    private fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                startActivity(Intent(this, MainActivity::class.java))

            } else {
                Toast.makeText(this, "user does not exists", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
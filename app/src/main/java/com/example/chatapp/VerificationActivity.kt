package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding
    var auth : FirebaseAuth?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.otp)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if(auth!!.currentUser!=null){
            val intent = Intent(this@VerificationActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        supportActionBar?.hide()
        binding.editNumber.requestFocus()
        binding.continueBtn.setOnClickListener{
            val intent = Intent(this@VerificationActivity,OTPActivity::class.java)
            intent.putExtra("phoneNumber",binding.editNumber.text.toString())
            startActivity(intent)
        }
    }
}
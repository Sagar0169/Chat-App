package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpactivityBinding
    var verificationId: String? = null
    var auth: FirebaseAuth? = null
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.otp)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = ProgressDialog(this@OTPActivity)
        dialog!!.setMessage("Sending Otp...")
        dialog!!.setCancelable(false)
        dialog!!.show()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        binding.phoneLabel.text = "Verify $phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this@OTPActivity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {

                }

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.otpView.setOtpCompletionListener { otp ->
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth!!.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@OTPActivity, SetupProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this@OTPActivity, "Failed", Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }
}
package com.example.chatapp

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chatapp.databinding.ActivitySetupProfileBinding
import com.example.chatapp.model.User
import com.example.chatapp.utilities.zoomimageview.Utility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupProfileBinding
    private var auth: FirebaseAuth? = null
    private lateinit var token: String
    private lateinit var name: String
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var selectedImage: Uri? = null
    private val galleryPermissionRequestCode = 123
    private var dialog: ProgressDialog? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding?.etName?.addTextChangedListener(textWatcher)
        binding?.etRole?.addTextChangedListener(textWatcher)
        token = intent.getStringExtra("DeviceToken").toString()
        name = intent.getStringExtra("Name").toString()
        binding.etName.setText(name)
        dialog = ProgressDialog(this).apply {
            setMessage("Updating Profile...")
            setCancelable(false)
        }
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        binding.imageView.setOnClickListener {
            openGallery()
        }

        binding.continueBtn02.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etName.error = "Please type a name"
                return@setOnClickListener
            }
            dialog?.show()
            if (selectedImage != null) {
                uploadImageAndSaveUser()
            } else {
                saveUserWithoutImage()
            }
        }
    }
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = binding?.etName?.text.toString().trim()
            val password = binding?.etRole?.text.toString().trim()
            binding?.continueBtn02?.isEnabled = email.isNotEmpty() && password.isNotEmpty()

            binding?.continueBtn02?.setTextColor(
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    resources.getColor(R.color.white) // Change to green when fields are filled
                } else {
                    resources.getColor(R.color.dark_gray) // Default color
                }
            )
        }

        override fun afterTextChanged(s: Editable?) {}
    }
    private fun uploadImageAndSaveUser() {
        val reference = storage!!.reference.child("Profile").child(auth!!.uid!!)
        val uploadTask = reference.putFile(selectedImage!!)

        uploadTask.addOnSuccessListener {
            Log.d("SetupProfileActivity", "Upload successful")
            reference.downloadUrl.addOnSuccessListener { uri ->
                val filePath = uri.toString()
                saveUserWithImage(filePath)
            }.addOnFailureListener { exception ->
                Log.e("SetupProfileActivity", "Failed to get download URL", exception)
                handleFailure("Failed to get download URL: ${exception.message}")
            }
        }.addOnFailureListener { exception ->
            Log.e("SetupProfileActivity", "Upload failed", exception)
            handleFailure("Upload failed: ${exception.message}")
        }
    }


    private fun saveUserWithImage(imageUrl: String) {
        val uid = auth!!.uid
        val phone = auth!!.currentUser!!.phoneNumber
        val name = binding.etName.text.toString()
        val role = binding.etRole.text.toString()
        val user = User(uid, name, name, phone, imageUrl, role, false, null, token, null)
        database!!.reference.child("users").child(uid!!).setValue(user)
            .addOnCompleteListener { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    Utility.savePreferencesString(this,AppConstants.senderName,name)
                    startActivity(Intent(this@SetupProfileActivity, MainActivity::class.java))
                    finish()
                } else {
                    handleFailure("Failed to save user")
                }
            }
    }

    private fun saveUserWithoutImage() {
        val uid = auth!!.uid
        val phone = auth!!.currentUser!!.phoneNumber
        val name = binding.etName.text.toString()
        val role = binding.etRole.text.toString()
        val user = User(uid, name, name, phone, "No Image", role, false, null, token, null)
        database!!.reference.child("users").child(uid!!).setValue(user)
            .addOnCompleteListener { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    startActivity(Intent(this@SetupProfileActivity, MainActivity::class.java))
                    finish()
                } else {
                    handleFailure("Failed to save user")
                }
            }
    }

    private fun handleFailure(message: String) {
        dialog!!.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 45 && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImage = data.data
            binding.imageView.setImageURI(selectedImage)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 45)
    }
}


package com.example.chatapp

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivitySetupProfileBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupProfileBinding
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var selectedImage: Uri? = null
    private val galleryPermissionRequestCode = 123
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.otp)
        setContentView(binding.root)
        dialog = ProgressDialog(this@SetupProfileActivity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        binding.imageView.setOnClickListener {
            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is granted, open the gallery
                openGallery()
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    galleryPermissionRequestCode
                )
            }
        }

        binding.continueBtn02.setOnClickListener {
            val name: String = binding.editName.text.toString()
            if (name.isEmpty()) {
                binding.editName.error = "Please type a name"
                return@setOnClickListener
            }
            dialog!!.show()
            if (selectedImage != null) {
                val reference = storage!!.reference.child("Profile")
                    .child(auth!!.uid!!)
                val uploadTask = reference.putFile(selectedImage!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    // Continue with the task to get the download URL
                    reference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val uid = auth!!.uid
                        val phone = auth!!.currentUser!!.phoneNumber
                        val name: String = binding.editName.text.toString()
                        val role: String = binding.role.text.toString()
                        val user = User(uid, name,name, phone, downloadUri.toString(),role,false,null,null)
                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCompleteListener {
                                dialog!!.dismiss()
                                val intent =
                                    Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        val uid = auth!!.uid
                        val phone = auth!!.currentUser!!.phoneNumber
                        val name: String = binding.editName.text.toString()
                        val role: String = binding.role.text.toString()
                        val user = User(uid, name,name, phone, "No Image",role,false,null,0)
                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCanceledListener {
                                dialog!!.dismiss()
                                val intent =
                                    Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (data.data != null) {
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")

                // Upload the image to Firebase Storage
                reference.putFile(uri!!)
                    .addOnSuccessListener { _ ->
                        // Once the upload is successful, get the download URL
                        reference.downloadUrl
                            .addOnSuccessListener { downloadUri ->
                                val filePath = downloadUri.toString()

                                // Update the com.example.chatapp.model.User object with the image URL
                                val uid = auth!!.uid
                                val phone = auth!!.currentUser!!.phoneNumber
                                val name: String = binding.editName.text.toString()
                                val role: String = binding.role.text.toString()
                                val user = User(uid, name, name,phone, filePath,role,false,null,null)

                                // Save the com.example.chatapp.model.User object to the database
                                database!!.reference
                                    .child("users")
                                    .child(uid!!)
                                    .setValue(user)
                                    .addOnCompleteListener { task ->
                                        dialog!!.dismiss()
                                        if (task.isSuccessful) {
                                            // Image URL saved successfully
                                            // Handle success scenario as needed
                                        } else {
                                            // Handle database error
                                            val exception = task.exception
                                            if (exception != null) {
                                                exception.printStackTrace()
                                                Log.e(
                                                    "DatabaseError",
                                                    exception.message ?: "Unknown error"
                                                )
                                            }
                                            // Handle the failure scenario as needed
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                // Handle failure to get the download URL
                                e.printStackTrace()
                                Log.e("DownloadURLError", e.message ?: "Unknown error")
                                // Handle the failure scenario as needed
                            }
                    }
                    .addOnFailureListener { e ->
                        // Handle upload failure
                        e.printStackTrace()
                        Log.e("UploadError", e.message ?: "Unknown error")
                        // Handle the failure scenario as needed
                    }

                binding.imageView.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == galleryPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, open the gallery
                openGallery()
            } else {
                // Permission is denied, show a toast message
                Toast.makeText(
                    this,
                    "Cannot access gallery without permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ...

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 45)
    }
}


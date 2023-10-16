package com.example.chatapp

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.adapter.Messages2Adapter
import com.example.chatapp.adapter.MessagesAdapter
import com.example.chatapp.databinding.ActivityGroupChatBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Date

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var messageAdapter: Messages2Adapter
    private lateinit var messageList: ArrayList<Message2>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var groupId: String
    private var groupName: String? = null
    private var userName: String? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var senderUid: String? = null
    var image: String? = null
    private lateinit var progressBar: ProgressBar

    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        groupId = intent.getStringExtra("GroupUid") ?: ""

        groupName = intent.getStringExtra("GroupName") ?: ""
        userName = intent.getStringExtra("user_name") ?: ""
        Log.d("username2", groupId.toString())
        supportActionBar?.title = groupName
        binding.name.text = groupName
        binding.imageView2.setOnClickListener {
            onBackPressed()
        }
        progressBar = binding.progressBar
        messageList = ArrayList()
        messageAdapter = Messages2Adapter(this, messageList, groupId)
        binding.groupMessageRecycler.layoutManager = LinearLayoutManager(this)
        binding.groupMessageRecycler.adapter = messageAdapter


        mDbRef.child("groupChats").child(groupId).child("messages")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message2::class.java)
                    if (message != null) {
                        messageList.add(message)
                        messageAdapter.notifyDataSetChanged()
                        binding.groupMessageRecycler.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle message changes if needed
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle message removal if needed
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle message movement if needed
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })

//        binding.sendGroupMessageButton.setOnClickListener {
//            val messageText = binding.groupMessageBox.text.toString().trim()
//            if (messageText.isNotEmpty()) {
//                val senderUid = auth.currentUser?.uid ?: ""
////                val groupid=mDbRef.child("groupChats").push().key
//                val message = Message2(messageText,userName, senderUid, System.currentTimeMillis())
//
//
//                    mDbRef.child("groupChats").child(groupId)
//                        .child("messages")
//                        .push()
//                        .setValue(message)
//
//
//                binding.groupMessageBox.text.clear()
//            }
//        }
        binding.sendGroupMessageButton.setOnClickListener {
            val messageText = binding.groupMessageBox.text.toString().trim()

            if (messageText.isNotEmpty()) {
                val senderUid = auth.currentUser?.uid ?: ""
                val date = Date()
                val message = Message2(messageText, userName, senderUid,date.time)
                val lastMsgObj = HashMap<String, Any>()
                lastMsgObj["lastMsg"] = "$userName: $messageText" // Include sender's username
                lastMsgObj["groupchatprofile"]
                lastMsgObj["lastMsgTime"] = date.time
                mDbRef.child("groupChats").child(groupId).updateChildren(lastMsgObj)

                mDbRef.child("groupChats").child(groupId).child("messages").push()
                    .setValue(message)

                binding.groupMessageBox.text.clear()
            }
        }



        binding.ivCamera.setOnClickListener {
            checkAndOpenGallery()
        }

        mDbRef.child("groupChats").child(groupId)
            .child("groupchatprofile")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    image = dataSnapshot.value.toString()
                    if (!isDestroyed && !isFinishing) {
                        // Load the profile image using Glide
                        Glide.with(this@GroupChatActivity)
                            .load(image)
                            .into(binding.profile01)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors if needed
                }
            })
        binding.profile01.setOnClickListener {
            showImageDialog(this, image!!)
        }
    }

    private fun checkAndOpenGallery() {
        // Check if the READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openGalleryForImage()
        } else {
            // Request the READ_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the gallery
                openGalleryForImage()
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Upload the image to Firebase Storage
                this.imageUri = imageUri
                uploadImageToStorage()
            }
        }
    }

    private fun uploadImageToStorage() {
        imageUri?.let {
            // Define a reference for the image in Firebase Storage
            val imageRef = storageReference.child("group_profile_images")
                .child("$groupId.jpg")

            // Upload the image
            imageRef.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    // Image upload successful
                    // Get the download URL for the uploaded image
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()

                        // Save the download URL to the groupchatprofile node
                        mDbRef.child("groupChats").child(groupId)
                            .child("groupchatprofile")
                            .setValue(imageUrl)

                        // Update the ImageView with the selected image
                        // Use an image loading library like Glide or Picasso to display the image
                        // For example, if you are using Glide:
                        Glide.with(this@GroupChatActivity).load(imageUrl).into(binding.profile01)
                        progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to upload the image
                    progressBar.visibility = View.GONE
                }
            progressBar.visibility = View.VISIBLE
        }
    }
    fun showImageDialog(context: Context, image: String) {
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.imagedialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setGravity(Gravity.CENTER)
        val lp: WindowManager.LayoutParams = dialog.window!!.attributes
        lp.dimAmount = 0.75f
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window!!.attributes = lp
        val dialogImage = dialog.findViewById(R.id.ivImg) as ImageView
        val ivClose = dialog.findViewById(R.id.ivClose) as ImageView

        Log.d("image", image)

        Glide.with(context).load(image)
            .placeholder(R.color.black)
            .into(dialogImage)

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    companion object {
        private const val IMAGE_REQUEST_CODE = 123
        private const val STORAGE_PERMISSION_CODE = 124
    }
}

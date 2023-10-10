package com.example.chatapp

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.adapter.MessagesAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date
import android.Manifest


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    var adapter: MessagesAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var cnt = 0
    private val galleryPermissionRequestCode = 123
    var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Sending image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        val id = intent.getStringExtra("uid")
        if (id != null) {
            Log.d("uidd", id)
        }

        binding.dots.setOnClickListener { view ->
            showPopupMenu(view)
        }


//        binding.transparentOverlay.setOnClickListener {
//            binding.delete.visibility = View.GONE
//            binding.transparentOverlay.visibility = View.GONE
//        }
//
//        binding.delete.setOnClickListener {
//            clearChat()
//        }

        binding.attachment.setOnClickListener {
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

        binding.name.text = name
        Glide.with(this).load(profile)
            .placeholder(R.drawable.gallery_placeholder)
            .into(binding.profile01)
        binding.profile01.setOnClickListener {
            showImageDialog(this, profile!!)
        }
        binding.imageView2.setOnClickListener {
            finish()
        }

        receiverUid = intent.getStringExtra("uid")
        Log.d("receiverUid", receiverUid!!)
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("senderUid", senderUid!!)

        database!!.reference
            .child("Presence")
            .child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline") {
                            binding.status.visibility = View.GONE
                        } else {
                            binding.status.text = status
                            binding.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this, messages!!, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Attach a listener to the "messages" node in the database
        val messagesRef = database!!.reference.child("chats").child(senderRoom!!).child("messages")
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages!!.clear()
                cnt = 1  // Reset unread message count
                for (snapshot1 in snapshot.children) {
                    val message = snapshot1.getValue(Message::class.java)
                    message!!.messageId = snapshot1.key
                    if (!message.seen) {
                        cnt += 1  // Increment unread message count
                    }



                    messages!!.add(message)
                }

                // Update the adapter and UI
                adapter!!.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(adapter!!.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })

        binding.sendBtn.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time, false)
            Log.d("onActivity", message.senderId!! + "2")
            binding.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time
            lastMsgObj["lastMsgFrom"] = senderUid!!
            lastMsgObj["lastMsgUnRead"] = cnt
            lastMsgObj["lastMsgSeen"] = false

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {}
                }
        }

//        binding.attachment.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(intent, 25)
//        }

        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStopperTyping, 1000)
            }

            var userStopperTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)

        // Inflate your menu resource
        popup.menuInflater.inflate(R.menu.image_context_menu, popup.menu)

        // Get the Menu object
        val menu = popup.menu

        // Apply the custom style to each menu item
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val view = menuItem.actionView
            view?.setBackgroundResource(R.drawable.white_circle) // Set your custom background drawable here
            view?.setBackgroundColor(ContextCompat.getColor(this, R.color.accentColor)) // Set your custom text color here
        }

        // Set a click listener for menu items
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    // Handle the delete action
                    clearChat()
                    true
                }
                // Add more cases for other menu items as needed

                else -> false
            }
        }

        // Show the popup menu
        popup.show()
    }


    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    // ... (other methods)

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

    private fun clearChat() {
        // Clear messages in sender's chat room
        database!!.reference
            .child("chats")
            .child(senderRoom!!)
            .child("messages")
            .removeValue()
            .addOnCompleteListener { senderTask ->
                if (senderTask.isSuccessful) {
                    // Sender's chat room is cleared
                    Toast.makeText(
                        this@ChatActivity,
                        "Chat history cleared",
                        Toast.LENGTH_SHORT
                    ).show()
//                    binding.delete.visibility = View.GONE
                } else {
                    // Handle the failure to clear sender's chat room
                    Toast.makeText(
                        this@ChatActivity,
                        "Failed to clear chat history",
                        Toast.LENGTH_SHORT
                    ).show()
//                    binding.delete.visibility = View.GONE
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25){
            if (data != null) {
                if(data.data!=null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    var reference = storage?.reference?.child("chats")
                        ?.child(calendar.timeInMillis.toString()+"")
                    dialog!!.show()
                    reference?.putFile(selectedImage!!)?.addOnCompleteListener{ task->
                        dialog!!.dismiss()
                        if(task.isSuccessful){
                            reference.downloadUrl.addOnSuccessListener { uri->
                                val filePath = uri.toString()
                                val messageTxt : String = binding.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid,date.time,false)
                                Log.d("onActivity", message.senderId!! + "2")
                                message.message = "photo"
                                message.imageUrl = filePath
                                binding.messageBox.setText("")
                                val randomkey = database!!.reference.push().key
                                val lastMsgObj = HashMap<String,Any>()
                                lastMsgObj["lastMsg"] = message.message!!
                                lastMsgObj["lastMsgTime"] = date.time
                                database!!.reference.child("chats")
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(receiverRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomkey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(receiverRoom!!)
                                            .child("messages")
                                            .child(randomkey)
                                            .setValue(message)
                                            .addOnSuccessListener {  }
                                    }
                            }
                        }
                        else{
                            Toast.makeText(this@ChatActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 25)
    }

}

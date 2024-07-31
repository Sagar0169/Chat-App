package com.example.chatapp

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
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
import com.example.chatapp.utilities.zoomimageview.Utility
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException

import java.util.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val SERVER_KEY = "AAAA0Suoa18:APA91bFS3mwFl_JEnfPjiqmfBdusauvb5n2LeTKP95hXdxaqRKWNqZC5Zams68sC165xNKlIklHOuNivfQzGCQTr2kN0nUh_nI1qLhD4zu3cTak4Z8Xh31gkXayVx-uaLjTi1Ujn32XT"
    private lateinit var token: String
    private lateinit var name: String
    private lateinit var binding: ActivityChatBinding
    private var adapter: MessagesAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var messages: ArrayList<Message>? = null
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var image: String? = null
    private var uid: String? = null
    private var profileImage: String? = null
    private var cnt = 0
    private val galleryPermissionRequestCode = 123
    private var receiverUid: String? = null
    private var isFrom: Int? = 0
    private var deviceId: String? = null
    private val PERMISSION_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        deviceId = Utility.getUniqueIDWithRandomString()
        token = intent.getStringExtra("DeviceToken").toString()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Sending image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)


        val id = intent.getStringExtra("uid")
        isFrom = intent.getIntExtra("Isfrom", 0)
        if (isFrom == 1) {
            profileImage = intent.getStringExtra("Image")
            name = intent.getStringExtra("Name").toString()
            senderUid = intent.getStringExtra("Suid")
            receiverUid = intent.getStringExtra("Rid")
        } else {
            profileImage = intent.getStringExtra("image")
            name = intent.getStringExtra("name").toString()
            Log.d("Namee",name)
            senderUid = FirebaseAuth.getInstance().currentUser?.uid
            receiverUid = intent.getStringExtra("uid")
        }


        database!!.reference.child("users").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.value as? Map<String, Any>
                        val profileImage = userData?.get("profileImage") as? String
                        val userName = userData?.get("name") as? String

                        binding.name.text = userName
                        if (profileImage != null) {
                            Glide.with(applicationContext).load(profileImage)
                                .placeholder(R.drawable.ic_placeholder)
                                .into(binding.profile01)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors if needed
                }
            })

        uid = id
        if (id != null) {
            Log.d("Token", token)
        }

        binding.dots.setOnClickListener { view ->
//            showPopupMenu(view)
        }

        createNotificationChannel()
        requestNecessaryPermissions()

        binding.attachment.setOnClickListener {
            if (hasStoragePermission()) {
                openGalleryForImage()
            } else {
                requestStoragePermission()
            }
        }

        binding.profile01.setOnClickListener {
            showFullImageDialog(profileImage!!)
        }
        binding.imageView2.setOnClickListener {
            onBackPressed()
        }
        val message = getPreferenceString(this, AppConstants.senderName)
        Log.d("SenderName",message)
        binding.cvCall.setOnClickListener {
            // sendPushNotificationToRecipientCalling(uid, message, "receive call")
        }
        binding.cvVideo.setOnClickListener {
            val intent = Intent(this, CallActivity::class.java).putExtra("frag", 1)
            startActivity(intent)
        }

        Log.d("Uidreceiver", receiverUid!!)
        Log.d("Uidsender", senderUid!!)

        database!!.reference.child("presence").child(receiverUid!!)
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
        adapter = MessagesAdapter(this, messages!!, senderRoom!!, receiverRoom!!, deviceId)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val messagesRef = database!!.reference.child("chats").child(senderRoom!!).child("messages")
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages!!.clear()
                cnt = 1
                for (snapshot1 in snapshot.children) {
                    val message = snapshot1.getValue(Message::class.java)
                    message!!.messageId = snapshot1.key
                    if (!message.seen) {
                        cnt += 1
                    }
                    messages!!.add(message)
                }
                adapter!!.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(adapter!!.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })

        binding.sendBtn.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            val encryptedMessage: String? = Utility.getEncryptedString(
                binding.messageBox.text.toString().trim(),
                AppConstants.CRYPT_KEY
            )
            val date = Date()
            val message = Message(encryptedMessage, senderUid, date.time, false)
            binding.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time
            lastMsgObj["lastMsgFrom"] = senderUid!!
            lastMsgObj["lastMsgUnRead"] = cnt
            lastMsgObj["lastMsgSeen"] = false

            database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages").child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats").child(receiverRoom!!)
                        .child("messages").child(randomKey).setValue(message)
                }
//            sendFcmMessage(messageTxt, name)
            sendPushNotificationToRecipient(receiverUid ,senderUid, getPreferenceString(this,AppConstants.senderName), messageTxt,profileImage)        }


        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                val user = database!!.reference.child("presence").child(senderUid!!)
                user.setValue("typing...")
                Handler().postDelayed({
                    user.setValue("Online")
                }, 1000)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        supportActionBar?.hide()
    }


    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()
        if (!hasStoragePermission()) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasStoragePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun hasNotificationPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val permissionResults = permissions.mapIndexed { index, permission ->
                    permission to grantResults[index]
                }.toMap()

                if (permissionResults[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryForImage()
                } else {
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (permissionResults[Manifest.permission.POST_NOTIFICATIONS] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

//    private fun showPopupMenu(view: View) {
//        val popupMenu = PopupMenu(this, view)
//        popupMenu.gravity = Gravity.END
//        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
//        popupMenu.setOnMenuItemClickListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_delete -> {
//                    val reference = database!!.reference.child("chats")
//                        .child(senderRoom!!).child("messages")
//                    reference.removeValue()
//                    true
//                }
//                else -> false
//            }
//        }
//        popupMenu.show()
//    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "chatApp"
            val descriptionText = "chatApp Notification Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("chatApp", name, importance)
            channel.description = descriptionText
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPreferenceString(context: Context, key: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, "") ?: ""
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, galleryPermissionRequestCode)
    }

    private fun showFullImageDialog(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Image URL is not valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this, R.style.TransparentDialog)
        dialog.setContentView(R.layout.dialog_full_image)

        val fullImageView: ImageView = dialog.findViewById(R.id.fullImageView)
        val closeButton: ImageButton = dialog.findViewById(R.id.closeButton)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(fullImageView)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            galleryPermissionRequestCode -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    openGalleryForImage()
//                } else {
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//            else -> {
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == galleryPermissionRequestCode) {
            val selectedImage = data?.data
            val filePath = storage!!.reference.child("chats").child(Date().time.toString() + "")
            dialog!!.show()
            filePath.putFile(selectedImage!!).addOnCompleteListener { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        val filePath = uri.toString()
                        val messageTxt = binding.messageBox.text.toString()
                        val date = Date()
                        val message = Message(messageTxt, senderUid, date.time, false)
                        message.message = "photo"
                        message.imageUrl = filePath
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
                            .child("messages").child(randomKey!!)
                            .setValue(message).addOnSuccessListener {
                                database!!.reference.child("chats")
                                    .child(receiverRoom!!)
                                    .child("messages")
                                    .child(randomKey).setValue(message)
                            }
//                        sendFcmMessage(messageTxt, name)
                        sendPushNotificationToRecipient(receiverUid ,senderUid, getPreferenceString(this,AppConstants.senderName), messageTxt,profileImage)                    }
                }
            }
        }
    }


    private fun sendPushNotificationToRecipient(SUid: String?, RUid: String?,
                                                senderName: String?, message: String,profileImage:String?) {
        // Replace "RECIPIENT_FCM_TOKEN" with the actual FCM token of the recipient
        val recipientFcmToken = token

        if (recipientFcmToken != null) {
            val notificationData = mapOf(
                "title" to "New Message from $senderName",
                "body" to message,
                "Suid" to SUid,
                "Ruid" to RUid,
                "name" to senderName,
                "image" to profileImage

                // Add any other relevant data
            )

            val fcmMessage = mapOf(
                "to" to recipientFcmToken,
                "data" to notificationData
            )
            Log.d("fcmMessage",fcmMessage.toString())
            // Use your preferred method to send the FCM message to the recipient
            // This could involve using a server, Cloud Functions, or any other backend solution
            sendFcmMessageToRecipient(fcmMessage)
        }
    }
    private fun sendFcmMessageToRecipient(fcmMessage: Map<String, Any>) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = sendFcmMessage(fcmMessage)
                // Handle the response as needed
                val responseBody = response?.body?.string()
                println("FCM Message Response: $responseBody")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private  fun sendFcmMessage(fcmMessage: Map<String, Any>): Response? {
        val json = mapToJson(fcmMessage)
        Log.d("JSONDATA",json)
        val client = OkHttpClient()

        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(FCM_API)
            .post(body)
            .addHeader("Authorization", "key=$SERVER_KEY")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            client.newCall(request).execute()

        } catch (e: IOException) {
            null
        }
    }
    private fun mapToJson(data: Map<String, Any>): String {
        return Gson().toJson(data)
    }
    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }



}

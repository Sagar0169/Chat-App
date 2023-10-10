package com.example.chatapp



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
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

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var messageAdapter: Messages2Adapter
    private lateinit var messageList: ArrayList<Message2>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var groupId: String
    private  var groupName: String?=null
    private  var userName: String?=null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        groupId = intent.getStringExtra("GroupUid") ?: ""
        groupName = intent.getStringExtra("GroupName") ?: ""
        userName = intent.getStringExtra("user_name")?: ""
        Log.d("username2",userName.toString())
        supportActionBar?.title = groupName
        binding.name.text = userName
        binding.imageView2.setOnClickListener{
            onBackPressed()
        }
        messageList = ArrayList()
        senderRoom = senderUid+receiverUid
        receiverRoom = receiverUid+senderUid
        messageAdapter = Messages2Adapter(this, messageList)
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
        binding.sendGroupMessageButton.setOnClickListener {
            val messageText = binding.groupMessageBox.text.toString().trim()

            if (messageText.isNotEmpty()) {
                val senderUid = auth.currentUser?.uid ?: ""
                val message = Message2(messageText,userName, senderUid, System.currentTimeMillis())

                mDbRef.child("groupChats").child(groupId).child("messages").push()
                    .setValue(message)

                binding.groupMessageBox.text.clear()
            }
        }
    }



}
package com.example.chatapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var database: FirebaseDatabase? = null
    var users: ArrayList<User>? = null
    var lastSeenMsg: ArrayList<String>? = null
    var groupChats: ArrayList<User>? = null
    var allChats: ArrayList<User>? = null

    var usersAdapter: UserAdapter? = null
    private var dialog: ProgressDialog? = null
    var user: User? = null
    var userName: String? = null
    var CurrentUser: User? = null
    var senderName: String? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(binding.root)

        FirebaseApp.initializeApp(this)


        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        users = ArrayList()
        groupChats = ArrayList()
        allChats = ArrayList()
        askNotificationPermission()

        binding?.llSchemes?.setOnClickListener {
//            binding?.view?.visibility=  View.VISIBLE
//            binding?.view1?.visibility= View.GONE
            binding.tvSchemes.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvGuildLines.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.groupChat.visibility = View.GONE
            RvAdapter(users!!)
        }

        binding?.llGuildLines?.setOnClickListener {
//            binding?.view?.visibility= View.GONE
//            binding?.view1?.visibility= View.VISIBLE
            binding.tvSchemes.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.tvGuildLines.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.groupChat.visibility = View.VISIBLE
            RvAdapter(groupChats!!)
        }


        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    userName = user!!.user_name

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        binding.groupChat.setOnClickListener {
            showCreateGroupDialog()
        }

        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children) {
                    val user: User? = snapshot1.getValue(User::class.java)
                    CurrentUser=user
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) {
                        users!!.add(user)
//                        allChats!!.add(user) // Add users to the combined list
                    }
                    usersAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (snapshot1 in snapshot.children) {
                    val user: User? = snapshot1.getValue(User::class.java)
                    if (user!!.uid.equals(FirebaseAuth.getInstance().uid)) {
                        senderName=snapshot1.child("name").value.toString()
                        savePreferencesString(this@MainActivity,AppConstants.senderName,senderName.toString())
//                        allChats!!.add(user) // Add users to the combined list
                    }
                    usersAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        database!!.reference.child("groupChats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupChats!!.clear()
                for (postSnapshot in snapshot.children) {
                    val groupId = postSnapshot.key
                    val groupName = postSnapshot.child("groupName").value.toString()
//                    val currentUser=postSnapshot.getValue(User::class.java)
                    val groupUser =
                        User(groupId, groupName, userName, null, null, null, true, null,null, null)
                    if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                          groupChats!!.add(groupUser)
//                        allChats!!.add(groupUser) // Add group chats to the combined list
                    }
//                    groupChats!!.add(groupUser)
//                    if (groupName!=null)
//                    {
//                    groupChats!!.add(groupUser)}
                }
                // Update the adapters after adding groups
                usersAdapter!!.notifyDataSetChanged()
                // Update the adapter for group chats
                // groupChatsAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })



        val userRef = database?.reference?.child("users")
        userRef?.keepSynced(true)
        RvAdapter(users!!)
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }


    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("offline")
    }

//    private fun showCreateGroupDialog() {
//        // Implement a dialog or start a new activity to get group information
//        // For simplicity, let's assume you show a dialog
//
//        // You can use AlertDialog or a custom dialog for this purpose
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Create Group")
//
//        val input = EditText(this)
//        input.inputType = InputType.TYPE_CLASS_TEXT
//        builder.setView(input)
//
//        builder.setPositiveButton("Create") { dialog, which ->
//            val groupName = input.text.toString().trim()
//
//            if (groupName.isNotEmpty()) {
//                // Create a new group in the database
//                val newGroupRef = database?.reference!!.child("groupChats").push()
//                val groupId = newGroupRef.key // Get a unique group ID
//
//                // Add current user as a member
//                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//                if (currentUserId != null) {
//                    newGroupRef.child("members").child(currentUserId).setValue(true)
//                }
//
//                // Add group information
//                newGroupRef.child("groupName").setValue(groupName)
//
//                // Redirect the user to the group chat activity
//                val intent = Intent(this, GroupChatActivity::class.java).apply {
//                    putExtra("isGroupChat", true)
//                    putExtra("groupId", groupId)
//                    putExtra("name", groupName)
//                    putExtra("user_name", userName)
//                }
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        builder.setNegativeButton("Cancel") { dialog, which ->
//            dialog.cancel()
//        }
//
//        builder.show()
//    }
private fun showCreateGroupDialog() {
    // Create an AlertDialog to get the group name from the user
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Create Group")

    val input = EditText(this)
    input.inputType = InputType.TYPE_CLASS_TEXT
    builder.setView(input)

    builder.setPositiveButton("Create") { dialog, which ->
        val groupName = input.text.toString().trim()

        if (groupName.isNotEmpty()) {
            // Create a new group in the database with a name
            val newGroupRef = database?.reference!!.child("groupChats").push()
            val groupId = newGroupRef.key // Get a unique group ID

            // Add current user as a member
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                newGroupRef.child("members").child(currentUserId).setValue(true)
            }

            // Add group information including the name
            newGroupRef.child("groupName").setValue(groupName)

            // Redirect the user to the group chat activity
            val intent = Intent(this, GroupChatActivity::class.java).apply {
                putExtra("isGroupChat", true)
                putExtra("groupId", groupId)
                putExtra("name", groupName)
                putExtra("user_name", userName)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
        }
    }

    builder.setNegativeButton("Cancel") { dialog, which ->
        dialog.cancel()
    }

    builder.show()
}



    private fun RvAdapter(list: ArrayList<User>) {
        usersAdapter = UserAdapter(this@MainActivity, list!!, database!!)
        val layoutManager = LinearLayoutManager(this@MainActivity)
        binding.mRec.layoutManager = layoutManager
        binding.mRec.adapter = usersAdapter
    }
    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionExplanationDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private fun showPermissionExplanationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("Granting the POST_NOTIFICATION permission enables XYZ feature. Do you want to proceed?")

        builder.setPositiveButton("OK") { _, _ ->
            // User clicked "OK," directly request the permission
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        builder.setNegativeButton("No thanks") { _, _ ->
            // User clicked "No thanks," allow the user to continue without notifications
            Toast.makeText(this,"You declined the permission",Toast.LENGTH_LONG).show()
        }

        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }
}
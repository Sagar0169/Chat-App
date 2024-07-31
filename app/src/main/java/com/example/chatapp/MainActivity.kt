package com.example.chatapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.User
import com.example.chatapp.utilities.zoomimageview.Utility
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
    var groupChats: ArrayList<User>? = null
    private lateinit var auth: FirebaseAuth
    var usersAdapter: UserAdapter? = null
    private var dialog: ProgressDialog? = null
    var user: User? = null
    var userName: String? = null
    var profileImage: String? = null
    var GroupId: String? = null
    var senderName: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, you can proceed with notifications.
        } else {
            // Permission denied, handle it as needed.
            Toast.makeText(this, "You declined the notification permission", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        users = ArrayList()
        groupChats = ArrayList()

        askNotificationPermission()

        // Initialize UI components and adapters

//        binding.llSchemes.setOnClickListener {
//            binding.tvSchemes.setTextColor(ContextCompat.getColor(this, R.color.white))
//            binding.tvGuildLines.setTextColor(ContextCompat.getColor(this, R.color.black))
//            binding.groupChat.visibility = View.GONE
//            RvAdapter(users!!)
//        }
//
//        binding?.llGuildLines?.setOnClickListener {
////            binding?.view?.visibility= View.GONE
////            binding?.view1?.visibility= View.VISIBLE
//            binding.tvSchemes.setTextColor(ContextCompat.getColor(this, R.color.black))
//            binding.tvGuildLines.setTextColor(ContextCompat.getColor(this, R.color.white))
//            binding.groupChat.visibility = View.VISIBLE
////            RvAdapter(groupChats!!)
//            logout()
//        }

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    if (user == null) {
                        // User does not exist in the database
                        Toast.makeText(this@MainActivity, "User not found. Redirecting to login page.", Toast.LENGTH_SHORT).show()
                        redirectToLogin()
                    } else {
                        userName = user?.user_name
                        Utility.savePreferencesString(this@MainActivity,AppConstants.senderName,userName!!)
                        profileImage = user?.profileImage

                        Log.d("UserName", userName.toString())

                        Glide.with(baseContext).load(profileImage)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(binding.profileIcon)

                        Glide.with(baseContext).load(profileImage)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(binding.storyImage)
                        // Continue with the existing logic if user exists
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })

        binding.groupChat.setOnClickListener {
            showCreateGroupDialog()
        }

        binding.profileIcon.setOnClickListener {
            showFullImageDialog(profileImage)
        }

        RvAdapter(users!!)
        // Fetch user data from Firebase and populate the user list
        fetchUserData()
        fetchGroupData()



        val storyList = listOf(
            Story(R.drawable.ic_placeholder, "Dior", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Adil", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Marina", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Dean", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Sam", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Speed", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Millie", R.color.yellow),
            Story(R.drawable.ic_placeholder, "Max", R.color.yellow)
        )
        val storyAdapter = StoryAdapter(storyList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = storyAdapter
        }
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


    private fun fetchUserData() {
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children) {
                    val user: User? = snapshot1.getValue(User::class.java)
                    if (user?.uid != FirebaseAuth.getInstance().uid) {
                        users!!.add(user!!)
                    }
                }
                usersAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    private fun fetchGroupData() {
        database!!.reference.child("groupChats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupChats!!.clear()
                for (postSnapshot in snapshot.children) {
                    GroupId = postSnapshot.key?: ""
                    val groupName = postSnapshot.child("groupName").value.toString()
                    val currentUser=postSnapshot.getValue(User::class.java)
                    val groupUser =
                        User(GroupId, groupName, userName, null, null, null, true, null,null, null)
                    if (FirebaseAuth.getInstance().currentUser?.uid != currentUser?.uid) {
                        groupChats!!.add(groupUser)
//
                    }
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

    }

    private fun showCreateGroupDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Group")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, which ->
            val groupName = input.text.toString().trim()

            if (groupName.isNotEmpty()) {
                // Create a new group in the database
                val newGroupRef = database?.reference!!.child("groupChats").push()
                val groupId = newGroupRef.key

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    newGroupRef.child("members").child(currentUserId).setValue(true)
                }

                newGroupRef.child("groupName").setValue(groupName)
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

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
    private fun redirectToLogin() {
        auth.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }


    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can proceed with notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionExplanationDialog()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("Granting the SEND_NOTIFICATIONS permission enables XYZ feature. Do you want to proceed?")

        builder.setPositiveButton("OK") { _, _ ->
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        builder.setNegativeButton("No thanks") { _, _ ->
            Toast.makeText(this, "You declined the permission", Toast.LENGTH_LONG).show()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
}

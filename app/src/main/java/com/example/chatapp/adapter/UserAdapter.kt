package com.example.chatapp.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.ChatActivity
import com.example.chatapp.GroupChatActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.UsersBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UserAdapter(
    var context: Context,
    var userList: ArrayList<User>,
    var database: FirebaseDatabase
) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var image: String? = null
    var timestamp: String? = null
    var unReadCount: String? = null

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: UsersBinding = UsersBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.users, parent, false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val chatRoomId =
            FirebaseAuth.getInstance().currentUser?.uid + user.uid // Replace with the actual chat room ID you want to display
        Log.d("chatRoomId", chatRoomId)

        // Add a ValueEventListener to fetch the last message for the chat room
        if (!user.isGroup) {
            database.reference.child("chats")
                .child(chatRoomId)
                .child("lastMsg")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val lastMsg = dataSnapshot.value.toString()
                        database.reference.child("chats")
                            .child(chatRoomId)
                            .child("lastMsgSeen")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val seen = dataSnapshot.value.toString()
                                    if (seen == "true") {
                                        holder.binding.messageSentTick.setImageResource(R.drawable.tick_green)
                                    } else {
                                        holder.binding.messageSentTick.setImageResource(R.drawable.tick_black)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle any errors if needed
                                }
                            })
                        database.reference.child("chats")
                            .child(chatRoomId)
                            .child("lastMsgFrom")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val lastMsgSender = dataSnapshot.value.toString()
                                    if (lastMsgSender == FirebaseAuth.getInstance().currentUser?.uid) {
                                        // Last message is from the current user
                                        holder.binding.tvRole.text = "You: $lastMsg"
                                        holder.binding.messageSentTick.visibility = View.VISIBLE

                                    } else {
                                        // Last message is from the receiver
                                        holder.binding.tvRole.text = lastMsg
                                        holder.binding.messageSentTick.visibility = View.GONE

                                    }

                                    database.reference.child("chats")
                                        .child(chatRoomId)
                                        .child("lastMsgUnRead")
                                        .addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                unReadCount =
                                                    dataSnapshot.value?.toString() // Use safe call operator
                                                if (lastMsgSender == FirebaseAuth.getInstance().currentUser?.uid) {
                                                    // Last message is from the current user or unReadCount is null/empty/zero
                                                    Log.d("checking", "2")
                                                    holder.binding.messageCount.visibility =
                                                        View.GONE
                                                } else if (unReadCount.isNullOrEmpty() || (unReadCount!!.toInt() == 0)) {
                                                    Log.d("checking", "3")
                                                    holder.binding.messageCount.visibility =
                                                        View.GONE
                                                } else {
                                                    Log.d("checking", "4")
                                                    holder.binding.messageCount.visibility =
                                                        View.VISIBLE
                                                    holder.binding.messageCount.text = unReadCount
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Handle any errors if needed
                                            }
                                        })


                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle any errors if needed
                                }
                            })
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors if needed
                    }
                })

            database.reference.child("chats")
                .child(chatRoomId)
                .child("lastMsgTime")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        timestamp = dataSnapshot.value.toString()
                        val timeInMillis = timestamp!!.toLongOrNull()

                        if (timeInMillis != null) {
                            val date = Date(timeInMillis)
                            val formattedTime = dateFormat.format(date)
                            holder.binding.timeStamp.text = formattedTime
                        } else {
                            // Handle the case where timestamp is null or not a valid Long
                            holder.binding.timeStamp.text = ""
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors if needed
                    }
                })


//        holder.binding.messageCount.text = unReadCount
            holder.binding.username.text = user.name

            Glide.with(context).load(user.profileImage)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.binding.profile)

            holder.binding.profile.setOnClickListener {
                showImageDialog(context, user.profileImage!!)
            }
        } else {
            holder.binding.username.text = user.name
            database.reference.child("groupChats").child(user.uid!!)
                .child("lastMsgTime")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        timestamp = dataSnapshot.value.toString()
                        val timeInMillis = timestamp!!.toLongOrNull()

                        if (timeInMillis != null) {
                            val date = Date(timeInMillis)
                            val formattedTime = dateFormat.format(date)
                            holder.binding.timeStamp.text = formattedTime
                        } else {
                            // Handle the case where timestamp is null or not a valid Long
                            holder.binding.timeStamp.text = ""
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors if needed
                    }
                })
            database.reference.child("groupChats").child(user.uid!!)
                .child("groupchatprofile")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        image = dataSnapshot.value.toString()

                        Glide.with(context).load(image)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(holder.binding.profile)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors if needed
                    }
                })

            holder.binding.profile.setOnClickListener {
                showImageDialog(context, image!!)
            }
            database.reference.child("groupChats")
                .child(user.uid!!)
                .child("lastMsg")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val lastSms = dataSnapshot.value.toString()
                        holder.binding.tvRole.text = lastSms
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors if needed
                    }
                })
        }




        holder.itemView.setOnClickListener {
            val intent = if (user.isGroup) {
                Intent(context, GroupChatActivity::class.java)
                    .putExtra("isGroupChat", true)
                    .putExtra("GroupName", user.name)
                    .putExtra("GroupUid", user.uid)
                    .putExtra("user_name", user.user_name)
            } else {
                Intent(context, ChatActivity::class.java)
                    .putExtra("name", user.name)
                    .putExtra("image", user.profileImage)
                    .putExtra("uid", user.uid)
                    .putExtra("DeviceToken", user.FcmToken)
            }
            context.startActivity(intent)
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

}

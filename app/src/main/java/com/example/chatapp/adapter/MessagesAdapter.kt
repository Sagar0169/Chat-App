package com.example.chatapp.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.ImageViewerActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.DeleteLayoutBinding
import com.example.chatapp.databinding.ReceiveMsgBinding
import com.example.chatapp.databinding.SendMsgBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var dialog: Dialog? = null
    val senderRoom: String
    var receiverRoom: String

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {

            val view = LayoutInflater.from(context).inflate(
                R.layout.send_msg, parent, false
            )
            SendMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(
                R.layout.receive_msg, parent, false
            )
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        Log.d("FirebaseAuth", FirebaseAuth.getInstance().uid!! + "1")
        Log.d("FirebaseAuth122", message.senderId!! + "2")
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            Log.d("SENT", "2")
//            seen = dateFormat.format(Date(message.timeStamp))
            ITEM_SENT
        } else {
            Log.d("SENT", "1")
            ITEM_RECEIVE
        }
    }


    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder.javaClass == SendMsgHolder::class.java) {
            val viewHolder = holder as SendMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE

                Glide.with(context).load(message.imageUrl)
                    .placeholder(R.drawable.gallery_placeholder)
                    .into(viewHolder.binding.image)
                viewHolder.binding.image.setOnClickListener {
                    val intent = Intent(context, ImageViewerActivity::class.java)
                    intent.putExtra("image_url", message.imageUrl)
                    context.startActivity(intent)
                }
            }
            if (message.seen) {
                viewHolder.binding.timestamp.text = "SEEN"
            } else {
                viewHolder.binding.timestamp.text = dateFormat.format(Date(message.timeStamp))
            }

            viewHolder.binding.message.text = message.message

            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_delete, null)

                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete")
                    .setView(binding.root)
                    .create()

                binding.everyone.visibility = View.VISIBLE
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()


                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE


                Glide.with(context).load(message.imageUrl)
                    .placeholder(R.drawable.gallery_placeholder)
                    .into(viewHolder.binding.image)
                viewHolder.binding.image.setOnClickListener {
                    val intent = Intent(context, ImageViewerActivity::class.java)
                    intent.putExtra("image_url", message.imageUrl)
                    context.startActivity(intent)
                }
            }
            if (!message.seen) {
                message.seen = true
                FirebaseDatabase.getInstance().reference.child("chats")
                    .child(senderRoom)
                    .child("lastMsgUnRead").setValue(0)

                FirebaseDatabase.getInstance().reference.child("chats")
                    .child(senderRoom)
                    .child("lastMsgSeen").setValue(true)

                FirebaseDatabase.getInstance().reference.child("chats")
                    .child(receiverRoom)
                    .child("lastMsgSeen").setValue(true)
                FirebaseDatabase.getInstance().reference.child("chats")
                    .child(receiverRoom)
                    .child("lastMsgUnRead").setValue(0)
                message.messageId?.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(it1).setValue(message)
                }
                message.messageId?.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(it1).setValue(message)
                }
                viewHolder.binding.timestamp.text = dateFormat.format(Date(message.timeStamp))
            }
            viewHolder.binding.message.text = message.message
            viewHolder.binding.timestamp.text = dateFormat.format(Date(message.timeStamp))
//            viewHolder.binding.timestamp.text = dateFormat.format(Date(message.timeStamp))
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_delete, null)

                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete com.example.chatapp.model.com.example.chatapp.Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.visibility = View.GONE
                binding.delete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }

    }


    inner class SendMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
}
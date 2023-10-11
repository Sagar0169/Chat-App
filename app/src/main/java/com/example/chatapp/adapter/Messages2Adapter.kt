package com.example.chatapp.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Message2
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Messages2Adapter(
    val context: Context, private var item: ArrayList<Message2>, var groupId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val item_receive = 1
    val item_sent = 2


//    private var filteredList: List<RecyclerViewDataClass> = originalList.toMutableList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            //inflate receive
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            return ReceiveViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            return SentViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = item[position]
        if (holder.javaClass == SentViewHolder::class.java) {


            val viewHolder = holder as SentViewHolder
            viewHolder.sentMessage.text = currentMessage.text
            viewHolder.timestamp.text = dateFormat.format(Date(currentMessage.timestamp!!))
            viewHolder.messageSentTick.visibility = View.GONE

        } else {
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.sentMessage.text = currentMessage.text
            viewHolder.timestamp.text = dateFormat.format(Date(currentMessage.timestamp!!))
//            if (currentMessage.senderName!=null)
//            {
//                viewHolder.senderName.text = currentMessage.senderName
//            }


        }

    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = item[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderUid)) {
            return item_sent
        } else {
            return item_receive
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.message)
        val messageSentTick = itemView.findViewById<ImageView>(R.id.messageSentTick)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)


    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.message)
        val timestamp = itemView.findViewById<TextView>(R.id.timestamp)
    }

}

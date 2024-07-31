package com.example.chatapp

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class StoryAdapter(private val storyList: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: ImageView = itemView.findViewById(R.id.story_image)
        val storyName: TextView = itemView.findViewById(R.id.story_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
//        holder.storyImage.setImageResource(story.imageResource)
        holder.storyName.text = story.name

        // Set the border color dynamically
        val borderColor = ContextCompat.getColor(holder.itemView.context, story.borderColorResource)
        val layerDrawable = holder.storyImage.background as LayerDrawable
        val borderLayer = layerDrawable.findDrawableByLayerId(R.id.border_layer) as GradientDrawable
        borderLayer.setStroke(6, borderColor)
    }

    override fun getItemCount() = storyList.size
}

data class Story(val imageResource: Int, val name: String, val borderColorResource: Int)

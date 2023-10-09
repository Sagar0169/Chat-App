package com.example.chatapp



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.example.chatapp.databinding.ActivityImageViewerBinding

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("image_url")


        // Load the image into the PhotoView using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(binding.photoView)

        binding.photoView.setOnViewTapListener { _, _, _ ->
            // Finish the activity when the image is tapped
            finish()
        }
    }

}

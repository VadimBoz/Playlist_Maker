package com.vadim.playlistmaker.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.presentation.App

class MediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        app.themeUseCase.getAndApplyTheme()

        enableEdgeToEdge()
        setContentView(R.layout.activity_media)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageUrl = "https://img.freepik.com/free-vector/open-blue-book-white_1308-69339.jpg"
        val imageView = findViewById<ImageView>(R.id.image)

        Glide.with(applicationContext)
            .load(imageUrl)
            .into(imageView)



    }


}
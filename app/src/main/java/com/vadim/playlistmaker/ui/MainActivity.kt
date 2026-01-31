package com.vadim.playlistmaker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.presentation.App

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val settingsBTN = findViewById<Button>(R.id.settings_BTN)
        settingsBTN.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            Log.d("TAG", "onClick: Settings button")
        }

        val clickListenerSearch = object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d("TAG", "onClick: Search button")
                var intent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
        }

        val searchBTN = findViewById<Button>(R.id.search_BTN)
        searchBTN.setOnClickListener(clickListenerSearch)

        val mediaBTN = findViewById<Button>(R.id.media_BTN)
        mediaBTN.setOnClickListener {
            Log.d("TAG", "onClick: Media button")
            var intent = Intent(this@MainActivity, MediaActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
    }
    override fun onRestart() {
        super.onRestart()
    }

}
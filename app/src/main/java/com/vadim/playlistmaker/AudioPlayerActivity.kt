package com.vadim.playlistmaker

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import kotlin.properties.Delegates

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private var track: Track? = null
    private lateinit var trackNameTV: TextView
    private lateinit var artistNameTV: TextView
    private lateinit var albumNameTV: TextView
    private lateinit var yearTV: TextView
    private lateinit var genreTV: TextView
    private lateinit var countryTV: TextView
    private lateinit var durationTV: TextView
    private lateinit var albumCover: ImageView
    private lateinit var albumGroup: Group
    private lateinit var yearGroup: Group
    private lateinit var genreGroup: Group
    private lateinit var countryGroup: Group
    private lateinit var playBTN: ImageButton
    private lateinit var addToPlayListBTN: ImageButton
    private lateinit var addToFavoriteBTN: ImageButton
    private var isPlaying = false
    private var isFavorite = false
    private var isAddedToPlayList = false
    private val KEY_TRACK = "track"
    private val KEY_IS_PLAYING = "is_playing"
    private val KEY_IS_FAVORITE = "is_favorite"
    private val KEY_IS_ADDED = "is_added"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolBar_BTN)
        trackNameTV = findViewById(R.id.tv_track_name)
        artistNameTV = findViewById(R.id.tv_artist_name)
        albumNameTV = findViewById(R.id.tv_album_value)
        yearTV = findViewById(R.id.tv_year_value)
        genreTV = findViewById(R.id.tv_genre_value)
        countryTV = findViewById(R.id.tv_country_value)
        durationTV = findViewById(R.id.tv_duration_value)
        albumCover = findViewById(R.id.album_cover)

        albumGroup = findViewById(R.id.album_group)
        yearGroup = findViewById(R.id.year_group)
        genreGroup = findViewById(R.id.genre_group)
        countryGroup = findViewById(R.id.country_group)

        toolbar.setNavigationOnClickListener { finish() }

        playBTN = findViewById(R.id.btn_play)
        addToPlayListBTN = findViewById(R.id.btn_add)
        addToFavoriteBTN = findViewById(R.id.btn_favorite)

        if (savedInstanceState != null) {
            track = savedInstanceState.getParcelable(KEY_TRACK)
            isPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING, false)
            isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE, false)
            isAddedToPlayList = savedInstanceState.getBoolean(KEY_IS_ADDED, false)
        } else {
            track = intent.getParcelableExtra("track")
        }

        track.let { track ->
            trackNameTV.text = track?.trackName
            artistNameTV.text = track?.artistName

            durationTV.text = track?.trackDuration.toString()
            if (track?.trackDuration == "") durationTV.visibility = Group.GONE
            else durationTV.visibility = Group.VISIBLE

            albumNameTV.text = track?.album
            if (track?.album == "") albumGroup.visibility = Group.GONE
            else albumGroup.visibility = Group.VISIBLE

            yearTV.text = track?.year.toString()
            if (track?.year == 0) yearGroup.visibility = Group.GONE
            else yearGroup.visibility = Group.VISIBLE

            countryTV.text = track?.country
            if (track?.country == "") countryGroup.visibility = Group.GONE
            else countryGroup.visibility = Group.VISIBLE

            genreTV.text = track?.genre
            if (track?.genre == "") genreGroup.visibility = Group.GONE
            else genreGroup.visibility = Group.VISIBLE

        }

        updateButtonsState()

        Glide.with(this)
            .load(track?.artworkUrl100?.artWorkToFullSize())
            .transform(
                CenterCrop(),
                RoundedCorners(this.resources.getDimensionPixelSize(R.dimen.corner_radius_art_work))
            )
            .placeholder(R.drawable.placeholder)
            .into(albumCover)


        addToFavoriteBTN.setOnClickListener {
            if (!isFavorite) {
                addToFavoriteBTN.setImageResource(R.drawable.ico_button_favorite_active)
                isFavorite = true
            } else {
                addToFavoriteBTN.setImageResource(R.drawable.ico_button_favorite_inactive)
                isFavorite = false
            }
        }

        addToPlayListBTN.setOnClickListener {
            if (!isAddedToPlayList) {
                addToPlayListBTN.setImageResource(R.drawable.ico_button_added)
                isAddedToPlayList = true
            } else {
                addToPlayListBTN.setImageResource(R.drawable.ico_button_add)
                isAddedToPlayList = false
            }
        }

        playBTN.setOnClickListener {
            if(!isPlaying) {
                playBTN.setImageResource(R.drawable.ico_button_pause)
                isPlaying = true
            }
            else {
                playBTN.setImageResource(R.drawable.ico_button_play)
                isPlaying = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_TRACK, track)
        outState.putBoolean(KEY_IS_PLAYING, isPlaying)
        outState.putBoolean(KEY_IS_FAVORITE, isFavorite)
        outState.putBoolean(KEY_IS_ADDED, isAddedToPlayList)

    }

    private fun updateButtonsState() {
        val favoriteIcon = if (isFavorite) R.drawable.ico_button_favorite_active
        else R.drawable.ico_button_favorite_inactive
        addToFavoriteBTN.setImageResource(favoriteIcon)

        val addIcon = if (isAddedToPlayList) R.drawable.ico_button_added
        else R.drawable.ico_button_add
        addToPlayListBTN.setImageResource(addIcon)

        val playIcon = if (isPlaying) R.drawable.ico_button_pause
        else R.drawable.ico_button_play
        playBTN.setImageResource(playIcon)
    }

}
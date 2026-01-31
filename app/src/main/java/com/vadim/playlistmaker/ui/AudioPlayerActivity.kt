package com.vadim.playlistmaker.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.domain.repository.ImageLoader
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.presentation.App
import com.vadim.playlistmaker.data.extension.epochTimeToTxt
import java.lang.ref.WeakReference


const val TIMER_UPDATE_DELAY_MS = 300L    // Задержка обновления таймера
const val TRACK_DURATION_MS = 30000L      // Длительность трека

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
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playerState: PlayerState
    private lateinit var mainThreadHandler: Handler
    private lateinit var timerTV: TextView

    private lateinit var activityWeakRef: WeakReference<AudioPlayerActivity>
    private var isFavorite = false
    private var isAddedToPlayList = false
    private var currentTimerRunnable: Runnable? = null
    private var savedPosition: Int = 0
    private var wasPlayingBeforePause: Boolean = false
    private val KEY_WAS_PLAYING_BEFORE_PAUSE = "was_playing_before_pause"
    private val KEY_TRACK = "track"
    private val KEY_IS_FAVORITE = "is_favorite"
    private val KEY_IS_ADDED = "is_added"
    private val KEY_CURRENT_POSITION = "current_position"
    private val KEY_PLAYER_STATE = "player_state"

    private lateinit var imageLoader: ImageLoader

    private enum class PlayerState { PLAYING, PAUSED, PREPARED, DEFAULT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as App
        app.themeUseCase.getAndApplyTheme()

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
        playBTN = findViewById(R.id.btn_play)
        addToPlayListBTN = findViewById(R.id.btn_add)
        addToFavoriteBTN = findViewById(R.id.btn_favorite)
        timerTV = findViewById(R.id.tv_timer)

        activityWeakRef = WeakReference(this)

        track = intent.getParcelableExtra("track")

        toolbar.setNavigationOnClickListener { finish() }
        mediaPlayer = MediaPlayer()
        playerState = PlayerState.DEFAULT
        playBTN.isEnabled = false
        mainThreadHandler = Handler(Looper.getMainLooper())

        imageLoader = app.imageLoader

        preparePlayer()

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
        loadCoverAlbum(track)

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
            playbackControl()
        }
    }

    private fun loadCoverAlbum(track: Track?) {
        if(track == null) return
        imageLoader.loadTrackCoverImage(track) { imageData ->
            imageData.imageBytes?.let { bytes ->
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                albumCover.setImageBitmap(bitmap)
            }
        }
    }

    private fun updateButtonsState() {
        val favoriteIcon = if (isFavorite) R.drawable.ico_button_favorite_active
        else R.drawable.ico_button_favorite_inactive
        addToFavoriteBTN.setImageResource(favoriteIcon)

        val addIcon = if (isAddedToPlayList) R.drawable.ico_button_added
        else R.drawable.ico_button_add
        addToPlayListBTN.setImageResource(addIcon)

        val playIcon = if (playerState == PlayerState.PLAYING)
            R.drawable.ico_button_pause
        else R.drawable.ico_button_play
        playBTN.setImageResource(playIcon)
    }

    private fun startPlayTrack() {
        mediaPlayer.start()
        playBTN.setImageResource(R.drawable.ico_button_pause)
        playerState = PlayerState.PLAYING
        startTimer()
    }

    private fun pausePlayTrack() {
        mediaPlayer.pause()
        playBTN.setImageResource(R.drawable.ico_button_play)
        playerState = PlayerState.PAUSED
        startTimer()
    }

    private fun stopPlayTrack() {
        mediaPlayer.stop()
        playBTN.setImageResource(R.drawable.ico_button_play)
        playerState = PlayerState.PAUSED
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(track?.previewUrl)
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            playBTN.isEnabled = true
            playerState = PlayerState.PREPARED
            if (savedPosition > 0) {
                mediaPlayer.seekTo(savedPosition)
                timerTV.text = savedPosition.toString().epochTimeToTxt()
            }

            if (wasPlayingBeforePause) {
                mainThreadHandler.post {
                    if (!isFinishing && !isDestroyed) {
                        startPlayTrack()
                    }
                }
            }
        }

        mediaPlayer.setOnCompletionListener {
            playBTN.setImageResource(R.drawable.ico_button_play)
            playerState = PlayerState.PREPARED
        }

    }

    private fun playbackControl() {
        when (playerState) {
            PlayerState.PLAYING -> {
                pausePlayTrack()
                playerState = PlayerState.PAUSED
                wasPlayingBeforePause = false
            }

            PlayerState.PREPARED, PlayerState.PAUSED -> {
                startPlayTrack()
                playerState = PlayerState.PLAYING
                wasPlayingBeforePause = true
                startTimer()
            }

            else -> {}
        }
    }

    private fun createTimerRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                val activity = activityWeakRef.get()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    stopTimer()
                    return
                }

                if (!activity::mediaPlayer.isInitialized) {
                    stopTimer()
                    return
                }

                if (mediaPlayer.isPlaying) {
                    val curTime = mediaPlayer.currentPosition
                    if (curTime < TRACK_DURATION_MS - TIMER_UPDATE_DELAY_MS) {
                        timerTV.text = curTime.toString().epochTimeToTxt()
                        mainThreadHandler.postDelayed(this, TIMER_UPDATE_DELAY_MS)
                    } else {
                        timerTV.text = getString(R.string.zero_duration)
                        stopTimer()
                    }

                }
            }
        }
    }

    private fun startTimer() {
        currentTimerRunnable = createTimerRunnable()
        currentTimerRunnable?.let {
            mainThreadHandler.post(it)
        }
    }

    private fun stopTimer() {
        currentTimerRunnable?.let {
            mainThreadHandler.removeCallbacks(it)
        }
        currentTimerRunnable = null
    }

    override fun onPause() {
        super.onPause()
        pausePlayTrack()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        mediaPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_TRACK, track)
        outState.putBoolean(KEY_IS_FAVORITE, isFavorite)
        outState.putBoolean(KEY_IS_ADDED, isAddedToPlayList)
        outState.putInt(KEY_CURRENT_POSITION, mediaPlayer.currentPosition)
        outState.putString(KEY_PLAYER_STATE, playerState.name)
        outState.putBoolean(KEY_WAS_PLAYING_BEFORE_PAUSE, wasPlayingBeforePause)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        track = savedInstanceState.getParcelable(KEY_TRACK)
        isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE, false)
        isAddedToPlayList = savedInstanceState.getBoolean(KEY_IS_ADDED, false)

        savedPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0)
        val savedStateName = savedInstanceState.getString(KEY_PLAYER_STATE)
        wasPlayingBeforePause = savedInstanceState.getBoolean(KEY_WAS_PLAYING_BEFORE_PAUSE, false)
        playerState = try {
            PlayerState.valueOf(savedStateName ?: PlayerState.DEFAULT.name)
        } catch (e: IllegalArgumentException) {
            PlayerState.DEFAULT
        }
    }

}
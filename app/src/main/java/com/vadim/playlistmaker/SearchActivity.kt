package com.vadim.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import kotlin.math.abs

class SearchActivity : AppCompatActivity() {

    private val SEARCH_DEBOUNCE_DELAY = 2000L
    private val CLICK_DEBOUNCE_DELAY = 1000L
    private lateinit var searchEditText: EditText
    private val KEY_SEARCH_TEXT = "search_text"
    private var KEY_CURRENT_STATE = "current_UI_state"
    private lateinit var clearEditTextBTN: ImageView
    private val trackBaseUrl = "https://itunes.apple.com"
    private lateinit var toolBarBTN: MaterialToolbar
    private lateinit var retrofit: Retrofit
    private lateinit var trackApiService: TrackApiService
    private var trackList = emptyList<Track>()
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var tracksHistoryAdapter: TracksAdapter
    private lateinit var trackListRecyclerView: RecyclerView
    private lateinit var trackListHistoryRecyclerView: RecyclerView
    private lateinit var errorConnectionFrame: View
    private lateinit var emptySearchFrame: View
    private lateinit var reloadBTN: MaterialButton
    private lateinit var historySearchFrame: View
    private lateinit var removeAllHistoryBTN: MaterialButton
    private lateinit var searchHistoryManager: SearchHistoryManager
    private lateinit var progressCircularPB: CircularProgressIndicator
    private lateinit var weakRef: WeakReference<SearchActivity>
    private var currentCall: Call<TrackApiResponse>? = null
    private var currentUIState: UIState = UIState.INITIAL

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private var searchText: String? = null
    }

    private enum class UIState {
    INITIAL, RESULTS, EMPTY, ERROR, HISTORY_RESULTS, LOADING_SEARCH_RESULTS
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchHistoryManager = SearchHistoryManager(this)
        searchHistoryManager.loadTrackHistoryFromPref()
        weakRef = WeakReference(this)

        initViews()

        toolBarBTN.setNavigationOnClickListener {
            finish()
        }

        retrofit = Retrofit.Builder()
            .baseUrl(trackBaseUrl)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(Track::class.java, TrackDeserializerAdapter())
                        .create()
                )
            )
            .build()

        trackApiService = retrofit.create(TrackApiService::class.java)

        searchEditText.addTextChangedListener(createSearchTextWatcher())

        clearEditTextBTN.updateVisibilityImage(searchEditText.text)

        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus
                && searchEditText.text.isNullOrEmpty()
                && searchHistoryManager.trackListHistory.isNotEmpty()
            ) {
                updateUIState(UIState.HISTORY_RESULTS)
            }
        }

        clearEditTextBTN.setOnClickListener {
            searchEditText.setText("")
            searchText = ""
            clearEditTextBTN.updateVisibilityImage("")
            trackList = emptyList()
            tracksAdapter.updateTracks(emptyList())
            updateUIState(UIState.INITIAL)
            hideKeyboard()
            handler.removeCallbacks(searchRunnable)
        }

        reloadBTN.setOnClickListener {
            handler.removeCallbacks(searchRunnable)
            searchRunnable.run()
        }

        tracksAdapter = TracksAdapter(trackList) { track ->
            if (!clickDebounce()) return@TracksAdapter
            searchHistoryManager.addTrackToHistory(track)
            Toast.makeText(
                this,
                "трек ${track.trackName} добавлен в список",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("track", track)
            startActivity(intent)
        }

        trackListRecyclerView.adapter = tracksAdapter
        trackListRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        hideKeyboard()
                    }
                }
            }
        )

        tracksHistoryAdapter = TracksAdapter(searchHistoryManager.trackListHistory) { track ->
            if (!clickDebounce()) return@TracksAdapter
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("track", track)
            startActivity(intent)
        }
        trackListHistoryRecyclerView.adapter = tracksHistoryAdapter

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                searchRunnable.run()
                hideKeyboard()
                true

            } else false
        }

        removeAllHistoryBTN.setOnClickListener {
            searchHistoryManager.clearTrackHistory()
            tracksHistoryAdapter.updateTracks(emptyList())
            updateUIState(UIState.INITIAL)
        }

        updateUIState(currentUIState)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(searchRunnable)
        searchHistoryManager.saveTrackHistory()
    }

    private fun performSearch() {
        val query = searchText?.cleanText() ?: return
        updateUIState(UIState.LOADING_SEARCH_RESULTS)

        currentCall?.cancel()
        currentCall = trackApiService.getTracks(query)
        currentCall?.enqueue(object : Callback<TrackApiResponse> {
            override fun onResponse(
                call: Call<TrackApiResponse>,
                response: Response<TrackApiResponse>
            ) {
                val activity: SearchActivity? = weakRef.get()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return
                }

                if (response.isSuccessful) {
                    activity.trackList = response.body()?.tracksList ?: emptyList()
                    activity.tracksAdapter.updateTracks(activity.trackList)

                    if (BuildConfig.DEBUG) {
                        val emptyFieldsCount = activity.trackList.sumOf { track ->
                            track.javaClass.declaredFields.count { field ->
                                field.isAccessible = true
                                val value = field.get(track)
                                value == null ||
                                        (value is String && (value.isEmpty() || value == "0")) ||
                                        (value is Number && abs(value.toDouble()) < 1e-4 )
                            }
                        }
                        Log.d("SearchActivity", "Количество пустых полей : $emptyFieldsCount")
                    }

                    if (activity.trackList.isEmpty()) {
                        activity.updateUIState(UIState.EMPTY)
                    } else {
                        activity.updateUIState(UIState.RESULTS)
                    }
                } else {
                    activity.updateUIState(UIState.ERROR)
                    activity.trackList = emptyList()
                    activity.tracksAdapter.updateTracks(trackList)
                }
            }

            override fun onFailure(call: Call<TrackApiResponse>, t: Throwable) {
                val activity = weakRef.get()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return
                }
                activity.updateUIState(UIState.ERROR)
                activity.trackList = emptyList()
                activity.tracksAdapter.updateTracks(emptyList())
            }
        })
    }

    private fun createSearchTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(searchRunnable)
            }

            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                clearEditTextBTN.updateVisibilityImage(searchText)
                handler.removeCallbacks(searchRunnable)

                if (searchText.isNullOrEmpty()) {
                    updateUIState(
                        if (searchEditText.hasFocus() && searchHistoryManager.trackListHistory.isNotEmpty())
                            UIState.HISTORY_RESULTS
                        else
                            UIState.INITIAL
                    )
                    trackList = emptyList()
                    tracksAdapter.updateTracks(emptyList())
                } else {
                    updateUIState(UIState.INITIAL)
                    if (searchText!!.length > 2) {
                        searchDebounce()
                    }
                }
            }
        }
    }

    private val searchRunnable = Runnable {
        val activity = weakRef.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return@Runnable
        }
        performSearch()
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        if (!searchText.isNullOrEmpty() && searchText!!.length > 2) {
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun ImageView.updateVisibilityImage(text: CharSequence?) {
        visibility = if (text.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
        outState.putString(KEY_CURRENT_STATE, currentUIState.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString(KEY_SEARCH_TEXT)?.let { savedText ->
            searchText = savedText
            searchEditText.setText(savedText)
            clearEditTextBTN.updateVisibilityImage(searchText)
            searchEditText.setSelection(savedText.length)
        }
        val savedStateName = savedInstanceState.getString(KEY_CURRENT_STATE)
        currentUIState = try {
            UIState.valueOf(savedStateName ?: UIState.INITIAL.name)
        } catch (e: IllegalArgumentException) {
            UIState.INITIAL
        }
        updateUIState(currentUIState)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearEditTextBTN = findViewById(R.id.clear_BTN)
        trackListRecyclerView = findViewById(R.id.trackList_RV)
        errorConnectionFrame = findViewById(R.id.frame_error_connection)
        emptySearchFrame = findViewById(R.id.frame_empty_search_result)
        reloadBTN = findViewById(R.id.reload_BTN)
        historySearchFrame = findViewById(R.id.frame_history_search)
        removeAllHistoryBTN = findViewById(R.id.remove_history_BTN)
        trackListHistoryRecyclerView = findViewById(R.id.savedTracks_RV)
        toolBarBTN = findViewById<MaterialToolbar>(R.id.toolBarBack_BTN)
        progressCircularPB = findViewById(R.id.progress_circular)
    }

    private fun updateUIState(state: UIState) {
        if (currentUIState == state) {
            searchEditText.post {
                if (!searchEditText.hasFocus()) {
                    searchEditText.requestFocus()
                }
            }
            return
        }
        currentUIState = state

        trackListRecyclerView.visibility = View.GONE
        emptySearchFrame.visibility = View.GONE
        errorConnectionFrame.visibility = View.GONE
        historySearchFrame.visibility = View.GONE
        progressCircularPB.visibility = View.GONE

        when (state) {
            UIState.ERROR -> {
                errorConnectionFrame.visibility = View.VISIBLE
            }

            UIState.EMPTY -> {
                emptySearchFrame.visibility = View.VISIBLE
            }

            UIState.RESULTS -> {
                trackListRecyclerView.visibility = View.VISIBLE
            }

            UIState.INITIAL -> {
                searchEditText.post{ searchEditText.requestFocus() }
            }

            UIState.HISTORY_RESULTS -> {
                historySearchFrame.visibility = View.VISIBLE
                searchEditText.requestFocus()
                tracksHistoryAdapter.updateTracks(searchHistoryManager.trackListHistory)
            }

            UIState.LOADING_SEARCH_RESULTS -> {
                progressCircularPB.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        currentCall?.cancel()
        currentCall = null

        searchHistoryManager.saveTrackHistory()
    }


}


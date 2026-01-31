package com.vadim.playlistmaker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
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
import com.vadim.playlistmaker.BuildConfig
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.state.TrackSearchResult
import com.vadim.playlistmaker.domain.useCase.TrackHistoryUseCase
import com.vadim.playlistmaker.domain.useCase.TrackSearchUseCase
import com.vadim.playlistmaker.presentation.App
import com.vadim.playlistmaker.presentation.TracksAdapter

class SearchActivity : AppCompatActivity() {

    // UI Components
    private lateinit var searchEditText: EditText
    private lateinit var clearEditTextBTN: ImageView
    private lateinit var toolBarBTN: MaterialToolbar
    private lateinit var trackListRecyclerView: RecyclerView
    private lateinit var trackListHistoryRecyclerView: RecyclerView
    private lateinit var errorConnectionFrame: View
    private lateinit var emptySearchFrame: View
    private lateinit var reloadBTN: MaterialButton
    private lateinit var historySearchFrame: View
    private lateinit var removeAllHistoryBTN: MaterialButton
    private lateinit var progressCircularPB: CircularProgressIndicator

    // Domain layer dependencies
    private lateinit var trackSearchUseCase: TrackSearchUseCase
    private lateinit var trackHistoryUseCase: TrackHistoryUseCase

    // UI State
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var tracksHistoryAdapter: TracksAdapter
    private var trackList = emptyList<Track>()
    private var searchHistory = emptyList<Track>()

    // Threading
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var backgroundHandler: Handler
    private lateinit var handlerThread: HandlerThread

    // Search state
    private var currentSearchQuery: String? = null
    private var isSearchFocused = false
    private var isClickAllowed = true
    private var searchRunnable: Runnable? = null

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val KEY_SEARCH_TEXT = "search_text"
    }

    private enum class UIState {
        INITIAL, RESULTS, EMPTY, ERROR, HISTORY_RESULTS, LOADING_SEARCH_RESULTS
    }

    private lateinit var app: App
    private var currentUIState: UIState = UIState.INITIAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlerThread = HandlerThread("SearchBackgroundThread").apply {
            start()
        }
        backgroundHandler = Handler(handlerThread.looper)

        app = application as App
        app.themeUseCase.getAndApplyTheme()

        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initDependencies()
        initViews()
        setupUI()
        setupListeners()
        restoreState(savedInstanceState)
        loadSearchHistory()
        updateUIState(UIState.INITIAL)
    }

    private fun initDependencies() {
        trackSearchUseCase = app.trackSearchUseCase
        trackHistoryUseCase = app.trackHistoryUseCase
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

    private fun setupUI() {
        toolBarBTN.setNavigationOnClickListener { finish() }

        tracksAdapter = TracksAdapter(trackList, app.imageLoader) { track ->
            onTrackClicked(track)
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

        tracksHistoryAdapter = TracksAdapter(searchHistory, app.imageLoader) { track ->
            if (clickDebounce()) {
                navigateToAudioPlayer(track)
            }
        }
        trackListHistoryRecyclerView.adapter = tracksHistoryAdapter
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(createSearchTextWatcher())

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            isSearchFocused = hasFocus
            if (hasFocus && searchEditText.text.isNullOrEmpty() && searchHistory.isNotEmpty()) {
                updateUIState(UIState.HISTORY_RESULTS)
            }
        }

        clearEditTextBTN.setOnClickListener {
            clearSearch()
        }

        reloadBTN.setOnClickListener {
            performSearch()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cancelPendingSearch()
                performSearch()
                hideKeyboard()
                true
            } else false
        }

        removeAllHistoryBTN.setOnClickListener {
            clearHistory()
        }
    }

    private fun createSearchTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cancelPendingSearch()
            }

            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString()
                clearEditTextBTN.updateVisibilityImage(s)
                cancelPendingSearch()

                if (s.isNullOrEmpty()) {
                    if (shouldShowHistory()) {
                        updateUIState(UIState.HISTORY_RESULTS)
                    } else {
                        updateUIState(UIState.INITIAL)
                    }
                    trackList = emptyList()
                    tracksAdapter.updateTracks(emptyList())
                } else {
                    updateUIState(UIState.INITIAL)
                    if (s.length > 2) {
                        scheduleSearch()
                    }
                }
            }
        }
    }

    private fun scheduleSearch() {
        searchRunnable = Runnable {
            performSearch()
        }
        searchRunnable?.let {
            backgroundHandler.postDelayed(it, SEARCH_DEBOUNCE_DELAY)
        }
    }

    private fun cancelPendingSearch() {
        searchRunnable?.let {
            backgroundHandler.removeCallbacks(it)
        }
    }

    private fun performSearch() {
        val query = currentSearchQuery ?: return

        mainHandler.post {
            updateUIState(UIState.LOADING_SEARCH_RESULTS)
        }

        trackSearchUseCase.cancel()

        backgroundHandler.post {
            val result = trackSearchUseCase.search(query)

            mainHandler.post {
                handleSearchResult(result)
            }
        }
    }

    private fun handleSearchResult(result: TrackSearchResult) {
        when (result) {
            is TrackSearchResult.Success -> {
                trackList = result.tracks
                tracksAdapter.updateTracks(result.tracks)

                if (BuildConfig.DEBUG) {
                    Log.d("SearchActivity", "Найдено треков: ${result.tracks.size}")
                }

                updateUIState(
                    if (trackList.isEmpty()) UIState.EMPTY else UIState.RESULTS
                )
            }
            is TrackSearchResult.Empty -> {
                trackList = emptyList()
                tracksAdapter.updateTracks(emptyList())
                updateUIState(UIState.EMPTY)
            }
            is TrackSearchResult.Error -> {
                trackList = emptyList()
                tracksAdapter.updateTracks(emptyList())
                updateUIState(UIState.ERROR)
            }
            is TrackSearchResult.EmptyQuery -> {
                updateUIState(UIState.INITIAL)
            }
        }
    }

    private fun onTrackClicked(track: Track) {
        if (!clickDebounce()) return

        trackHistoryUseCase.addTrackToHistory(track) { success ->
            mainHandler.post {
                if (success) {
                    loadSearchHistory()

                    Toast.makeText(
                        this@SearchActivity,
                        "Трек ${track.trackName} добавлен в список",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToAudioPlayer(track)
                } else {
                    Toast.makeText(
                        this@SearchActivity,
                        "Не удалось добавить трек в историю",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("track", track)
        startActivity(intent)
    }

    private fun clearSearch() {
        searchEditText.setText("")
        currentSearchQuery = ""
        clearEditTextBTN.updateVisibilityImage("")
        trackList = emptyList()
        tracksAdapter.updateTracks(emptyList())
        updateUIState(UIState.INITIAL)
        hideKeyboard()
        cancelPendingSearch()
    }

    private fun clearHistory() {
        trackHistoryUseCase.clearHistory { success ->
            mainHandler.post {
                if (success) {
                    searchHistory = emptyList()
                    tracksHistoryAdapter.updateTracks(emptyList())
                    updateUIState(UIState.INITIAL)

                    Toast.makeText(
                        this@SearchActivity,
                        "История поиска очищена",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@SearchActivity,
                        "Не удалось очистить историю",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadSearchHistory() {
        trackHistoryUseCase.getHistory { history ->
            mainHandler.post {
                searchHistory = history
                tracksHistoryAdapter.updateTracks(history)

                if (shouldShowHistory()) {
                    updateUIState(UIState.HISTORY_RESULTS)
                }
            }
        }
    }

    private fun shouldShowHistory(): Boolean {
        return isSearchFocused && searchEditText.text.isNullOrEmpty() && searchHistory.isNotEmpty()
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
            UIState.ERROR -> errorConnectionFrame.visibility = View.VISIBLE
            UIState.EMPTY -> emptySearchFrame.visibility = View.VISIBLE
            UIState.RESULTS -> trackListRecyclerView.visibility = View.VISIBLE
            UIState.INITIAL -> {
                mainHandler.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        searchEditText.requestFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                }, 2000)
            }
            UIState.HISTORY_RESULTS -> {
                historySearchFrame.visibility = View.VISIBLE
                searchEditText.requestFocus()
                tracksHistoryAdapter.updateTracks(searchHistory)
            }
            UIState.LOADING_SEARCH_RESULTS -> progressCircularPB.visibility = View.VISIBLE
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
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

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, currentSearchQuery)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.getString(KEY_SEARCH_TEXT)?.let { savedText ->
            currentSearchQuery = savedText
            searchEditText.setText(savedText)
            clearEditTextBTN.updateVisibilityImage(savedText)
            searchEditText.setSelection(savedText.length)

            if (savedText.isNotEmpty() && savedText.length > 2) {
                mainHandler.post {
                    performSearch()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cancelPendingSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем все операции
        backgroundHandler.removeCallbacksAndMessages(null)
        mainHandler.removeCallbacksAndMessages(null)
        trackSearchUseCase.cancel()
        handlerThread.quitSafely()
    }
}
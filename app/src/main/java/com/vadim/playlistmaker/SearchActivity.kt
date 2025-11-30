package com.vadim.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

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

    private var currentUIState: UIState = UIState.INITIAL

    companion object {
        private var searchText: String? = null
    }

    private enum class UIState {
        INITIAL, RESULTS, EMPTY, ERROR, HISTORY_RESULTS
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

        initViews()

        toolBarBTN.setNavigationOnClickListener {
            finish()
        }

        retrofit = Retrofit.Builder()
            .baseUrl(trackBaseUrl)
            .addConverterFactory(GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(Track::class.java, TrackDeserializerAdapter())
                        .create()
                )
            )
            .build()

        trackApiService = retrofit.create(TrackApiService::class.java)

        searchEditText.addTextChangedListener(createSearchTextWatcher())

        clearEditTextBTN.updateVisibilityImage(searchEditText.text)

        searchEditText.setOnFocusChangeListener  { view, hasFocus ->
            if (hasFocus
                && searchEditText.text.isNullOrEmpty()
                && searchHistoryManager.trackListHistory.isNotEmpty()
                ) {
                currentUIState = UIState.HISTORY_RESULTS
                updateUIState()
            }
        }

        clearEditTextBTN.setOnClickListener {
            searchEditText.setText("")
            searchText = ""
            clearEditTextBTN.updateVisibilityImage("")
            trackList = emptyList()
            tracksAdapter.updateTracks(emptyList())
            currentUIState = UIState.INITIAL
            updateUIState()
            hideKeyboard()
        }

        reloadBTN.setOnClickListener {
            performSearch()
        }

        tracksAdapter = TracksAdapter(trackList) { track ->
            searchHistoryManager.addTrackToHistory(track)
            Toast.makeText(
                this,
                "трек ${track.trackName} добавлен в список",
                Toast.LENGTH_SHORT
            ).show()
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

        tracksHistoryAdapter = TracksAdapter(searchHistoryManager.trackListHistory) {}
        trackListHistoryRecyclerView.adapter = tracksHistoryAdapter

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                hideKeyboard()
                true

            } else false
        }

        removeAllHistoryBTN.setOnClickListener {
            searchHistoryManager.clearTrackHistory()
            tracksHistoryAdapter.updateTracks(emptyList())
            currentUIState = UIState.INITIAL
            updateUIState()
        }
        updateUIState()
    }

    override fun onPause() {
        super.onPause()
        searchHistoryManager.saveTrackHistory()
    }

    private fun performSearch() {
        val query = searchText?.cleanText() ?: return

        trackApiService.getTracks(query).enqueue(object : Callback<TrackApiResponse> {
            override fun onResponse(
                call: Call<TrackApiResponse>,
                response: Response<TrackApiResponse>
            ) {
                if (response.isSuccessful) {
                    trackList = response.body()?.tracksList ?: emptyList()
                    tracksAdapter.updateTracks(trackList)

                    if (trackList.isEmpty()) {
                        currentUIState = UIState.EMPTY
                        updateUIState()
                    } else {
                        currentUIState = UIState.RESULTS
                        updateUIState()
                    }
                } else {
                    currentUIState = UIState.ERROR
                    updateUIState()
                    trackList = emptyList()
                    tracksAdapter.updateTracks(trackList)
                }
            }

            override fun onFailure(call: Call<TrackApiResponse>, t: Throwable) {
                currentUIState = UIState.ERROR
                updateUIState()
                trackList = emptyList()
                tracksAdapter.updateTracks(emptyList())
            }
        })
    }

    private fun createSearchTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (searchEditText.hasFocus()
                    && s.isNullOrEmpty()
                    && searchHistoryManager.trackListHistory.isNotEmpty()
                    ) {
                    currentUIState = UIState.HISTORY_RESULTS
                    updateUIState()
                } else {
                    currentUIState = UIState.INITIAL
                    updateUIState()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                clearEditTextBTN.updateVisibilityImage(searchText)
            }
        }
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
        updateUIState()
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
    }

    private fun updateUIState() {
        trackListRecyclerView.visibility = View.GONE
        emptySearchFrame.visibility = View.GONE
        errorConnectionFrame.visibility = View.GONE
        historySearchFrame.visibility = View.GONE

        when (currentUIState) {
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
                searchEditText.postDelayed({
                    searchEditText.requestFocus()
                }, 1000)
            }
            UIState.HISTORY_RESULTS -> {
                historySearchFrame.visibility = View.VISIBLE
                tracksHistoryAdapter.updateTracks(searchHistoryManager.trackListHistory)
            }
        }
    }


}


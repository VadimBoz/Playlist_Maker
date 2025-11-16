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
    private val KEY_TRACK_LIST = "track_list"
    private var KEY_CURRENT_STATE = "current_UI_state"
    private lateinit var clearBTN: ImageView
    private val trackBaseUrl = "https://itunes.apple.com"
    private lateinit var retrofit: Retrofit
    private lateinit var trackApiService: TrackApiService
    private var trackList = emptyList<Track>()
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorConnectionFrame: View
    private lateinit var emptySearchFrame: View
    private lateinit var reloadBTN: MaterialButton
    private var currentUIState: UIState = UIState.INITIAL

    companion object {
        private var searchText: String? = null
    }

    private enum class UIState {
        INITIAL, RESULTS, EMPTY, ERROR
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

        val toolBarBTN = findViewById<MaterialToolbar>(R.id.toolBarBack_BTN)
        toolBarBTN.setNavigationOnClickListener {
            finish()
        }

        initViews()

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

        searchEditText = findViewById(R.id.searchEditText)
        clearBTN = findViewById<ImageView>(R.id.clear_BTN)
        searchEditText.addTextChangedListener(createSearchTextWatcher())
        clearBTN.updateVisibility(searchEditText.text)

        clearBTN.setOnClickListener {
            searchEditText.setText("")
            searchText = ""
            clearBTN.updateVisibility("")
            trackList = emptyList()
            tracksAdapter.updateTracks(emptyList())
            currentUIState = UIState.INITIAL
            updateUIState()
            hideKeyboard()
        }

        reloadBTN.setOnClickListener {
            performSearch()
        }

        val trackListRecyclerView = findViewById<RecyclerView>(R.id.trackList_RV)
        tracksAdapter = TracksAdapter(trackList)
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

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                hideKeyboard()
                true

            } else false
        }

        showInitialState()
    }

    private fun performSearch() {
        val query = searchText ?: return

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
                        Toast.makeText(
                            this@SearchActivity,
                            "Найдено ${trackList.size} треков",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    currentUIState = UIState.ERROR
                    updateUIState()
                    Toast.makeText(
                        this@SearchActivity,
                        "Ошибка сервера: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    trackList = emptyList()
                    tracksAdapter.updateTracks(trackList)
                }
            }

            override fun onFailure(call: Call<TrackApiResponse>, t: Throwable) {
                currentUIState = UIState.ERROR
                updateUIState()
                Toast.makeText(this@SearchActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                t.printStackTrace()
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
            }

            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                clearBTN.updateVisibility(searchText)
            }
        }
    }

    private fun ImageView.updateVisibility(text: CharSequence?) {
        visibility = if (text.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
        outState.putParcelableArrayList(KEY_TRACK_LIST, ArrayList(trackList))
        outState.putString(KEY_CURRENT_STATE, currentUIState.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString(KEY_SEARCH_TEXT)?.let { savedText ->
            searchText = savedText
            searchEditText.setText(savedText)
            clearBTN.updateVisibility(searchText)
            searchEditText.setSelection(savedText.length)
        }
        val savedTracks = savedInstanceState.getParcelableArrayList<Track>(KEY_TRACK_LIST)
        trackList = savedTracks ?: emptyList()
        tracksAdapter.updateTracks(trackList)
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


    private fun showInitialState() {
        recyclerView.visibility = View.GONE
        emptySearchFrame.visibility = View.GONE
        errorConnectionFrame.visibility = View.GONE
    }

    private fun showResultsState() {
        recyclerView.visibility = View.VISIBLE
        emptySearchFrame.visibility = View.GONE
        errorConnectionFrame.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptySearchFrame.visibility = View.VISIBLE
        errorConnectionFrame.visibility = View.GONE
    }

    private fun showErrorState() {
        recyclerView.visibility = View.GONE
        emptySearchFrame.visibility = View.GONE
        errorConnectionFrame.visibility = View.VISIBLE
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearBTN = findViewById(R.id.clear_BTN)
        recyclerView = findViewById(R.id.trackList_RV)
        errorConnectionFrame = findViewById(R.id.frame_error_connection)
        emptySearchFrame = findViewById(R.id.frame_empty_search_result)
        reloadBTN = findViewById(R.id.reload_BTN)
    }

    private fun updateUIState() {
        when (currentUIState) {
            UIState.ERROR -> showErrorState()
            UIState.EMPTY -> showEmptyState()
            UIState.RESULTS -> showResultsState()
            UIState.INITIAL -> showInitialState()
            else -> showInitialState()
        }
    }
}


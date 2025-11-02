package com.vadim.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private val KEY_SEARCH_TEXT = "search_text"
    private lateinit var clearBTN: ImageView

    companion object {
        private var searchText: String? = null
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

        searchEditText = findViewById(R.id.searchEditText)
        clearBTN = findViewById<ImageView>(R.id.clear_BTN)
        searchEditText.addTextChangedListener(createSearchTextWatcher())
        clearBTN.updateVisibility(searchEditText.text)

        clearBTN.setOnClickListener {
            searchEditText.setText("")
            searchText = ""
            clearBTN.updateVisibility("")
            hideKeyboard()
        }

        searchEditText.setOnClickListener {
            Toast.makeText(this, "Search text - $searchText", Toast.LENGTH_SHORT).show()
        }

        val trackListRecyclerView = findViewById<RecyclerView>(R.id.trackList_RV)
        val trackAdapter = TrackAdapter(getTrackList())
        trackListRecyclerView.adapter = trackAdapter
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString(KEY_SEARCH_TEXT)?.let { savedText ->
            searchText = savedText
            searchEditText.setText(savedText)
            clearBTN.updateVisibility(searchText)
            searchEditText.setSelection(savedText.length)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

}


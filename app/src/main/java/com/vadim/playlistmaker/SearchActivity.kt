package com.vadim.playlistmaker

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.properties.Delegates

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private val KEY_SEARCH_TEXT = "search_text"
    private var searchText: String? = null

    private lateinit var searchIcon: Drawable
    private lateinit var clearIcon: Drawable
    private var startIconSize by Delegates.notNull<Int>()
    private var endIconSize by Delegates.notNull<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolBarBTN = findViewById<TextView>(R.id.toolBar_BTN)
        toolBarBTN.setOnClickListener {
            finish()
        }

        searchEditText = findViewById(R.id.searchEditText)
        initializeIcons()
        searchEditText.addTextChangedListener(createSearchTextWatcher())
        updateClearIconVisibility(searchEditText.text)

        searchEditText.setOnClickListener {
            Toast.makeText(this, "Search text - $searchText", Toast.LENGTH_SHORT).show()
        }

        searchEditText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = (view as EditText).compoundDrawables[2]
                if (drawableEnd != null) {
                    val iconClickArea = view.width - view.paddingEnd - endIconSize
                    if (event.x >= iconClickArea) {
                        view.performClick()
                        view.text.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

    }

    private fun initializeIcons() {
        searchIcon = ContextCompat.getDrawable(this, R.drawable.search_ico)!!
        clearIcon = ContextCompat.getDrawable(this, R.drawable.clear_ico)?.apply {
            setTint(ContextCompat.getColor(this@SearchActivity, R.color.grey_light))
        }!!
        startIconSize = resources.getDimension(R.dimen.icon_size_small).toInt()
        endIconSize = resources.getDimension(R.dimen.icon_size_small).toInt()
    }

    private fun createSearchTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                searchText = s.toString()
                updateClearIconVisibility(s)
            }
        }

    }

    private fun updateClearIconVisibility(text: CharSequence?) {
        val clearIcon = if (text.isNullOrEmpty()) {
            null
        } else {
            this@SearchActivity.clearIcon
        }

        searchEditText.setCompoundDrawablesRelative(
            searchIcon,
            null,
            clearIcon,
            null
        )
        searchEditText.setupIconSizes(startIconSize, endIconSize)
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
            updateClearIconVisibility(savedText)
            searchEditText.setSelection(savedText.length)
        }
    }

    fun EditText.setupIconSizes(sizeStartIcon: Int, sizeEndIcon: Int) {
        val drawables = this.compoundDrawablesRelative
        val startDrawable: Drawable? = drawables[0]
        val endDrawable: Drawable? = drawables[2]
        startDrawable?.setBounds(0, 0, sizeStartIcon, sizeStartIcon)

        endDrawable?.setBounds(0, 0, sizeEndIcon, sizeEndIcon)
        this.setCompoundDrawablesRelative(
            startDrawable,
            drawables[1],
            endDrawable,
            drawables[3]
        )
    }

}


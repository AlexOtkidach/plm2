package com.example.plm2

import Track
import TrackAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.internal.ViewUtils.hideKeyboard

class SearchActivity : AppCompatActivity() {

    private lateinit var searchQuery: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView // Добавляем переменную

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Включаем кнопку "назад" (стрелку)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Устанавливаем слушатель для кнопки
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val linearLayout = findViewById<FrameLayout>(R.id.seachBarLine)
        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
        val clearButton = findViewById<ImageView>(R.id.seachBarLineImageV)

        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard(inputEditText) // Скрываем клавиатуру после очистки поля
        }

        // Создаем адаптер
        trackAdapter = TrackAdapter(createTrackList())

        // Инициализируем RecyclerView
        recyclerView = findViewById(R.id.recyclerView)

        // Скрываем RecyclerView до начала ввода текста
        recyclerView.visibility = View.GONE

        // Настраиваем RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                searchQuery = s.toString() // Обновляем searchQuery при изменении текста
                trackAdapter.filter(searchQuery) // Передаем запрос в адаптер

                // Строка для логгирования, чтобы видеть текст в логах из текстового поля
                Log.d("SearchActivity", "onTextChanged: $s")

                // Если есть результаты фильтрации, делаем RecyclerView видимым,
                // в противном случае возвращаем его к начальному состоянию
                recyclerView.visibility = if (s.isNullOrBlank() || trackAdapter.itemCount > 0) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    // переопределяем метод onSaveInstanceState, чтобы сохранить значение searchQuery в Bundle
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("search_query", searchQuery)
        super.onSaveInstanceState(outState)
    }

    // переопределяем метод onRestoreInstanceState, чтобы извлечь данные из Bundle и установить их в EditText
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredQuery = savedInstanceState.getString("search_query")
        if (restoredQuery != null) {
            searchQuery = restoredQuery
            val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
            inputEditText.setText(searchQuery)
        }
    }

    private fun createTrackList(): List<Track> {
        return listOf(
            Track(
                "Smells Like Teen Spirit",
                "Nirvana",
                "5:01",
                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Billie Jean",
                "Michael Jackson",
                "4:35",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                "Stayin' Alive",
                "Bee Gees",
                "4:10",
                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Whole Lotta Love",
                "Led Zeppelin",
                "5:33",
                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                "Sweet Child O'Mine",
                "Guns N' Roses",
                "5:03",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )
        )
    }
}

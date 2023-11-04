package com.example.plm2

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.internal.ViewUtils.hideKeyboard
import androidx.appcompat.app.AppCompatDelegate


class SearchActivity : AppCompatActivity() {

    private lateinit var searchQuery: String

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
        val simpleTextWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                searchQuery = s.toString() // Обновляем searchQuery при изменении текста
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
}

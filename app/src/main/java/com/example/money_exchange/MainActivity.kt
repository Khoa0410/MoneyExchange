package com.example.money_exchange

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sourceEditText: EditText
    private lateinit var targetEditText: EditText
    private lateinit var sourceSpinner: Spinner
    private lateinit var targetSpinner: Spinner

    private var sourceCurrency = "USD"
    private var targetCurrency = "VND"
    private var isUpdating = false  // Cờ ngăn chặn cập nhật đệ quy
    private var lastEditedSource = true  // Cờ để xác định EditText nào vừa được thay đổi lần cuối

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ các thành phần giao diện
        sourceEditText = findViewById(R.id.sourceEditText)
        targetEditText = findViewById(R.id.targetEditText)
        sourceSpinner = findViewById(R.id.sourceSpinner)
        targetSpinner = findViewById(R.id.targetSpinner)

        // Thiết lập Spinner với danh sách loại tiền tệ
        val currencies = arrayOf("USD", "VND", "EUR", "JPY")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceSpinner.adapter = adapter
        targetSpinner.adapter = adapter

        // Thiết lập sự kiện thay đổi cho các Spinner
        sourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                sourceCurrency = currencies[position]
                convertCurrency(isSourceToTarget = lastEditedSource)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                targetCurrency = currencies[position]
                convertCurrency(isSourceToTarget = lastEditedSource)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Thiết lập sự kiện thay đổi cho các EditText
        sourceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) {
                    lastEditedSource = true
                    convertCurrency(isSourceToTarget = true)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        targetEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) {
                    lastEditedSource = false
                    convertCurrency(isSourceToTarget = false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun convertCurrency(isSourceToTarget: Boolean) {
        if (isUpdating) return  // Ngăn chặn vòng lặp đệ quy

        isUpdating = true  // Đặt cờ cập nhật để tránh đệ quy

        // Lấy tỷ giá chuyển đổi thực tế
        val exchangeRate = getExchangeRate(sourceCurrency, targetCurrency)

        if (isSourceToTarget) {
            // Chuyển đổi từ sourceEditText sang targetEditText
            val amount = sourceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val result = amount * exchangeRate
            targetEditText.setText(result.toString())
        } else {
            // Chuyển đổi từ targetEditText sang sourceEditText
            val amount = targetEditText.text.toString().toDoubleOrNull() ?: 0.0
            val result = amount / exchangeRate
            sourceEditText.setText(result.toString())
        }

        isUpdating = false  // Hủy cờ sau khi cập nhật
    }

    private fun getExchangeRate(source: String, target: String): Double {
        // Bảng tỷ giá tạm thời
        return when (source to target) {
            "USD" to "VND" -> 23000.0
            "VND" to "USD" -> 1 / 23000.0
            "USD" to "EUR" -> 0.85
            "EUR" to "USD" -> 1 / 0.85
            "USD" to "JPY" -> 110.0
            "JPY" to "USD" -> 1 / 110.0
            "VND" to "EUR" -> 0.85 / 23000.0
            "EUR" to "VND" -> 23000.0 / 0.85
            "VND" to "JPY" -> 110.0 / 23000.0
            "JPY" to "VND" -> 23000.0 / 110.0
            "EUR" to "JPY" -> 110.0 / 0.85
            "JPY" to "EUR" -> 0.85 / 110.0
            // Tỷ giá mặc định nếu không tìm thấy
            else -> 1.0
        }
    }
}

package com.example.sam_pak_springbreakchooser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.Exception
import java.util.Locale

class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    val languageList = arrayOf<String>("English","한국어 (Korean)","日本語 (Japanese)")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val languageSelector = findViewById<Spinner>(R.id.languageSelector)
        languageSelector.onItemSelectedListener = this
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any>(this, android.R.layout.simple_spinner_item,languageList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSelector.adapter = arrayAdapter

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        //Log.d("Spinner",position.toString())
        val languageSelector = findViewById<Spinner>(R.id.languageSelector)
        try {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            when (position) {
                0 -> speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                1 -> speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko")
                2 -> speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja")
            }

            result.launch(speechIntent)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val editText = findViewById<EditText>(R.id.textBox)
            editText.setText(results[0])
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
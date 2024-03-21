package com.example.sam_pak_springbreakchooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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
import java.util.Objects
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnItemSelectedListener, TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private val languageList = arrayOf<String>("English","한국어 (Korean)","日本語 (Japanese)")
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var languageSetting = 0
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
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        textToSpeech = TextToSpeech(this,this)
    }
    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta


            if (acceleration > 12) {
                //Log.d("Sensors","Shake Detected")
                when (languageSetting) {
                    0 -> textToSpeech!!.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, null)
                    //english tts can handle hangul, no korean tts supported
                    1 -> textToSpeech!!.speak("안녕하세요", TextToSpeech.QUEUE_FLUSH, null, null)
                    //no tts for japanese, so we will use english tts
                    2 -> textToSpeech!!.speak("konnichiwa", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                var gmmIntentUri = Uri.parse("geo:51.5072,0.1276")
                val coin = Random.nextInt(0,1)
                if(languageSetting == 0){
                    if(coin == 0) {
                        gmmIntentUri = Uri.parse("geo:51.5072,0.1276")
                    }else{
                        gmmIntentUri = Uri.parse("geo:36.1716,115.1391")
                    }
                }else if(languageSetting == 1){
                    if(coin == 0) {
                        gmmIntentUri = Uri.parse("geo:33.3846,126.5535")
                    }else{
                        gmmIntentUri = Uri.parse("geo:37.5519,126.9918")
                    }
                }else {
                    if(coin == 0) {
                        gmmIntentUri = Uri.parse("geo:35.6764,139.6500")
                    }else{
                        gmmIntentUri = Uri.parse("geo:34.6937,135.5023")
                    }
                }
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        //Log.d("Spinner",position.toString())
        textToSpeech = TextToSpeech(this,this)
        languageSetting = position
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
    private val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
            val editText = findViewById<EditText>(R.id.textBox)
            editText.setText(results[0])
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var currentLocale: Locale = Locale.ENGLISH
            //korean and japanese tts not available so unable to test.
            /*when(languageSetting){
                0 -> currentLocale = Locale.ENGLISH
                1 -> currentLocale = Locale.KOREAN
                2 -> currentLocale = Locale.JAPANESE
            }*/
            val result = textToSpeech!!.setLanguage(currentLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            }
        }
    }
}
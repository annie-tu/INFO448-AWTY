package edu.uw.ischool.annietu8.arewethereyet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var editTextMessage: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextMinutes: EditText
    private lateinit var buttonStart: Button

    private var isServiceRunning = false
    private var messageHandler: Handler? = null
    private var phoneNumber: String? = null
    private var message: String? = null
    private var intervalMinutes: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextMessage= findViewById(R.id.editTextMessage)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        editTextMinutes = findViewById(R.id.editTextMinutes)
        buttonStart = findViewById(R.id.buttonStart)

        editTextMessage.addTextChangedListener(textWatcher)
        editTextPhoneNumber.addTextChangedListener(textWatcher)
        editTextMinutes.addTextChangedListener(textWatcher)

    }
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val message = editTextMessage.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()
            val minutes = editTextMinutes.text.toString()

            buttonStart.isEnabled =
                message.isNotEmpty() && phoneNumber.isNotEmpty() && isValidMinutes(minutes)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    private fun isValidMinutes(minutes: String): Boolean {
        return minutes.isNotEmpty() && minutes.toInt() > 0
    }

    // Handle "Start" button click
    fun onStartButtonClick(view: View) {
        if (!isServiceRunning) {
            startService()
            buttonStart.text = "Stop"
        } else {
            stopService()
            buttonStart.text = "Start"
        }
    }


    private fun startService() {
        if (isValidMinutes(editTextMinutes.text.toString())) {
            // Get values from UI
            phoneNumber = editTextPhoneNumber.text.toString()
            message = editTextMessage.text.toString()
            intervalMinutes = editTextMinutes.text.toString().toInt()

            // Create a handler to send messages at regular intervals
            messageHandler = Handler(Looper.getMainLooper())
            messageHandler?.postDelayed(sendMessageRunnable, (intervalMinutes * 60 * 1000).toLong())

            // Set the flag indicating the service is running
            isServiceRunning = true
        }
    }

    private fun stopService() {
        // Remove any pending callbacks from the handler
        messageHandler?.removeCallbacks(sendMessageRunnable)

        // Set the flag indicating the service is stopped
        isServiceRunning = false
    }

    private val sendMessageRunnable = object : Runnable {
        override fun run() {
            // Log.i("MainActivity", "toasting message")
            Toast.makeText(
                this@MainActivity,
                "$phoneNumber: $message",
                Toast.LENGTH_SHORT
            ).show()

            // Schedule the next message
            messageHandler?.postDelayed(this, (intervalMinutes * 60 * 1000).toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}
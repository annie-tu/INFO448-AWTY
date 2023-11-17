package edu.uw.ischool.annietu8.arewethereyet

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

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
    }

    private fun isValidMinutes(minutes: String): Boolean {
        return minutes.isNotEmpty() && minutes.toInt() > 0
    }

    // Handle "Start" button click
    fun onStartButtonClick(view: View) {
        if (!isServiceRunning) {
            startService()
        } else {
            stopService()
        }
    }


    private fun startService(): Boolean {
        if (!isValidMinutes(editTextMinutes.text.toString())) {
            showInvalidValuesDialog()
            return false
        }

        phoneNumber = editTextPhoneNumber.text.toString()
        message = editTextMessage.text.toString()
        intervalMinutes = editTextMinutes.text.toString().toInt()

        // Send messages at regular intervals
        messageHandler = Handler(Looper.getMainLooper())
        messageHandler?.postDelayed(sendMessageRunnable, (intervalMinutes * 60 * 1000).toLong())

        isServiceRunning = true
        buttonStart.text = "Stop"
        return true
    }

    private fun showInvalidValuesDialog() {
        // asked chatGPT how to set an alert dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Application Cannot Start")
        alertDialogBuilder.setMessage("Please enter legitimate values.")
        alertDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            // dismiss the dialog
        })
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun stopService() {
        messageHandler?.removeCallbacks(sendMessageRunnable)
        isServiceRunning = false
        buttonStart.text = "Start"
    }

    private val sendMessageRunnable = object : Runnable {
        override fun run() {
            Log.i("MainActivity", "toasting message")
            Toast.makeText(
                this@MainActivity,
                "Texting $phoneNumber: $message",
                Toast.LENGTH_SHORT
            ).show()

            messageHandler?.postDelayed(this, (intervalMinutes * 60 * 1000).toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}
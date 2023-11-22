package edu.uw.ischool.annietu8.arewethereyet

import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.telephony.SmsManager
import android.Manifest

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

    private val SMS_PERMISSION_REQUEST_CODE = 101
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
        if (!checkSmsPermission()) {
            return false
        }

        messageHandler = Handler(Looper.getMainLooper())
        messageHandler?.postDelayed(sendMessageRunnable, (intervalMinutes * 60 * 1000).toLong())

        buttonStart.text = "Stop"
        isServiceRunning = true
        return true
    }

    private val sendMessageRunnable = object : Runnable {
        override fun run() {
            Log.i("MainActivity", "message")

            // Send SMS
            sendSMS(phoneNumber, message)

            // Schedule the next message
            messageHandler?.postDelayed(this, (intervalMinutes * 60 * 1000).toLong())
        }
    }

    private fun sendSMS(phoneNumber: String?, message: String?) {
        try {
            val smsManager = SmsManager.getDefault()
            val piSent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT)
            val piDelivered = PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_UPDATE_CURRENT)

            // Ensure phoneNumber is not null
            phoneNumber?.let {
                smsManager.sendTextMessage(it, null, message, piSent, piDelivered)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to send SMS.")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkSmsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    SMS_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, start the service
            startService()
        } else {
            showToast("SMS permission denied. Cannot send messages.")
        }
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

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}
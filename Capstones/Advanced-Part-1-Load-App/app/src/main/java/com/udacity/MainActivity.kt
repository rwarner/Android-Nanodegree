package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cancelNotifications
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import sendNotification
import java.util.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var lastDownloadedSelection = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Reset clickable on re-entry of MainActivity
        radioGroup.isClickable = true
        radio_option1.isClickable = true
        radio_option2.isClickable = true
        radio_option3.isClickable = true

        download_button.isClickable = true
        download_button.setLoadingState(ButtonState.Click)

        // Create a notification channel to send our notifications to
        createChannel(CHANNEL_ID, "Download Notifications")

        // Setup our download button on click listener to download the appropriate repo zip
        download_button.setOnClickListener {
            // Disable download button
            download_button.isClickable = false

            // Disable radio group
            radioGroup.isClickable = false
            radio_option1.isClickable = false
            radio_option2.isClickable = false
            radio_option3.isClickable = false

            val radioSelection = this.radioGroup.checkedRadioButtonId

            if(radioSelection == -1) {
                // Nothing selected, notify user
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            } else {

                // Check which radio button was clicked
                when (radioSelection) {
                    R.id.radio_option1 -> {
                        lastDownloadedSelection = getString(R.string.radio_option_1)
                        download(GLIDE_URL)
                    }
                    R.id.radio_option2 -> {
                        lastDownloadedSelection = getString(R.string.radio_option_2)
                        download(LOADAPP_URL)
                    }
                    R.id.radio_option3 -> {
                        lastDownloadedSelection = getString(R.string.radio_option_3)
                        download(RETROFIT_URL)
                    }
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notificationManager = ContextCompat.getSystemService(
                this@MainActivity,
                NotificationManager::class.java
            ) as NotificationManager

            // Set the loading state of the button, so we can show an updated view
            download_button.setLoadingState(ButtonState.Completed)

            // Clear any pending notifications
            notificationManager.cancelNotifications()

            // Send out our notification that it is complete
            notificationManager.sendNotification(
                "Download complete",
                lastDownloadedSelection,
                this@MainActivity
            )
        }
    }

    private fun download(urlToDownload: String) {
        download_button.setLoadingState(ButtonState.Downloading)

        val request =
            DownloadManager.Request(Uri.parse(urlToDownload))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.

        // Update progress / counting every second
        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                if(download_button.getLoadingState() == ButtonState.Downloading)
                    // Continually increment progress bar until reaching 90%, then pause
                    if(download_button.progress < 0.9f) {
                        download_button.progress += 0.1f
                        download_button.invalidate()
                    }
            }
        }, 0, 1000)
    }

    companion object {
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val LOADAPP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
    }


    private fun createChannel(channelId: String, channelName: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download is complete"

            val notificationManager = ContextCompat.getSystemService(
                this@MainActivity,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onStart() {
        super.onStart()

        // Register for our downloads being completed
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}

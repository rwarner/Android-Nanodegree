package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*


class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        title = getString(R.string.title_activity_detail)

        // Cancel notification that sent us here
        val notificationId = intent.extras?.get("notificationId")
        val notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancel(notificationId as Int)

        // Fetch the file name that was sent along to us
        val downloadSelection = intent.extras?.getString("selection")

        // Update the text view to reflect the selected download
        tvFileName.text = downloadSelection

        // Send the user back to Main Activity once clicking OK
        btnOk.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

}

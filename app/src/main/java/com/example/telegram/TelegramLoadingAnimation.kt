package com.example.telegram

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.telegram.views.ChatProgressView

class TelegramLoadingAnimation : AppCompatActivity(R.layout.telegram_loading_animation) {

    private val handler = Handler()
    private val progressRunnable = ProgressRunnable()

    private lateinit var vChatProgressView: ChatProgressView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vChatProgressView = findViewById(R.id.vChatProgress)
        vChatProgressView.setOnClickListener { handler.postDelayed(progressRunnable, DELAY_MILLIS) }
    }

    private inner class ProgressRunnable : Runnable {

        private var progress: Float = 0f

        override fun run() {
            progress += PROGRESS_VALUE
            vChatProgressView.progress = progress

            if (progress == FINISH_VALUE) {
                handler.removeCallbacks(this)
            } else {
                handler.postDelayed(this, DELAY_MILLIS)
            }
        }
    }

    companion object {
        private const val DELAY_MILLIS = 1000L
        private const val PROGRESS_VALUE = 0.1f
        private const val FINISH_VALUE = 1f
    }
}

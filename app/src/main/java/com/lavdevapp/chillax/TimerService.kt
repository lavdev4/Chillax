package com.lavdevapp.chillax

import android.app.Service
import android.os.CountDownTimer
import android.content.Intent
import android.os.IBinder

class TimerService : Service() {
    private var timer: CountDownTimer? = null
    override fun onCreate() {
        super.onCreate()
        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sendBroadcast(Intent())
            }

            override fun onFinish() {}
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
package com.lavdevapp.chillax

import android.app.Service
import android.os.CountDownTimer
import android.content.Intent
import android.os.IBinder

// TODO: add in manifest
class PlayerService : Service() {

    override fun onCreate() {
        super.onCreate()
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
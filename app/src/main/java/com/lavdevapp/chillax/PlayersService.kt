package com.lavdevapp.chillax

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

// TODO: add in manifest
class PlayersService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayersServiceBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private inner class PlayersServiceBinder : Binder() {
        val service: PlayersService
            get() = this@PlayersService
    }
}
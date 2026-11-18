package com.illustravibes.arus

import android.app.Application

class ArusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

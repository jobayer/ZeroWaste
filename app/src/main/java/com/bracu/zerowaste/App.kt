package com.bracu.zerowaste

import android.app.Application
import com.bracu.zerowaste.data.db.SecureDb

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SecureDb.init(this)
    }

}
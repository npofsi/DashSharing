package pro.npofsi.dashsharing

import android.app.Application
import android.content.Intent

class MainApplication : Application() {
    lateinit var httpServiceIntent: Intent
    var cacheFilePath: String? = null
    var filename: String? = null
    var isServiceRunning = false
    override fun onCreate() {
        super.onCreate()
        httpServiceIntent = Intent(this, HttpService::class.java)
    }
}
package pro.npofsi.dashsharing

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.style.TtsSpan
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import com.yanzhenjie.andserver.website.StorageWebsite
import com.yanzhenjie.andserver.website.WebSite
import pro.npofsi.dashsharing.utils.FileX
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit


class HttpService : Service() {
    var CHANNEL_ID = "pro.npofsi.dashsharing"
    val CHANNEL_NAME = "Channel DashSharing"
    var serverbuilder: Server.Builder? = null
    var server: Server? = null
    var port = 3090
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        (application as MainApplication).isServiceRunning = true
        setForgeground()
        Toast.makeText(
            this,  "Open http://" + getLocalIpAddress() + ":" + port.toString() + " on another device while IN THE SAME NETWORK to download file",
            Toast.LENGTH_LONG
        ).show()
        startServer()
    }

    override fun onDestroy() {

        (application as MainApplication).isServiceRunning = false
        server?.shutdown()
        super.onDestroy()
    }

    fun startServer() {

        val website: WebSite =
            StorageWebsite(FileX((application as MainApplication).cacheFilePath).backward().absolutePath)

        serverbuilder = AndServer.serverBuilder()
        serverbuilder?.apply {
            port(port)
            timeout(5, TimeUnit.SECONDS)
            website(website)
        }

        server = serverbuilder?.build()
        server?.startup()
        if (server == null) Log.e("HttpService", "Server is null")
    }

    fun setForgeground() {

        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).also {
                it.apply {
                    enableLights(false)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                }
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("File is on air now")
            .setContentText("Open http://" + getLocalIpAddress() + ":" + port.toString() + " on another device while IN THE SAME NETWORK to download file")
            .setContentIntent(pendingIntent)
            .setTicker("DashSharing running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

// Notification ID cannot be 0.
        startForeground(1, notification)

    }

    fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }
}
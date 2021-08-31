package com.deevvdd.rider.socket

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.deevvdd.rider.MainActivity
import com.deevvdd.rider.R
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject
import timber.log.Timber
import java.net.URISyntaxException

/**
 * Created by heinhtet deevvdd@gmail.com on 15,August,2021
 */
class SocketService : Service() {

    private lateinit var locationHandler: LocationHandler
    private lateinit var socket: Socket
    override fun onCreate() {
        super.onCreate()
        Timber.d("On Create")
        locationHandler =
            LocationHandler(this, ::emitLocation, onLocationUpdateOnError = ::emitLocationOnError)
        locationHandler.startLocationUpdates()
        try {
            val options = IO.Options()
            options.auth = mapOf("Authorization" to "Bearer empty")
            socket = IO.socket(SOCKET_URL, options)
            socket.connect()
            socket.on(
                "connect_error"
            ) { args ->
                Timber.d("Socket onConnect Error ${args.last()}")
            }
        } catch (e: URISyntaxException) {
            Timber.d("Socket Connect Error ${e.message}")
        }
        socket.emit("12345", "asfasldfjasdfadskfaks")
        Timber.d("Emit Order Id")
    }

    private fun emitLocation(location: Location) {
        Timber.d("Emit Location : $location isActive ${socket.isActive}")
        val json = JSONObject()
        json.put("lat", location.latitude)
        json.put("lng", location.longitude)
        json.put("to","12345")
        socket.emit("location", json)
    }

    private fun emitLocationOnError(message: String) {
        Timber.d("Emit Location Error : $message")
        socket.emit("orderId", message)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        socket.close()
        locationHandler.stopLocationUpdates()
        Timber.d("Service Destroy")
    }

    companion object {
        const val CHANNEL_ID = "LocationRemainderChannel"
        const val ONGOING_NOTIFICATION_ID = 100
        const val SOCKET_URL = "https://socket1.hungrynow.co.th"
    }
}
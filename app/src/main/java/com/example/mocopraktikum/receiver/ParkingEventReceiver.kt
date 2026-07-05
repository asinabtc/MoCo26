package com.example.mocopraktikum.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mocopraktikum.utils.NotificationHelper

class ParkingEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Update"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        
        when (intent.action) {
            ACTION_SPOT_ADDED, ACTION_STATUS_CHANGED -> {
                NotificationHelper.showNotification(context, title, message)
            }
        }
    }

    companion object {
        const val ACTION_SPOT_ADDED = "com.example.mocopraktikum.SPOT_ADDED"
        const val ACTION_STATUS_CHANGED = "com.example.mocopraktikum.STATUS_CHANGED"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }
}

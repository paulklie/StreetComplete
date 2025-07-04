package de.westnordost.streetcomplete.data.sync

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.app.PendingIntentCompat
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.MainActivity

/** Creates the notification for syncing in the Android notifications area. Used both by the upload
 *  and by the download service. */
fun createSyncNotification(context: Context, cancelIntent: PendingIntent): Notification {
    val manager = NotificationManagerCompat.from(context)
    if (manager.getNotificationChannelCompat(ApplicationConstants.NOTIFICATIONS_CHANNEL_SYNC) == null) {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(ApplicationConstants.NOTIFICATIONS_CHANNEL_SYNC, IMPORTANCE_LOW)
                .setName(context.getString(R.string.notification_channel_sync))
                .build()
        )
    }

    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    // Intent has to be mutable, otherwise the intent flags defined above are not applied
    val mainActivityIntent = PendingIntentCompat.getActivity(context, 0, intent, 0, true)

    return NotificationCompat.Builder(context, ApplicationConstants.NOTIFICATIONS_CHANNEL_SYNC)
        .setSmallIcon(R.mipmap.ic_notification)
        .setContentTitle(ApplicationConstants.NAME)
        .setTicker(context.resources.getString(R.string.notification_syncing))
        .setContentIntent(cancelIntent)
        .setOngoing(true)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setContentIntent(mainActivityIntent)
        .setDeleteIntent(cancelIntent)
        .addAction(android.R.drawable.ic_delete, context.resources.getString(android.R.string.cancel), cancelIntent)
        .build()
}

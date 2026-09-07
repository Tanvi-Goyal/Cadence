package com.mindset.race

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mindset.MainActivity
import com.mindset.R
import com.mindset.components.formatClockMs

internal object RaceNotifications {

    const val CHANNEL_ID = "race_timer"
    const val NOTIFICATION_ID = 1001

    const val ACTION_TOGGLE_PAUSE = "com.mindset.race.action.TOGGLE_PAUSE"
    const val ACTION_NEXT = "com.mindset.race.action.NEXT"

    const val EXTRA_OPEN_ACTIVE_RACE = "com.mindset.race.extra.OPEN_ACTIVE_RACE"

    private const val REQ_TAP = 1
    private const val REQ_TOGGLE_PAUSE = 2
    private const val REQ_NEXT = 3

    fun ensureChannel(context: Context) {
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.race_channel_name))
            .setDescription(context.getString(R.string.race_channel_description))
            .setShowBadge(false)
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    /**
     * Builds the notification for [race].
     *
     * The clock is rendered by the **system**: `setUsesChronometer` with a `when` in the past by the
     * elapsed time means SystemUI ticks the digits itself, so this only has to be re-posted when the
     * step or pause state actually changes — roughly 20 times a race instead of once a second.
     *
     * The system chronometer can't be paused, so a paused race switches it off and shows the frozen
     * time as text instead.
     */
    fun build(context: Context, race: RaceNotice, totalElapsedMs: Long): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_race)
            .setContentTitle(race.stepTitle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(tapIntent(context))

        if (race.paused) {
            builder
                .setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(
                    "${context.getString(R.string.race_paused)} · ${formatClockMs(totalElapsedMs)}",
                )
        } else {
            builder
                .setUsesChronometer(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis() - totalElapsedMs)
                .setContentText(
                    context.getString(R.string.race_step_progress, race.stepIndex + 1, race.totalSteps),
                )
        }

        val pauseLabel = if (race.paused) R.string.race_action_resume else R.string.race_action_pause
        builder.addAction(
            0,
            context.getString(pauseLabel),
            servicePendingIntent(context, ACTION_TOGGLE_PAUSE, REQ_TOGGLE_PAUSE),
        )

        val nextLabel = if (race.isLastStep) R.string.race_action_finish else R.string.race_action_next
        builder.addAction(
            0,
            context.getString(nextLabel),
            servicePendingIntent(context, ACTION_NEXT, REQ_NEXT),
        )

        return builder.build()
    }

    private fun servicePendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
    ): PendingIntent = PendingIntent.getService(
        context,
        requestCode,
        Intent(context, RaceTimerService::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun tapIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        REQ_TAP,
        Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_OPEN_ACTIVE_RACE, true),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

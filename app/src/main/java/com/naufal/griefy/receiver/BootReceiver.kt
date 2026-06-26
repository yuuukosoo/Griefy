package com.naufal.griefy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naufal.griefy.domain.usecase.reminder.GetRemembranceDaysUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getRemembranceDaysUseCase: GetRemembranceDaysUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = ReminderScheduler(context)
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val list = getRemembranceDaysUseCase().first()
                val now = System.currentTimeMillis()
                list.forEach { day ->
                    if (day.dateTime > now) {
                        scheduler.schedule(day)
                    }
                }
            }
        }
    }
}

package com.prajwalpawar.fiscus.util

import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

// Starts the app fresh after its database or settings were replaced (restore, wipe):
// relaunch the main screen in a new task, then end this process so nothing keeps
// the old database or cached state.
fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ?: return
    context.startActivity(intent)
    exitProcess(0)
}

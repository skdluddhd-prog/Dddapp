package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import com.example.MainActivity

/** Creates the assistant session Android opens on the home/power-button assist gesture. */
class SovereignAssistSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession =
        SovereignAssistSession(this)
}

private class SovereignAssistSession(context: Context) : VoiceInteractionSession(context) {
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        try {
            val launch = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_ASSIST
                putExtra("launched_from_home_assist_button", true)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
            context.startActivity(launch)
        } catch (_: Throwable) {
        }
        hide()
    }
}

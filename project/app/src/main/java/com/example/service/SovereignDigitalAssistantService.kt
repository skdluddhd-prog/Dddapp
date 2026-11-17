package com.example.service

import android.service.voice.VoiceInteractionService

/**
 * Registers SKRL 2 in Android Settings -> Apps -> Default apps -> Digital assistant app.
 * The user must select it there once; Android never lets an app replace the assistant silently.
 * The actual launch work is done by [SovereignAssistSessionService].
 */
class SovereignDigitalAssistantService : VoiceInteractionService()

package com.example.service

import android.content.Intent
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Android requires a RecognitionService to be declared before an app can be chosen as the
 * default assistant. SKRL 2 does its own speech handling in NativeVoiceEngine, so this service
 * honestly reports that it does not recognise speech for other apps.
 */
class SovereignRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        try {
            listener?.error(SpeechRecognizer.ERROR_CLIENT)
        } catch (_: Throwable) {
        }
    }

    override fun onCancel(listener: Callback?) {}

    override fun onStopListening(listener: Callback?) {}
}

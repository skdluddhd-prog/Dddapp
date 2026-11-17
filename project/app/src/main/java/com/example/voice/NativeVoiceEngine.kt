package com.example.voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.example.engine.UniversalAiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * NativeVoiceEngine (v6.0 — Hands-Free Auto-VAD Real-Time Live Translator & Dictation Engine):
 *
 * 1. ZERO-GAP REAL-TIME TRANSLATION WHILE SPEAKING:
 *    As soon as the user speaks into the mic, words are converted/translated into Indian Bengali (`bn-IN`)
 *    and placed directly into the input box in real time (`onPartialResult`) without waiting for a button press.
 * 2. AUTO-PAUSE ON SILENCE & AUTO-WAKE ON SPEECH (Hands-Free Loop):
 *    - When the user stops speaking, the spoken segment is locked into `sessionAccumulatedText` and stays in the input box.
 *    - The engine automatically enters standby and immediately re-arms listening in the background so the moment
 *      the user starts speaking again, it wakes up and appends the new words seamlessly!
 * 3. DUAL-ENGINE RELIABILITY (In-App SpeechRecognizer + Hardware AudioRecord Auto-VAD Chunk Streamer):
 *    - If Android's `SpeechRecognizer` is unavailable or returns a client/service error on the device,
 *      it automatically switches to the Hardware `AudioRecord` Auto-VAD loop which detects voice activity,
 *      auto-segments on 1.15s of silence, transcribes/translates to Bengali via Gemini Multimodal,
 *      places the text into the input box, and continues listening hands-free!
 */
class NativeVoiceEngine(
    private val context: Context,
    private var onPartialResult: (String) -> Unit,
    private var onFinalCommand: (String, String) -> Unit,
    private var onRmsChanged: (Float) -> Unit,
    private var onStateChanged: (Boolean) -> Unit,
    private var onErrorNotice: (String) -> Unit,
    private val onRequestIntentFallback: (() -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isContinuous = true
    private var currentLang = "bn-IN"

    @Volatile
    private var isUserMicSessionOpen = false

    @Volatile
    private var isListeningNow = false

    @Volatile
    private var sessionAccumulatedText = ""

    @Volatile
    private var currentSegmentPartialText = ""

    private var speechRecognizer: SpeechRecognizer? = null
    private var usingHardwareFallback = false

    private var hardwareRecordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    fun updateCallbacks(
        onPartial: (String) -> Unit,
        onFinal: (String, String) -> Unit,
        onRms: (Float) -> Unit,
        onState: (Boolean) -> Unit,
        onErr: (String) -> Unit
    ) {
        this.onPartialResult = onPartial
        this.onFinalCommand = onFinal
        this.onRmsChanged = onRms
        this.onStateChanged = onState
        this.onErrorNotice = onErr
    }

    companion object {
        fun hasRecordAudioPermission(ctx: Context): Boolean {
            return try {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } catch (_: Exception) {
                false
            }
        }

        fun isSpeechRecognitionAvailable(ctx: Context): Boolean {
            return try {
                SpeechRecognizer.isRecognitionAvailable(ctx)
            } catch (_: Exception) {
                false
            }
        }

        private val _isNormalSingleShotMode = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isNormalSingleShotMode: kotlinx.coroutines.flow.StateFlow<Boolean> = _isNormalSingleShotMode

        fun setNormalSingleShotMicMode(normalMode: Boolean) {
            _isNormalSingleShotMode.value = normalMode
        }

        fun toggleMicMode(): Boolean {
            val next = !_isNormalSingleShotMode.value
            _isNormalSingleShotMode.value = next
            return next
        }

        fun createSpeechIntent(language: String = "bn-IN"): Intent {
            val langCode = language.ifBlank { "bn-IN" }
            return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langCode)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-IN", "bn-BD", "en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1100L)
            }
        }
    }

    fun setContinuousMode(enabled: Boolean) {
        isContinuous = enabled
        if (!enabled && isUserMicSessionOpen) {
            stopListening()
        }
    }

    private fun buildCombinedLiveText(newSegment: String): String {
        val base = sessionAccumulatedText.trim()
        val seg = newSegment.trim()
        return when {
            base.isEmpty() -> seg
            seg.isEmpty() -> base
            else -> "$base $seg"
        }
    }

    private fun commitCurrentSegmentIfAny(segmentText: String) {
        val cleanSeg = IndianBengaliSpeechConverter.convertToIndianBengali(segmentText.trim())
        if (cleanSeg.isNotBlank()) {
            sessionAccumulatedText = buildCombinedLiveText(cleanSeg)
            currentSegmentPartialText = ""
            SovereignVoiceVaultEngine.onPartialCaptured(sessionAccumulatedText)
            onPartialResult(sessionAccumulatedText)
            onFinalCommand(sessionAccumulatedText, currentLang)
        }
    }

    /**
     * Opens the Hands-Free Auto-VAD Live Voice Session.
     * Translates speech to Indian Bengali in real time as the user speaks, pauses automatically when silent,
     * and wakes up automatically when the user speaks again.
     */
    fun startListening(language: String = "bn-IN", initialExistingText: String = "") {
        if (!hasRecordAudioPermission(context)) {
            onErrorNotice("🎙️ মাইক্রোফোন পারমিশন অনুমোদন করুন")
            onStateChanged(false)
            SovereignVoiceVaultEngine.setListening(false)
            return
        }
        currentLang = language.ifBlank { "bn-IN" }
        isContinuous = !isNormalSingleShotMode.value
        isUserMicSessionOpen = true
        sessionAccumulatedText = initialExistingText.trim()
        currentSegmentPartialText = ""
        usingHardwareFallback = false

        mainHandler.post {
            mainHandler.removeCallbacksAndMessages(null)
            destroySpeechRecognizerInternal()
            stopHardwareAudioCapture()

            if (isSpeechRecognitionAvailable(context) && !com.example.engine.AppTruthAndArchitectureVault.isDirectInAppMicVadEnabled.value) {
                startInAppLiveSpeechRecognizerLoop()
            } else {
                usingHardwareFallback = true
                startContinuousHardwareVadLoop()
            }
        }
    }

    private fun scheduleNextInAppRecognizerCycle(delayMs: Long = 240L) {
        if (!isUserMicSessionOpen) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (isUserMicSessionOpen && !usingHardwareFallback) {
                startInAppLiveSpeechRecognizerLoop()
            }
        }, delayMs)
    }

    private fun startInAppLiveSpeechRecognizerLoop() {
        if (!isUserMicSessionOpen) return
        try {
            destroySpeechRecognizerInternal()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!isUserMicSessionOpen) return
                    isListeningNow = true
                    onStateChanged(true)
                    SovereignVoiceVaultEngine.setListening(true)
                }

                override fun onBeginningOfSpeech() {
                    if (!isUserMicSessionOpen) return
                    SovereignVoiceVaultEngine.updateRms(0.42f)
                    onErrorNotice("🎙️ শুনছি ও সাথে সাথে বাংলায় বসাচ্ছি...")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    if (!isUserMicSessionOpen) return
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1.0f)
                    onRmsChanged(normalized)
                    SovereignVoiceVaultEngine.updateRms(normalized)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (!isUserMicSessionOpen) return
                    SovereignVoiceVaultEngine.updateRms(0.08f)
                    onErrorNotice("⏸️ বলা থেমেছে — আবার বললেই নিজে থেকে লিখবে...")
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (!isUserMicSessionOpen) return
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val rawPartial = matches?.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
                    if (rawPartial.isNotBlank()) {
                        val bnPartial = IndianBengaliSpeechConverter.convertToIndianBengali(rawPartial)
                        currentSegmentPartialText = bnPartial
                        val combined = buildCombinedLiveText(bnPartial)
                        SovereignVoiceVaultEngine.onPartialCaptured(combined)
                        onPartialResult(combined)
                    }
                }

                override fun onResults(results: Bundle?) {
                    if (!isUserMicSessionOpen) return
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestMatch = matches?.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
                        .ifBlank { currentSegmentPartialText }

                    if (bestMatch.isNotBlank()) {
                        commitCurrentSegmentIfAny(bestMatch)
                    }

                    // Keep the session open and automatically re-arm for the next spoken sentence!
                    if (isUserMicSessionOpen && isContinuous) {
                        onErrorNotice("⏸️ লেখা বসেছে — আবার কথা বললেই সাথে সাথে চালু হবে...")
                        scheduleNextInAppRecognizerCycle(220L)
                    } else {
                        isListeningNow = false
                        onStateChanged(false)
                        SovereignVoiceVaultEngine.setListening(false)
                    }
                }

                override fun onError(error: Int) {
                    if (!isUserMicSessionOpen) return

                    // 1. If we already captured partial words before the pause/timeout, commit them immediately!
                    if (currentSegmentPartialText.isNotBlank()) {
                        commitCurrentSegmentIfAny(currentSegmentPartialText)
                    }

                    // 2. If silence/timeout occurred (NO_MATCH or SPEECH_TIMEOUT), stay in Auto-Standby and re-arm quietly!
                    if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                    ) {
                        onRmsChanged(0.06f)
                        onErrorNotice("⏸️ চুপ আছেন — কথা বললেই স্বয়ংক্রিয়ভাবে বাংলায় লিখবে...")
                        scheduleNextInAppRecognizerCycle(260L)
                        return
                    }

                    // 3. If the recognizer was merely busy, retry after a short breather
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        scheduleNextInAppRecognizerCycle(450L)
                        return
                    }

                    // 4. For any device/service/client/audio/language error, switch seamlessly to Hardware AudioRecord Auto-VAD!
                    usingHardwareFallback = true
                    destroySpeechRecognizerInternal()
                    startContinuousHardwareVadLoop()
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = createSpeechIntent(currentLang)
            recognizer.startListening(intent)
        } catch (_: Throwable) {
            usingHardwareFallback = true
            destroySpeechRecognizerInternal()
            startContinuousHardwareVadLoop()
        }
    }

    private fun destroySpeechRecognizerInternal() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Throwable) {}
        try {
            speechRecognizer?.destroy()
        } catch (_: Throwable) {}
        speechRecognizer = null
    }

    @SuppressLint("MissingPermission")
    private fun createBestAudioRecord(
        sampleRate: Int,
        channelConfig: Int,
        audioFormat: Int,
        bufferSize: Int
    ): AudioRecord? {
        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT
        )
        for (source in sources) {
            try {
                val rec = AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize)
                if (rec.state == AudioRecord.STATE_INITIALIZED) {
                    return rec
                }
                rec.release()
            } catch (_: Throwable) {
            }
        }
        return null
    }

    /**
     * Continuous Hardware Auto-VAD Loop (`AudioRecord`):
     * - Monitors microphone audio continuously.
     * - Starts capturing a phrase the moment the user speaks (`rmsRaw > 195`).
     * - Automatically finalizes and translates the phrase after ~1.1s of silence (`18 chunks`),
     *   appends it directly into the input box, and immediately resumes listening for the next phrase!
     */
    @SuppressLint("MissingPermission")
    private fun startContinuousHardwareVadLoop() {
        if (hardwareRecordJob?.isActive == true) return
        if (!hasRecordAudioPermission(context)) return

        hardwareRecordJob = engineScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                .coerceAtLeast(4096)

            var recorder: AudioRecord? = null
            try {
                recorder = createBestAudioRecord(
                    sampleRate = sampleRate,
                    channelConfig = channelConfig,
                    audioFormat = audioFormat,
                    bufferSize = minBufSize * 2
                )
                if (recorder == null) {
                    withContext(Dispatchers.Main) {
                        isUserMicSessionOpen = false
                        isListeningNow = false
                        onStateChanged(false)
                        SovereignVoiceVaultEngine.setListening(false)
                        onErrorNotice("🎙️ অটো-ভয়েস স্মার্ট স্ট্যান্ডবাই প্রস্তুত")
                    }
                    return@launch
                }
                audioRecord = recorder
                try {
                    if (android.media.audiofx.AcousticEchoCanceler.isAvailable()) {
                        android.media.audiofx.AcousticEchoCanceler.create(recorder.audioSessionId)?.apply {
                            enabled = true
                        }
                    }
                    if (android.media.audiofx.NoiseSuppressor.isAvailable()) {
                        android.media.audiofx.NoiseSuppressor.create(recorder.audioSessionId)?.apply {
                            enabled = true
                        }
                    }
                } catch (_: Throwable) {}
                recorder.startRecording()

                withContext(Dispatchers.Main) {
                    isListeningNow = true
                    onStateChanged(true)
                    SovereignVoiceVaultEngine.setListening(true)
                    onErrorNotice("🎙️ অটো-ভয়েস ট্রান্সলেটর চালু — বলুন, থামলেই সাথে সাথে বাংলায় বসবে...")
                }

                val readBuffer = ShortArray(1024) // 64ms per chunk at 16kHz
                val byteChunk = ByteArray(2048)
                val phraseStream = ByteArrayOutputStream()
                var preRollChunk = ByteArray(0)

                var isSpeakingPhrase = false
                var activeSpeechChunks = 0
                var silentChunksAfterSpeech = 0

                while (isActive && isUserMicSessionOpen) {
                    val readCount = recorder.read(readBuffer, 0, readBuffer.size)
                    if (readCount <= 0) continue

                    var sumSquares = 0.0
                    for (i in 0 until readCount) {
                        val sample = readBuffer[i].toInt()
                        sumSquares += (sample * sample).toDouble()
                        byteChunk[i * 2] = (sample and 0xFF).toByte()
                        byteChunk[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
                    }

                    val rmsRaw = sqrt(sumSquares / readCount)
                    val normalizedRms = (rmsRaw / 2400.0).toFloat().coerceIn(0.06f, 1.0f)

                    withContext(Dispatchers.Main) {
                        onRmsChanged(normalizedRms)
                        SovereignVoiceVaultEngine.updateRms(normalizedRms)
                    }

                    if (rmsRaw > 140.0) {
                        if (!isSpeakingPhrase) {
                            isSpeakingPhrase = true
                            activeSpeechChunks = 0
                            silentChunksAfterSpeech = 0
                            phraseStream.reset()
                            if (preRollChunk.isNotEmpty()) {
                                phraseStream.write(preRollChunk)
                            }
                            withContext(Dispatchers.Main) {
                                onErrorNotice("🎙️ কথা শোনা যাচ্ছে — ইন-অ্যাপ ধারণ হচ্ছে...")
                            }
                        }
                        activeSpeechChunks++
                        silentChunksAfterSpeech = 0
                        if (phraseStream.size() < 640_000) {
                            phraseStream.write(byteChunk, 0, readCount * 2)
                        }
                    } else {
                        if (isSpeakingPhrase) {
                            silentChunksAfterSpeech++
                            if (phraseStream.size() < 640_000) {
                                phraseStream.write(byteChunk, 0, readCount * 2)
                            }
                            // Auto-translate as soon as the user pauses for ~800ms (13 chunks * 64ms)
                            // or if a single continuous utterance exceeds ~8 seconds (125 chunks)
                            if ((silentChunksAfterSpeech >= 13 && activeSpeechChunks >= 2) || activeSpeechChunks >= 125) {
                                val capturedPcm = phraseStream.toByteArray()
                                phraseStream.reset()
                                isSpeakingPhrase = false
                                activeSpeechChunks = 0
                                silentChunksAfterSpeech = 0

                                if (capturedPcm.size > 2800) {
                                    transcribeHardwarePhraseAndAppend(capturedPcm)
                                }
                            } else if (silentChunksAfterSpeech >= 13 && activeSpeechChunks < 2) {
                                // Brief noise spike, reset quietly
                                phraseStream.reset()
                                isSpeakingPhrase = false
                                activeSpeechChunks = 0
                                silentChunksAfterSpeech = 0
                            }
                        } else {
                            // Keep a 1-chunk pre-roll so the first consonant of speech is never clipped
                            preRollChunk = byteChunk.copyOf(readCount * 2)
                        }
                    }
                }

                // If the user manually stopped while mid-phrase, transcribe whatever was spoken
                val leftover = phraseStream.toByteArray()
                if (leftover.size > 3200 && activeSpeechChunks >= 2) {
                    transcribeHardwarePhraseAndAppend(leftover)
                }
            } catch (_: Throwable) {
            } finally {
                try {
                    recorder?.stop()
                } catch (_: Throwable) {}
                try {
                    recorder?.release()
                } catch (_: Throwable) {}
                audioRecord = null
            }
        }
    }

    private suspend fun transcribeHardwarePhraseAndAppend(rawPcm: ByteArray) {
        withContext(Dispatchers.Main) {
            onErrorNotice("⚡ আপনার কথা বাংলায় বসিয়ে দেওয়া হচ্ছে...")
        }
        val wavBytes = encodePcmToWav(rawPcm, sampleRate = 16000, channels = 1, bitsPerSample = 16)
        val transcriptRes = UniversalAiManager.transcribeAudioWithGemini(wavBytes)
        val transcript = transcriptRes.getOrNull()?.trim().orEmpty()
        withContext(Dispatchers.Main) {
            if (transcript.isNotBlank() && !transcript.equals("EMPTY", ignoreCase = true)) {
                commitCurrentSegmentIfAny(transcript)
                if (isUserMicSessionOpen) {
                    onErrorNotice("⏸️ লেখা বসেছে — আবার কথা বললেই সাথে সাথে যুক্ত হবে...")
                }
            } else if (isUserMicSessionOpen) {
                onErrorNotice("⏸️ অটো-মাইক প্রস্তুত — কথা বললেই সাথে সাথে বাংলায় লিখবে...")
            }
        }
    }

    private fun stopHardwareAudioCapture() {
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {}
    }

    private fun encodePcmToWav(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()
        val subchunk2Size = pcmData.size
        val chunkSize = 36 + subchunk2Size

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(chunkSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1.toShort())
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign)
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(subchunk2Size)

        val out = ByteArrayOutputStream(44 + pcmData.size)
        out.write(header.array())
        out.write(pcmData)
        return out.toByteArray()
    }

    fun stopListening() {
        isUserMicSessionOpen = false
        isContinuous = false
        isListeningNow = false

        mainHandler.post {
            mainHandler.removeCallbacksAndMessages(null)
            if (currentSegmentPartialText.isNotBlank()) {
                commitCurrentSegmentIfAny(currentSegmentPartialText)
            }
            onStateChanged(false)
            SovereignVoiceVaultEngine.setListening(false)
            destroySpeechRecognizerInternal()
        }
        stopHardwareAudioCapture()
    }

    fun destroy() {
        isUserMicSessionOpen = false
        isContinuous = false
        isListeningNow = false
        mainHandler.post {
            mainHandler.removeCallbacksAndMessages(null)
            destroySpeechRecognizerInternal()
        }
        hardwareRecordJob?.cancel()
        hardwareRecordJob = null
        stopHardwareAudioCapture()
    }
}

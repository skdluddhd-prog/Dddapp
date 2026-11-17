package com.example.engine

import android.webkit.WebView
import com.example.model.RuleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WorkerConfig(
    val taskId: String,
    val taskTitle: String,
    val rule: RuleType = RuleType.EXTRACT_CODE,
    val brainPower: Int = 75,
    val isCloudExecution: Boolean = false,
    val cloudWebhookUrl: String = "",
    val customParam: String = "",
    val inputPayload: String = "",
    val autoFlushMemory: Boolean = true
)

sealed class RouterResult {
    data class Success(val output: String, val routeType: String) : RouterResult()
    data class Error(val message: String) : RouterResult()
}

object TaskRouter {
    private val routerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun dispatchWorker(
        config: WorkerConfig,
        webView: WebView?,
        onComplete: (RouterResult) -> Unit
    ) {
        routerScope.launch {
            try {
                if (config.isCloudExecution && config.cloudWebhookUrl.isNotBlank()) {
                    val json = JSONObject().apply {
                        put("taskId", config.taskId)
                        put("title", config.taskTitle)
                        put("rule", config.rule.name)
                        put("brainPower", config.brainPower)
                        put("customParam", config.customParam)
                        put("payload", config.inputPayload)
                    }
                    val request = Request.Builder()
                        .url(config.cloudWebhookUrl.trim())
                        .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                onComplete(RouterResult.Success(body.ifBlank { config.inputPayload }, "CLOUD_WEBHOOK"))
                            } else {
                                onComplete(RouterResult.Error("Cloud HTTP ${response.code}: $body"))
                            }
                        }
                    }
                } else {
                    val processed = when (config.rule) {
                        RuleType.EXTRACT_CODE -> RegexSurgeon.extractCodeOnly(config.inputPayload)
                        RuleType.APPEND_TEXT -> "${config.inputPayload}\n\n/* ${config.customParam} */"
                        else -> config.inputPayload
                    }
                    if (config.autoFlushMemory) {
                        System.gc()
                    }
                    withContext(Dispatchers.Main) {
                        onComplete(RouterResult.Success(processed, "LOCAL_ENGINE"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(RouterResult.Error(e.message ?: "Router dispatch failed"))
                }
            }
        }
    }
}

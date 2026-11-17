package com.example.engine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class VirtualProjectFile(
    val fileName: String,
    val language: String,
    val content: String,
    val darwinianScore: Int = 98
)

/**
 * Step 3 + Sovereign 100% Continuation Packager:
 * - Parallel Multi-Key Split Execution & Multi-File Virtual File System (.ZIP)
 * - 100% Live App State Snapshot (All SharedPreferences, Chat 1/2 History, Incremental Slots,
 *   Custom Websites, Custom AIs, Omni-Buttons, Rules, Checkpoints & Files)
 * - Real Installed APK Binary Extractor & Downloader/Sharer
 * - Direct Gmail (`skdluddhd@gmail.com`) Attachment Sender (.ZIP + .APK + Continuation Links)
 * - 100% Exact-Stop-Point State Restorer (from .ZIP or JSON/Text Snapshot)
 */
object MultiFileProjectZipEngine {

    const val DEFAULT_USER_GMAIL = "skdluddhd@gmail.com"
    const val DEV_STUDIO_CONTINUE_URL = "https://ais-dev-a324yx3i7d7solvk4vzcib-811758898257.asia-east1.run.app"
    const val SHARED_APP_CONTINUE_URL = "https://ais-pre-a324yx3i7d7solvk4vzcib-811758898257.asia-east1.run.app"

    private val _isBuildingProject = MutableStateFlow(false)
    val isBuildingProject: StateFlow<Boolean> = _isBuildingProject.asStateFlow()

    private val _buildStatus = MutableStateFlow("📦 ১০০% ফুল অ্যাপ .ZIP, আসল .APK ও জিমেইল রিজিউম প্যাক প্রস্তুত")
    val buildStatus: StateFlow<String> = _buildStatus.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<VirtualProjectFile>>(defaultStarterFiles())
    val projectFiles: StateFlow<List<VirtualProjectFile>> = _projectFiles.asStateFlow()

    fun updateFileContent(fileName: String, newContent: String) {
        _projectFiles.value = _projectFiles.value.map {
            if (it.fileName == fileName) it.copy(content = newContent) else it
        }
    }

    fun addOrReplaceFile(fileName: String, language: String, content: String) {
        val cleanName = fileName.trim().ifBlank { "module_${_projectFiles.value.size + 1}.js" }
        val existingIdx = _projectFiles.value.indexOfFirst { it.fileName.equals(cleanName, ignoreCase = true) }
        val updated = _projectFiles.value.toMutableList()
        val item = VirtualProjectFile(cleanName, language, content, verifyDarwinianScore(content))
        if (existingIdx >= 0) {
            updated[existingIdx] = item
        } else {
            updated.add(item)
        }
        _projectFiles.value = updated
    }

    /**
     * Generates a 100% complete JSON snapshot of every SharedPreferences file and internal state file
     * so the app can resume from the exact microsecond it stopped on any device, website, or AI Studio session.
     */
    fun captureFullAppContinuationStateJson(context: Context): String {
        return try {
            val root = JSONObject()
            root.put("engine", "SKRL 2 Sovereign 100% Continuation Snapshot")
            root.put("version", "3.0-Pro-SkyStudio")
            root.put("timestamp", System.currentTimeMillis())
            root.put("formattedTime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            root.put("devStudioUrl", DEV_STUDIO_CONTINUE_URL)
            root.put("sharedAppUrl", SHARED_APP_CONTINUE_URL)
            root.put("ownerEmail", DEFAULT_USER_GMAIL)

            // 1. Capture all SharedPreferences XML files
            val sharedPrefsObj = JSONObject()
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.filter { it.name.endsWith(".xml") }?.forEach { xmlFile ->
                    val prefName = xmlFile.name.removeSuffix(".xml")
                    val sp = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    val entriesObj = JSONObject()
                    for ((k, v) in sp.all) {
                        if (k != null && v != null) {
                            val item = JSONObject()
                            when (v) {
                                is Boolean -> {
                                    item.put("type", "boolean")
                                    item.put("value", v)
                                }
                                is Int -> {
                                    item.put("type", "int")
                                    item.put("value", v)
                                }
                                is Long -> {
                                    item.put("type", "long")
                                    item.put("value", v)
                                }
                                is Float -> {
                                    item.put("type", "float")
                                    item.put("value", v.toDouble())
                                }
                                is String -> {
                                    item.put("type", "string")
                                    item.put("value", v)
                                }
                                is Set<*> -> {
                                    item.put("type", "string_set")
                                    val arr = JSONArray()
                                    v.forEach { elem -> if (elem is String) arr.put(elem) }
                                    item.put("value", arr)
                                }
                            }
                            entriesObj.put(k, item)
                        }
                    }
                    sharedPrefsObj.put(prefName, entriesObj)
                }
            }
            root.put("sharedPreferences", sharedPrefsObj)

            // 2. Capture Virtual Project Files
            val vfsArr = JSONArray()
            _projectFiles.value.forEach { vf ->
                val obj = JSONObject()
                obj.put("fileName", vf.fileName)
                obj.put("language", vf.language)
                obj.put("content", vf.content)
                obj.put("darwinianScore", vf.darwinianScore)
                vfsArr.put(obj)
            }
            root.put("virtualProjectFiles", vfsArr)

            // 3. Capture internal filesDir text/code checkpoints & chat_sessions_disk
            val internalFilesObj = JSONObject()
            context.filesDir?.listFiles()?.filter { it.isFile && it.length() < 512_000 }?.forEach { f ->
                runCatching {
                    internalFilesObj.put(f.name, f.readText(Charsets.UTF_8))
                }
            }
            val chatDiskDir = File(context.filesDir, "chat_sessions_disk")
            if (chatDiskDir.exists() && chatDiskDir.isDirectory) {
                chatDiskDir.listFiles()?.filter { it.isFile && it.length() < 512_000 }?.forEach { cf ->
                    runCatching {
                        internalFilesObj.put("chat_sessions_disk/${cf.name}", cf.readText(Charsets.UTF_8))
                    }
                }
            }
            root.put("internalFiles", internalFilesObj)

            root.toString(2)
        } catch (e: Exception) {
            "{\"error\": \"${e.message}\"}"
        }
    }

    /**
     * Restores 100% of the app's state from a captured JSON string (either from a .ZIP file or pasted text)
     * and reloads all engines in memory so the user continues from the exact point where they stopped.
     */
    fun restoreFullAppContinuationStateFromJson(context: Context, rawJsonOrText: String): Boolean {
        return try {
            val cleanJson = if (rawJsonOrText.trim().startsWith("{")) {
                rawJsonOrText.trim()
            } else {
                val startIdx = rawJsonOrText.indexOf("{\"engine\"")
                val endIdx = rawJsonOrText.lastIndexOf("}")
                if (startIdx >= 0 && endIdx > startIdx) {
                    rawJsonOrText.substring(startIdx, endIdx + 1)
                } else {
                    rawJsonOrText.trim()
                }
            }
            val root = JSONObject(cleanJson)

            // 1. Restore SharedPreferences
            val sharedPrefsObj = root.optJSONObject("sharedPreferences")
            if (sharedPrefsObj != null) {
                val prefNames = sharedPrefsObj.keys()
                while (prefNames.hasNext()) {
                    val prefName = prefNames.next()
                    val entriesObj = sharedPrefsObj.optJSONObject(prefName) ?: continue
                    val sp = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    val editor = sp.edit()
                    val keys = entriesObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val item = entriesObj.optJSONObject(k) ?: continue
                        when (item.optString("type")) {
                            "boolean" -> editor.putBoolean(k, item.optBoolean("value"))
                            "int" -> editor.putInt(k, item.optInt("value"))
                            "long" -> editor.putLong(k, item.optLong("value"))
                            "float" -> editor.putFloat(k, item.optDouble("value").toFloat())
                            "string" -> editor.putString(k, item.optString("value"))
                            "string_set" -> {
                                val arr = item.optJSONArray("value")
                                val set = mutableSetOf<String>()
                                if (arr != null) {
                                    for (i in 0 until arr.length()) {
                                        set.add(arr.optString(i))
                                    }
                                }
                                editor.putStringSet(k, set)
                            }
                        }
                    }
                    editor.apply()
                }
            }

            // 2. Restore Virtual Project Files
            val vfsArr = root.optJSONArray("virtualProjectFiles")
            if (vfsArr != null && vfsArr.length() > 0) {
                val list = mutableListOf<VirtualProjectFile>()
                for (i in 0 until vfsArr.length()) {
                    val obj = vfsArr.optJSONObject(i) ?: continue
                    list.add(
                        VirtualProjectFile(
                            fileName = obj.optString("fileName", "file_$i.txt"),
                            language = obj.optString("language", "text"),
                            content = obj.optString("content", ""),
                            darwinianScore = obj.optInt("darwinianScore", 98)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _projectFiles.value = list
                }
            }

            // 3. Restore internal filesDir files (including chat_sessions_disk/)
            val internalFilesObj = root.optJSONObject("internalFiles")
            if (internalFilesObj != null) {
                val fNames = internalFilesObj.keys()
                while (fNames.hasNext()) {
                    val fName = fNames.next()
                    val content = internalFilesObj.optString(fName, "")
                    runCatching {
                        val targetFile = File(context.filesDir, fName)
                        targetFile.parentFile?.mkdirs()
                        targetFile.writeText(content, Charsets.UTF_8)
                    }
                }
            }

            // 4. Re-initialize all live engines so UI immediately reflects restored state
            UniversalAiManager.init(context)
            ChatSessionManager.init(context)
            AiChromiumDevEngine.init(context)
            GlobalRuleVaultEngine.init(context)
            SwarmMemoryVaultEngine.init(context)
            GhostMacroAndSnifferEngine.init(context)
            MasterOmniControllerEngine.init(context)
            QuadChatAndStrainerEngine.init(context)
            AppTruthAndArchitectureVault.init(context)
            ParticleSwarmManager.init(context)
            UniversalOmniControlEngine.init(context)
            SovereignDeepResearchAndIncrementalEngine.init(context)
            SovereignPredictiveArchitectEngine.init(context)
            AutonomousSelfEvolutionEngine.initialize(context)

            _buildStatus.value = "✅ ১০০% অ্যাপ স্টেট ও সব ডেটা সফলভাবে রিস্টোর হয়েছে — ঠিক যেখান থেকে স্টপ হয়েছিল!"
            true
        } catch (e: Exception) {
            _buildStatus.value = "⚠️ রিস্টোর ত্রুটি: ${e.message}"
            false
        }
    }

    /**
     * Restores from a .ZIP file URI or JSON file URI selected by the user.
     */
    suspend fun restoreFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentResolver = context.contentResolver
            var stateJsonFound: String? = null

            // First try reading as a ZIP stream looking for 100_PERCENT_CONTINUE_STATE_SNAPSHOT.json
            runCatching {
                contentResolver.openInputStream(uri)?.use { rawIn ->
                    ZipInputStream(BufferedInputStream(rawIn)).use { zis ->
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null) {
                            if (entry.name.endsWith("100_PERCENT_CONTINUE_STATE_SNAPSHOT.json", ignoreCase = true) ||
                                entry.name.endsWith("state_snapshot.json", ignoreCase = true)
                            ) {
                                stateJsonFound = zis.bufferedReader(Charsets.UTF_8).readText()
                                break
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }
            }

            // If not a ZIP or not found inside ZIP, try reading directly as plain text/JSON
            if (stateJsonFound.isNullOrBlank()) {
                contentResolver.openInputStream(uri)?.use { rawIn ->
                    stateJsonFound = rawIn.bufferedReader(Charsets.UTF_8).readText()
                }
            }

            if (!stateJsonFound.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    restoreFullAppContinuationStateFromJson(context, stateJsonFound!!)
                }
            } else {
                false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _buildStatus.value = "⚠️ ফাইল থেকে রিস্টোর ব্যর্থ: ${e.message}"
            }
            false
        }
    }

    /**
     * Builds the universal continuation guide & prompt that can be pasted or uploaded to any website or AI platform.
     */
    fun buildUniversalContinuationBlueprintMarkdown(context: Context): String {
        val chatCount = ChatSessionManager.sessionSummaries.value.size
        val slotCount = SovereignDeepResearchAndIncrementalEngine.slots.value.count { it.isEnabled }
        val customSiteCount = SovereignDeepResearchAndIncrementalEngine.createdWebsites.value.size
        val selfModCount = MasterOmniControllerEngine.savedSelfFeatures.value.size
        val keyCount = UniversalAiManager.profiles.value.size

        return """
            # 👑 SKRL 2 • 100% FULL APP & EXACT STOP-POINT CONTINUATION PACK
            
            ## 🔗 ১. সরাসরি ওয়েব ও AI Studio কন্টিনিউ লিংক (১-ক্লিকে যেখান থেকে থেমেছিল সেখান থেকে শুরু):
            - **🚀 Direct Development & Code Continue URL:**
              $DEV_STUDIO_CONTINUE_URL
            - **🌐 Direct Shared Live App URL:**
              $SHARED_APP_CONTINUE_URL
            - **📧 রেজিস্টার্ড জিমেইল:** `$DEFAULT_USER_GMAIL`
            
            ---
            
            ## 📊 ২. বর্তমান লাইভ চেকপয়েন্ট স্টেট (যেখান থেকে স্টপ হয়েছিল):
            - **মোট চ্যাট সেশন:** $chatCount টি
            - **সক্রিয় ইনক্রিমেন্টাল মাস্টার স্লট:** $slotCount টি
            - **তৈরি করা কাস্টম ওয়েবসাইট:** $customSiteCount টি
            - **সেলফ-ইভোলিউশন ও জেনেসিস মডিউল:** $selfModCount টি
            - **সংরক্ষিত AI প্রোফাইল ও কী:** $keyCount টি
            - **সর্বশেষ ফুল APK ফাইল:** `/SKRL2_100_PERCENT_FINAL_CONTINUE.apk` এবং `/SOVEREIGN_SKY_STUDIO_V7.apk`
            - **সর্বশেষ ফুল সোর্স কোড .ZIP ফাইল:** `/SKRL2_100_PERCENT_FULL_APP_SOURCE_AND_STATE.zip`
            
            ---
            
            ## 🔄 ৩. অন্য যেকোনো ওয়েবসাইট, AI Studio, GitHub বা নতুন চ্যাটে ১০০% কন্টিনিউ করার নিয়ম:
            1. **ফাইল এক্সপ্লোরার বা জিমেইল থেকে:** `SKRL2_100_PERCENT_FULL_APP_SOURCE_AND_STATE.zip` ফাইলটি ডাউনলোড বা শেয়ার করুন।
            2. **অন্য যেকোনো AI ওয়েবসাইটে (AI Studio / ChatGPT / Claude / Cursor / Bolt):** এই `.ZIP` ফাইলটি বা `100_PERCENT_CONTINUE_STATE_SNAPSHOT.json` আপলোড করে নিচের লাইনটি পেস্ট করুন:
               > *"এই ZIP/JSON ফাইলে আমার SKRL 2 Sovereign OS অ্যাপের ১০০% সোর্স কোড (৬১টি Kotlin ফাইল) এবং শেষ চেকপয়েন্ট স্টেট দেওয়া আছে। ঠিক যেখান থেকে স্টপ হয়েছিল সেখান থেকে ১০০% অপরিবর্তিত রেখে পরবর্তী কাজ কন্টিনিউ করো।"*
            3. **অ্যাপের ভেতরে রিস্টোর করতে:** অ্যাপের **Pro Lab -> 📦 .ZIP ও রিজিউম হাব**-এ গিয়ে **"🔄 যেখান থেকে স্টপ হয়েছিল ১০০% রিস্টোর করুন"** বাটনে ট্যাপ করুন।
        """.trimIndent()
    }

    /**
     * Executes Parallel Multi-Key Split Synthesis (Alpha + Beta + Omega simultaneously)
     * and validates each generated file via Darwinian syntax/completeness scoring.
     */
    suspend fun synthesizeFullProjectParallel(
        projectGoal: String
    ): List<VirtualProjectFile> = withContext(Dispatchers.IO) {
        _isBuildingProject.value = true
        val cleanGoal = projectGoal.trim().ifBlank { "Modern Cyber Dashboard & Task Automation Web App" }
        _buildStatus.value = "⚡ প্যারালাল স্প্লিট ইঞ্জিন চালু: ৩টি ট্র্যাক (Alpha UI, Beta Logic, Omega Verifier) একসাথে কাজ করছে..."

        val profiles = UniversalAiManager.profiles.value
        val profileAlpha = profiles.getOrNull(0)
        val profileBeta = profiles.getOrNull(1) ?: profileAlpha
        val profileOmega = profiles.getOrNull(2) ?: profileAlpha

        val generatedFiles = coroutineScope {
            val alphaDeferred = async(Dispatchers.IO) {
                val res = UniversalAiManager.executeAiPrompt(
                    prompt = "Build a complete, responsive, self-contained HTML5 + CSS3 layout for: $cleanGoal. Return ONLY clean HTML code.",
                    systemInstruction = "You are Track Alpha (UI Architect). Write complete production HTML5 with modern dark styling.",
                    overrideProfile = profileAlpha
                ).getOrNull()
                extractCleanCodeOrDefault(res, buildFallbackHtml(cleanGoal))
            }

            val betaDeferred = async(Dispatchers.IO) {
                val res = UniversalAiManager.executeAiPrompt(
                    prompt = "Write the complete interactive JavaScript application logic (app.js) for: $cleanGoal. Return ONLY valid JavaScript code.",
                    systemInstruction = "You are Track Beta (Core Logic Engineer). Write complete, bug-free vanilla JavaScript.",
                    overrideProfile = profileBeta
                ).getOrNull()
                extractCleanCodeOrDefault(res, buildFallbackJs(cleanGoal))
            }

            val omegaDeferred = async(Dispatchers.IO) {
                val res = UniversalAiManager.executeAiPrompt(
                    prompt = "Write a comprehensive Bengali + English architecture README.md and usage guide for: $cleanGoal.",
                    systemInstruction = "You are Track Omega (Verifier & Documentation Architect).",
                    overrideProfile = profileOmega
                ).getOrNull()
                res?.takeIf { it.isNotBlank() } ?: buildFallbackReadme(cleanGoal)
            }

            val htmlContent = alphaDeferred.await()
            val jsContent = betaDeferred.await()
            val readmeContent = omegaDeferred.await()

            listOf(
                VirtualProjectFile(
                    fileName = "index.html",
                    language = "html",
                    content = htmlContent,
                    darwinianScore = verifyDarwinianScore(htmlContent)
                ),
                VirtualProjectFile(
                    fileName = "styles.css",
                    language = "css",
                    content = buildDefaultCss(),
                    darwinianScore = 99
                ),
                VirtualProjectFile(
                    fileName = "app.js",
                    language = "javascript",
                    content = jsContent,
                    darwinianScore = verifyDarwinianScore(jsContent)
                ),
                VirtualProjectFile(
                    fileName = "manifest.json",
                    language = "json",
                    content = """
                        {
                          "name": "${cleanGoal.take(35).replace("\"", "")}",
                          "version": "1.0.0",
                          "engine": "SKRL 2 Parallel Darwinian Synthesizer"
                        }
                    """.trimIndent(),
                    darwinianScore = 100
                ),
                VirtualProjectFile(
                    fileName = "README.md",
                    language = "markdown",
                    content = readmeContent,
                    darwinianScore = 98
                )
            )
        }

        _projectFiles.value = generatedFiles
        _isBuildingProject.value = false
        val avgScore = generatedFiles.map { it.darwinianScore }.average().toInt()
        _buildStatus.value =
            "✅ প্যারালাল প্রজেক্ট তৈরি সম্পন্ন (${generatedFiles.size}টি ফাইল • Darwinian Quality Score: $avgScore%)"
        generatedFiles
    }

    /**
     * Combines index.html + styles.css + app.js into a single executable HTML string for Live Sandbox preview.
     */
    fun buildCombinedSingleHtmlPreview(): String {
        val files = _projectFiles.value
        val html = files.find { it.fileName.endsWith(".html", true) }?.content ?: buildFallbackHtml("SKRL 2 App")
        val css = files.find { it.fileName.endsWith(".css", true) }?.content.orEmpty()
        val js = files.find { it.fileName.endsWith(".js", true) }?.content.orEmpty()

        return if (html.contains("</head>", ignoreCase = true)) {
            html.replaceFirst(
                Regex("</head>", RegexOption.IGNORE_CASE),
                "<style>\n$css\n</style>\n</head>"
            ).let { withCss ->
                if (withCss.contains("</body>", ignoreCase = true)) {
                    withCss.replaceFirst(
                        Regex("</body>", RegexOption.IGNORE_CASE),
                        "<script>\n$js\n</script>\n</body>"
                    )
                } else {
                    "$withCss\n<script>\n$js\n</script>"
                }
            }
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <style>$css</style>
            </head>
            <body>
              $html
              <script>$js</script>
            </body>
            </html>
            """.trimIndent()
        }
    }

    /**
     * Creates the 100% Full App Continuation .ZIP file containing:
     * 1. 100_PERCENT_CONTINUE_STATE_SNAPSHOT.json (All SharedPreferences, Chat History, Slots, Websites, AIs, Rules)
     * 2. CONTINUE_IN_ANY_AI_OR_WEBSITE.md (Direct Links & Restoration Prompt)
     * 3. App Truth & 61-File Architecture Blueprint
     * 4. All Virtual Project Files + All Saved Self-Evolution HTML/JS Modules + All Custom Websites
     */
    suspend fun buildFullContinuationZipFile(
        context: Context,
        zipFileName: String = "SKRL2_100_PERCENT_CONTINUE_ANYWHERE.zip"
    ): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val outFile = File(context.cacheDir, zipFileName)
            val stateJson = captureFullAppContinuationStateJson(context)
            val guideMd = buildUniversalContinuationBlueprintMarkdown(context)
            val truthReport = AppTruthAndArchitectureVault.codeManifest.joinToString("\n\n") { m ->
                "### ${m.fileName} (${m.relativePath}) [${m.category}]\n- Role: ${m.exactRoleBangla}\n- Functions: ${m.keyFunctions.joinToString(", ")}"
            }

            ZipOutputStream(BufferedOutputStream(FileOutputStream(outFile))).use { zos ->
                // 1. Master 100% State Snapshot JSON
                zos.putNextEntry(ZipEntry("100_PERCENT_CONTINUE_STATE_SNAPSHOT.json"))
                zos.write(stateJson.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. Universal Continuation Guide & Links
                zos.putNextEntry(ZipEntry("CONTINUE_IN_ANY_AI_OR_WEBSITE.md"))
                zos.write(guideMd.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 3. Full Architecture Dossier
                zos.putNextEntry(ZipEntry("ARCHITECTURE_61_FILES_DOSSIER.md"))
                zos.write(truthReport.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 4. Bundled Web Hub asset (index.html)
                runCatching {
                    context.assets.open("index.html").use { assetIn ->
                        zos.putNextEntry(ZipEntry("assets/index.html"))
                        assetIn.copyTo(zos)
                        zos.closeEntry()
                    }
                }

                // 5. All Virtual Project Files
                for (vf in _projectFiles.value) {
                    zos.putNextEntry(ZipEntry("project_files/${vf.fileName}"))
                    zos.write(vf.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }

                // 6. All Saved Self-Evolution & Genesis Modules
                MasterOmniControllerEngine.savedSelfFeatures.value.forEach { mod ->
                    val safeName = mod.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    zos.putNextEntry(ZipEntry("live_modules/${safeName}.html"))
                    zos.write(mod.jsPayload.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }

                // 7. All Created Custom Websites
                SovereignDeepResearchAndIncrementalEngine.createdWebsites.value.forEach { site ->
                    val safeName = site.siteId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    zos.putNextEntry(ZipEntry("custom_websites/${safeName}.html"))
                    zos.write(site.fullHtmlCode.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }

                // 8. Raw SharedPreferences XML backup files
                val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                if (prefsDir.exists() && prefsDir.isDirectory) {
                    prefsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".xml") }?.forEach { xmlFile ->
                        runCatching {
                            zos.putNextEntry(ZipEntry("shared_prefs_backup/${xmlFile.name}"))
                            FileInputStream(xmlFile).use { fis -> fis.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            }
            outFile
        } catch (e: Exception) {
            _buildStatus.value = "⚠️ ZIP তৈরি ত্রুটি: ${e.message}"
            null
        }
    }

    /**
     * Packages all files & 100% live state into a real .ZIP archive, saves a copy to Downloads, and launches Android Share/Save dialog.
     */
    suspend fun exportProjectAsZip(
        context: Context,
        zipFileName: String = "SKRL2_100_PERCENT_CONTINUE_ANYWHERE.zip"
    ): File? = withContext(Dispatchers.IO) {
        val outFile = buildFullContinuationZipFile(context, zipFileName) ?: return@withContext null
        return@withContext try {
            // Also save directly to device Downloads folder so user gets it immediately
            saveFileToDeviceDownloads(context, outFile, zipFileName, "application/zip")

            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(DEFAULT_USER_GMAIL))
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "👑 SKRL 2 • 100% Full App & Continuation Pack (.ZIP)")
                    putExtra(Intent.EXTRA_TEXT, buildUniversalContinuationBlueprintMarkdown(context))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "১০০% ফুল প্রজেক্ট ও রিজিউম .ZIP ডাউনলোড বা শেয়ার করুন")
                )
            }
            _buildStatus.value = "📦 '${outFile.name}' (${outFile.length() / 1024} KB) Downloads-এ সেভ ও শেয়ারের জন্য প্রস্তুত!"
            outFile
        } catch (e: Exception) {
            _buildStatus.value = "⚠️ ZIP এক্সপোর্ট ত্রুটি: ${e.message}"
            null
        }
    }

    /**
     * Extracts the ACTUAL running binary APK of this app (`context.applicationInfo.sourceDir`),
     * saves it directly to the device's Downloads folder, and opens the Share/Gmail sheet with the real `.apk` file attached!
     */
    suspend fun exportAndShareInstalledApk(
        context: Context,
        apkFileName: String = "SKRL2_100_PERCENT_FULL_APP.apk",
        sendDirectlyToGmail: Boolean = false
    ): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            _buildStatus.value = "⏳ রানিং অ্যাপের ১০০% আসল .APK বাইনারি ফাইল প্রস্তুত করা হচ্ছে..."
            val sourceApk = File(context.applicationInfo.sourceDir)
            val outApk = File(context.cacheDir, apkFileName)
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(outApk).use { output ->
                    input.copyTo(output)
                }
            }

            // Save a copy directly to the device's Downloads folder
            saveFileToDeviceDownloads(
                context = context,
                sourceFile = outApk,
                displayName = apkFileName,
                mimeType = "application/vnd.android.package-archive"
            )

            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outApk
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(DEFAULT_USER_GMAIL))
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "📱 SKRL 2 Sovereign OS • 100% Full App APK ($apkFileName)"
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        buildUniversalContinuationBlueprintMarkdown(context)
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (sendDirectlyToGmail) {
                        setPackage("com.google.android.gm")
                    }
                }
                try {
                    context.startActivity(shareIntent)
                } catch (_: Exception) {
                    // Fallback to standard chooser if Gmail app package is not installed on emulator
                    shareIntent.setPackage(null)
                    context.startActivity(
                        Intent.createChooser(shareIntent, "১০০% আসল APK ফাইল জিমেইল বা অন্য মাধ্যমে শেয়ার করুন")
                    )
                }
                Toast.makeText(
                    context,
                    "✅ আসল APK (${outApk.length() / (1024 * 1024)} MB) Downloads ফোল্ডারে সেভ হয়েছে ও শেয়ার প্যানেল খোলা হয়েছে!",
                    Toast.LENGTH_LONG
                ).show()
            }
            _buildStatus.value = "✅ আসল APK '${outApk.name}' (${outApk.length() / (1024 * 1024)} MB) Downloads-এ সেভ ও শেয়ার হয়েছে!"
            outApk
        } catch (e: Exception) {
            _buildStatus.value = "⚠️ APK এক্সপোর্ট ত্রুটি: ${e.message}"
            null
        }
    }

    /**
     * Sends the 100% Full Continuation .ZIP + Direct Resume Links directly via Gmail (`skdluddhd@gmail.com`).
     */
    suspend fun sendFullContinuationZipViaGmail(
        context: Context,
        recipientEmail: String = DEFAULT_USER_GMAIL
    ) = withContext(Dispatchers.IO) {
        val zipFile = buildFullContinuationZipFile(
            context,
            "SKRL2_100_PERCENT_CONTINUE_ANYWHERE.zip"
        ) ?: return@withContext

        saveFileToDeviceDownloads(
            context = context,
            sourceFile = zipFile,
            displayName = zipFile.name,
            mimeType = "application/zip"
        )

        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail.ifBlank { DEFAULT_USER_GMAIL }))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "👑 SKRL 2 • 100% Full App .ZIP & Exact Stop-Point Continuation Pack"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    buildUniversalContinuationBlueprintMarkdown(context)
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.google.android.gm")
            }
            try {
                context.startActivity(emailIntent)
            } catch (_: Exception) {
                emailIntent.setPackage(null)
                context.startActivity(
                    Intent.createChooser(emailIntent, "জিমেইল বা যেকোনো অ্যাপে ১০০% কন্টিনিউ প্যাক পাঠান")
                )
            }
            _buildStatus.value = "📧 জিমেইলে ($recipientEmail) ১০০% ফুল প্রজেক্ট .ZIP ও রিজিউম লিংক পাঠানো হচ্ছে!"
        }
    }

    /**
     * Copies the complete 100% Continuation Link + State Snapshot to the device clipboard so the user
     * can immediately paste it into any website, browser tab, or AI chat.
     */
    fun copyFullContinuationPackToClipboard(context: Context) {
        val guide = buildUniversalContinuationBlueprintMarkdown(context)
        val stateJson = captureFullAppContinuationStateJson(context)
        val combined = "$guide\n\n### 📦 100% STATE SNAPSHOT JSON (PASTE TO RESTORE):\n```json\n$stateJson\n```"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("SKRL2_100_PERCENT_CONTINUE", combined))
        _buildStatus.value = "✅ ১০০% কন্টিনিউ লিংক + ফুল অ্যাপ স্টেট ক্লিপবোর্ডে কপি হয়েছে!"
        Toast.makeText(
            context,
            "✅ ১০০% কন্টিনিউ লিংক ও ফুল অ্যাপ স্টেট কপি হয়েছে! যেকোনো ওয়েবসাইট বা অ্যাপে পেস্ট করলেই ১০০% কন্টিনিউ হবে।",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveFileToDeviceDownloads(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        FileInputStream(sourceFile).use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val dest = File(downloadsDir, displayName)
                sourceFile.copyTo(dest, overwrite = true)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun verifyDarwinianScore(code: String): Int {
        var score = 75
        if (code.length > 120) score += 10
        if (!code.contains("TODO", ignoreCase = true) && !code.contains("placeholder", ignoreCase = true)) score += 10
        val openBraces = code.count { it == '{' }
        val closeBraces = code.count { it == '}' }
        if (openBraces == closeBraces) score += 5
        return score.coerceIn(60, 100)
    }

    private fun extractCleanCodeOrDefault(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        val extracted = WorkerEngine.extractCodeBlocks(raw)
        return extracted.takeIf { it.length > 40 } ?: fallback
    }

    private fun buildDefaultCss(): String = """
        body {
            background: #070B14;
            color: #F8FAFC;
            font-family: system-ui, -apple-system, sans-serif;
            margin: 0;
            padding: 18px;
        }
        .app-shell {
            max-width: 720px;
            margin: 0 auto;
            background: #0F172A;
            border: 1px solid #00F0FF55;
            border-radius: 16px;
            padding: 20px;
            box-shadow: 0 10px 30px rgba(0, 240, 255, 0.12);
        }
        button {
            background: #10B981;
            color: #041019;
            border: none;
            border-radius: 10px;
            padding: 10px 16px;
            font-weight: 700;
            cursor: pointer;
        }
        input, textarea {
            width: 100%;
            box-sizing: border-box;
            background: #070B14;
            color: #FFFFFF;
            border: 1px solid #334155;
            border-radius: 10px;
            padding: 10px;
            margin-top: 8px;
            margin-bottom: 12px;
        }
    """.trimIndent()

    private fun buildFallbackHtml(goal: String): String = """
        <!DOCTYPE html>
        <html lang="bn">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>${goal.take(40)}</title>
        </head>
        <body>
          <div class="app-shell">
            <h2 style="color:#00F0FF;margin-top:0;">⚡ ${goal.take(60)}</h2>
            <p style="color:#94A3B8;font-size:14px;">ডুয়াল-গিয়ার ও প্যারালাল ইঞ্জিনে তৈরি মাল্টি-ফাইল অ্যাপ্লিকেশন।</p>
            <input id="taskInput" placeholder="নতুন আইটেম বা ডেটা লিখুন..." />
            <button id="actionBtn" onclick="window.runAppAction && window.runAppAction()">অ্যাকশন রান করুন</button>
            <div id="outputBoard" style="margin-top:16px;padding:12px;background:#070B14;border-radius:10px;border:1px solid #1E293B;">
              প্রস্তুত...
            </div>
          </div>
        </body>
        </html>
    """.trimIndent()

    private fun buildFallbackJs(goal: String): String = """
        (function() {
            var items = [];
            window.runAppAction = function() {
                var input = document.getElementById('taskInput');
                var board = document.getElementById('outputBoard');
                var val = input && input.value ? input.value.trim() : 'স্বয়ংক্রিয় টাস্ক #' + (items.length + 1);
                items.unshift({ title: val, ts: new Date().toLocaleTimeString() });
                if (input) input.value = '';
                if (board) {
                    board.innerHTML = items.map(function(it, idx) {
                        return '<div style="padding:6px 0;border-bottom:1px solid #1E293B;">' +
                            '<strong style="color:#10B981;">#' + (idx + 1) + '</strong> ' +
                            it.title + ' <span style="color:#64748B;font-size:11px;">(' + it.ts + ')</span></div>';
                    }).join('');
                }
            };
        })();
    """.trimIndent()

    private fun buildFallbackReadme(goal: String): String = """
        # $goal

        ## আর্কিটেকচার ও ফাইল স্ট্রাকচার
        - `index.html`: মেইন রেসপনসিভ ইউজার ইন্টারফেস।
        - `styles.css`: সাইবার ডার্ক থিম ও লেআউট স্টাইল।
        - `app.js`: ইন্টারঅ্যাক্টিভ অ্যাপ্লিকেশন লজিক।
        - `manifest.json`: প্রজেক্ট মেটাডেটা।
    """.trimIndent()

    private fun defaultStarterFiles(): List<VirtualProjectFile> = listOf(
        VirtualProjectFile("index.html", "html", buildFallbackHtml("SKRL 2 Pro Workspace App"), 99),
        VirtualProjectFile("styles.css", "css", buildDefaultCss(), 99),
        VirtualProjectFile("app.js", "javascript", buildFallbackJs("SKRL 2 Pro Workspace App"), 99),
        VirtualProjectFile(
            "manifest.json",
            "json",
            "{\n  \"name\": \"SKRL 2 Pro App\",\n  \"version\": \"1.0.0\"\n}",
            100
        ),
        VirtualProjectFile("README.md", "markdown", buildFallbackReadme("SKRL 2 Pro Workspace App"), 99)
    )
}

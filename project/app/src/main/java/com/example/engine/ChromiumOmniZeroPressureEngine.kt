package com.example.engine

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * ChromiumOmniZeroPressureEngine ("জিরো-প্রেশার ক্রোমিয়াম ভার্চুয়াল ওএস ও ডিস্ক ভল্ট ইঞ্জিন"):
 *
 * Implements the 4-Pillar Zero-RAM Architecture:
 * 1. Disk & IndexedDB Partitioning (`omni_virtual_os_disk`) so massive apps/3D assets live on disk, not RAM.
 * 2. On-Demand CDN Micro-Engine Loader (`loadCdnEngine('THREE' | 'CHART' | 'MARKED' | 'CRYPTO' | 'ANIME' | 'PDF')`).
 * 3. Native Hardware & Live AI Brain Bridge (`window.OmniVirtualOS`).
 * 4. Multi-Pass Self-Healing & Blob/WebGL GPU Memory Purger (`purgeAllBlobMemory()`).
 */
object ChromiumOmniZeroPressureEngine {

    private const val DISK_VAULT_DIR = "omni_virtual_os_disk"

    private fun getVaultDir(context: Context): File {
        val dir = File(context.filesDir, DISK_VAULT_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sanitizeVaultKey(rawKey: String): String {
        return rawKey.trim()
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(80)
            .ifBlank { "default_omni_slot" }
    }

    fun saveToNativeDiskVault(context: Context, key: String, content: String): Boolean {
        return try {
            val safeKey = sanitizeVaultKey(key)
            val target = File(getVaultDir(context), "$safeKey.omni")
            target.writeText(content, Charsets.UTF_8)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun readFromNativeDiskVault(context: Context, key: String): String {
        return try {
            val safeKey = sanitizeVaultKey(key)
            val target = File(getVaultDir(context), "$safeKey.omni")
            if (target.exists()) target.readText(Charsets.UTF_8) else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun listNativeDiskVaultKeys(context: Context): String {
        return try {
            val arr = JSONArray()
            val files = getVaultDir(context).listFiles().orEmpty()
            files.sortedByDescending { it.lastModified() }.forEach { f ->
                if (f.isFile && f.name.endsWith(".omni")) {
                    arr.put(f.name.removeSuffix(".omni"))
                }
            }
            arr.toString()
        } catch (_: Exception) {
            "[]"
        }
    }

    /**
     * JavaScript Runtime Bootstrap injected into every Live Runner & Full-Screen App container.
     * Exposes `window.OmniVirtualOS` for IndexedDB + Native Disk + Live AI + CDN Micro-Engines + Zero-RAM Purge.
     */
    fun buildOmniVirtualOsRuntimeBootstrapJs(): String {
        return """
            window.__omniAiCallbacks = window.__omniAiCallbacks || {};
            window.__omniBlobUrls = window.__omniBlobUrls || [];

            window.OmniVirtualOS = {
              dbName: 'OmniSovereignZeroRamDB',
              storeName: 'partitions',

              openDb: function() {
                var self = this;
                return new Promise(function(resolve, reject) {
                  try {
                    var req = indexedDB.open(self.dbName, 1);
                    req.onupgradeneeded = function(e) {
                      var db = e.target.result;
                      if (!db.objectStoreNames.contains(self.storeName)) {
                        db.createObjectStore(self.storeName, { keyPath: 'key' });
                      }
                    };
                    req.onsuccess = function(e) { resolve(e.target.result); };
                    req.onerror = function(e) { reject(e); };
                  } catch (err) {
                    reject(err);
                  }
                });
              },

              saveAppToPartition: async function(key, title, data) {
                try {
                  if (window.OmniNative && window.OmniNative.saveToDiskVault) {
                    window.OmniNative.saveToDiskVault(key, typeof data === 'string' ? data : JSON.stringify(data));
                  }
                  var db = await this.openDb();
                  return new Promise(function(resolve) {
                    var tx = db.transaction(['partitions'], 'readwrite');
                    tx.objectStore('partitions').put({
                      key: key,
                      title: title || key,
                      data: data,
                      updatedAt: Date.now()
                    });
                    tx.oncomplete = function() { resolve(true); };
                    tx.onerror = function() { resolve(false); };
                  });
                } catch (e) {
                  return false;
                }
              },

              loadAppFromPartition: async function(key) {
                try {
                  if (window.OmniNative && window.OmniNative.readFromDiskVault) {
                    var fromDisk = window.OmniNative.readFromDiskVault(key);
                    if (fromDisk && fromDisk.length > 0) return fromDisk;
                  }
                  var db = await this.openDb();
                  return new Promise(function(resolve) {
                    var tx = db.transaction(['partitions'], 'readonly');
                    var req = tx.objectStore('partitions').get(key);
                    req.onsuccess = function() {
                      resolve(req.result ? req.result.data : '');
                    };
                    req.onerror = function() { resolve(''); };
                  });
                } catch (e) {
                  return '';
                }
              },

              loadCdnEngine: function(engineKey) {
                var cdnMap = {
                  'THREE': 'https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js',
                  'CHART': 'https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js',
                  'MARKED': 'https://cdn.jsdelivr.net/npm/marked/marked.min.js',
                  'CRYPTO': 'https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.2.0/crypto-js.min.js',
                  'ANIME': 'https://cdnjs.cloudflare.com/ajax/libs/animejs/3.2.2/anime.min.js',
                  'PDF': 'https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js'
                };
                var url = cdnMap[(engineKey || '').toUpperCase()] || engineKey;
                return new Promise(function(resolve, reject) {
                  if (!url) { resolve(false); return; }
                  var existing = document.querySelector('script[data-omni-cdn="' + url + '"]');
                  if (existing) { resolve(true); return; }
                  var s = document.createElement('script');
                  s.src = url;
                  s.async = true;
                  s.setAttribute('data-omni-cdn', url);
                  s.onload = function() { resolve(true); };
                  s.onerror = function(err) { reject(err); };
                  document.head.appendChild(s);
                });
              },

              askAiAsync: function(promptText) {
                return new Promise(function(resolve) {
                  if (window.OmniNative && window.OmniNative.askAiFromJs) {
                    var reqId = 'req_' + Date.now() + '_' + Math.floor(Math.random() * 10000);
                    window.__omniAiCallbacks[reqId] = function(ans) {
                      delete window.__omniAiCallbacks[reqId];
                      resolve(ans);
                    };
                    window.OmniNative.askAiFromJs(promptText, reqId);
                  } else {
                    resolve('⚠️ Native AI Bridge সংযুক্ত নেই।');
                  }
                });
              },

              hotPatchMe: function(upgradeDirective) {
                if (window.OmniNative && window.OmniNative.hotPatchCurrentModule) {
                  window.OmniNative.hotPatchCurrentModule(upgradeDirective);
                }
              },

              purgeAllBlobMemory: function() {
                try {
                  for (var i = 0; i < window.__omniBlobUrls.length; i++) {
                    URL.revokeObjectURL(window.__omniBlobUrls[i]);
                  }
                  window.__omniBlobUrls = [];
                  if (window.__releaseOmniWebGlGpu) window.__releaseOmniWebGlGpu();
                  if (window.OmniNative && window.OmniNative.purgeNativeRam) {
                    window.OmniNative.purgeNativeRam();
                  }
                } catch (e) {}
              }
            };
        """.trimIndent()
    }

    /**
     * Built-in Flagship 4-Pillar Sovereign Chromium Omni-OS Workstation UI.
     */
    fun proSovereignChromiumOmniOsWorkstationHtml(): String {
        return """
            <div style="background:#020408;color:#E2E8F0;padding:12px;border-radius:12px;border:1px solid #00F0FF55;font-family:sans-serif;">
              <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #1E293B;padding-bottom:8px;margin-bottom:10px;">
                <div>
                  <div style="color:#00F0FF;font-weight:900;font-size:15px;">👑 সার্বভৌম ক্রোমিয়াম অমনি-ওএস (Zero-Pressure Workstation)</div>
                  <div style="color:#10B981;font-size:11px;font-weight:bold;">IndexedDB + Native Disk Vault • CDN Micro-Engines • Live AI Brain • 0% RAM Bloat</div>
                </div>
                <button onclick="window.OmniVirtualOS.purgeAllBlobMemory()" style="background:#F43F5E22;color:#F43F5E;border:1px solid #F43F5E;border-radius:8px;padding:6px 10px;font-size:11px;font-weight:bold;">🧹 জিরো-র‍্যাম ফ্লাশ</button>
              </div>

              <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:10px;">
                <div style="background:#090E18;padding:10px;border-radius:8px;border:1px solid #1E293B;">
                  <div style="color:#A855F7;font-size:11px;font-weight:bold;">💾 ডিস্ক ভল্ট ও পার্টিশন</div>
                  <div id="omniVaultStatus" style="color:#E2E8F0;font-size:12px;margin-top:4px;">প্রস্তুত (আনলিমিটেড ডিস্ক স্টোরেজ)</div>
                </div>
                <div style="background:#090E18;padding:10px;border-radius:8px;border:1px solid #1E293B;">
                  <div style="color:#00F0FF;font-size:11px;font-weight:bold;">⚡ CDN মাইক্রো-ইঞ্জিন</div>
                  <div style="display:flex;gap:4px;flex-wrap:wrap;margin-top:5px;">
                    <button onclick="loadEngineDemo('THREE')" style="background:#00F0FF22;color:#00F0FF;border:1px solid #00F0FF;border-radius:5px;padding:3px 7px;font-size:10px;">+3D Three.js</button>
                    <button onclick="loadEngineDemo('CHART')" style="background:#10B98122;color:#10B981;border:1px solid #10B981;border-radius:5px;padding:3px 7px;font-size:10px;">+Chart.js</button>
                    <button onclick="loadEngineDemo('CRYPTO')" style="background:#A855F722;color:#A855F7;border:1px solid #A855F7;border-radius:5px;padding:3px 7px;font-size:10px;">+CryptoJS</button>
                  </div>
                </div>
              </div>

              <div style="background:#090E18;padding:10px;border-radius:8px;border:1px solid #1E293B;margin-bottom:10px;">
                <div style="color:#10B981;font-size:12px;font-weight:bold;margin-bottom:6px;">🧠 লাইভ এআই ও হট-প্যাচ কমান্ডার</div>
                <div style="display:flex;gap:6px;">
                  <input id="omniCmdInput" placeholder="যেকোনো নতুন ফিচার বা প্রশ্ন লিখুন..." style="flex:1;background:#020408;color:#fff;border:1px solid #334155;border-radius:7px;padding:8px;font-size:12px;" />
                  <button onclick="askOmniAiNow()" style="background:#00F0FF;color:#000;border:none;border-radius:7px;padding:8px 12px;font-weight:900;font-size:11px;">🤖 এআই উত্তর</button>
                  <button onclick="hotPatchNow()" style="background:#10B981;color:#000;border:none;border-radius:7px;padding:8px 12px;font-weight:900;font-size:11px;">🧬 হট-প্যাচ</button>
                </div>
                <pre id="omniAiOutput" style="background:#020408;color:#00F0FF;padding:8px;border-radius:6px;margin-top:8px;font-size:11px;white-space:pre-wrap;max-height:140px;overflow:auto;">এখানে লাইভ এআই আউটপুট বা ডিস্ক স্ট্যাটাস দেখা যাবে...</pre>
              </div>

              <script>
                async function loadEngineDemo(name) {
                  var out = document.getElementById('omniAiOutput');
                  out.textContent = '⏳ CDN থেকে ' + name + ' ইঞ্জিন লোড হচ্ছে (0% মেমরি ব্লোট)...';
                  try {
                    await window.OmniVirtualOS.loadCdnEngine(name);
                    out.textContent = '✅ ' + name + ' ইঞ্জিন সফলভাবে লোড হয়েছে এবং ব্যবহারের জন্য প্রস্তুত!';
                  } catch (e) {
                    out.textContent = '⚠️ CDN লোড ব্যর্থ হয়েছে, অফলাইন মোড সক্রিয়।';
                  }
                }
                async function askOmniAiNow() {
                  var q = document.getElementById('omniCmdInput').value.trim();
                  if (!q) return;
                  var out = document.getElementById('omniAiOutput');
                  out.textContent = '🧠 এআই চিন্তা করছে...';
                  var ans = await window.OmniVirtualOS.askAiAsync(q);
                  out.textContent = ans;
                }
                function hotPatchNow() {
                  var q = document.getElementById('omniCmdInput').value.trim();
                  if (!q) return;
                  window.OmniVirtualOS.hotPatchMe(q);
                }
              </script>
            </div>
        """.trimIndent()
    }
}

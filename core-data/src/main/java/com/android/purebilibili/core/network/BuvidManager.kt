package com.android.purebilibili.core.network

import com.android.purebilibili.core.store.TokenManager
import kotlinx.coroutines.CancellationException

/**
 * buvid3 设备身份引导：缺失时通过 SPI 获取并激活。原实现位于手机 VideoRepository，
 * 评论读取等共享仓库也需要同一初始化，故按 WbiKeyManager 的方式下沉为共享能力；
 * 手机端原入口改为委托，避免两份初始化状态。
 */
object BuvidManager {
    private val buvidApi = NetworkModule.buvidApi

    @Volatile
    private var buvidInitialized = false

    suspend fun ensureBuvid3() {
        // 会话备份为异步恢复，先等它完成再判断 buvid 是否缺失，避免启动窗口内多打一次 SPI。
        TokenManager.awaitRestore()
        if (buvidInitialized) return
        try {
            CoreDataLog.d("BuvidManager", " Fetching buvid3 from SPI API...")
            val response = buvidApi.getSpi()
            if (response.code == 0 && response.data != null) {
                val checkedResponseData = requireNotNull(response.data)
                val b3 = checkedResponseData.b_3
                if (b3.isNotEmpty()) {
                    TokenManager.buvid3Cache = b3
                    CoreDataLog.d("BuvidManager", " buvid3 from SPI: ${b3.take(20)}...")

                    //  [关键] 激活 buvid (参考 PiliPala)
                    try {
                        activateBuvid()
                        CoreDataLog.d("BuvidManager", " buvid activated!")
                    } catch (e: Exception) {
                        CoreDataLog.w("BuvidManager", "buvid activation failed: ${e.message}")
                    }

                    buvidInitialized = true
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CoreDataLog.e("BuvidManager", " Failed to get buvid3 from SPI: ${e.message}")
        }
    }

    //  激活 buvid (参考 PiliPala buvidActivate)
    private suspend fun activateBuvid() {
        val random = java.util.Random()
        val randBytes = ByteArray(32) { random.nextInt(256).toByte() }
        val endBytes = byteArrayOf(0, 0, 0, 0, 73, 69, 78, 68) + ByteArray(4) { random.nextInt(256).toByte() }
        val randPngEnd = android.util.Base64.encodeToString(randBytes + endBytes, android.util.Base64.NO_WRAP)

        val payload = org.json.JSONObject().apply {
            put("3064", 1)
            put("39c8", "333.999.fp.risk")
            put("3c43", org.json.JSONObject().apply {
                put("adca", "Windows") // 与 User-Agent (Windows NT 10.0) 保持一致
                put("bfe9", randPngEnd.takeLast(50))
            })
        }.toString()

        buvidApi.activateBuvid(payload)
    }
}

package com.dietrecord.app.core.network

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    private const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/"
    private const val REAL_DEVICE_BASE_URL = "http://127.0.0.1:8080/"
    private const val CONNECT_TIMEOUT_SECONDS = 20L
    private const val IO_TIMEOUT_SECONDS = 90L

    val DEFAULT_BASE_URL: String
        get() = if (isRunningOnEmulator()) EMULATOR_BASE_URL else REAL_DEVICE_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createDietApiService(baseUrl: String = DEFAULT_BASE_URL): DietApiService {
        return buildRetrofit(baseUrl).create(DietApiService::class.java)
    }

    val dietApiService: DietApiService by lazy {
        createDietApiService()
    }

    /**
     * 模拟器继续访问宿主机的 10.0.2.2，真机通过 adb reverse 访问本机 127.0.0.1。
     */
    private fun isRunningOnEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.BRAND.startsWith("generic")
            || Build.DEVICE.startsWith("generic")
            || Build.PRODUCT == "google_sdk"
    }
}

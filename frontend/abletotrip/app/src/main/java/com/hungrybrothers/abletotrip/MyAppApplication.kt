package com.hungrybrothers.abletotrip

import android.app.Application
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.kakao.sdk.common.KakaoSdk

class MyAppApplication : Application() {
    lateinit var placesClient: PlacesClient

    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        initializeKakaoSDK()

        // Places API 초기화
        initializePlacesAPI()
    }

    private fun initializeKakaoSDK() {
        // "3e467ddd4cc251a02dcd9b746240a5d6"는 예시 키입니다. 실제 사용 시 안전한 저장 방법을 고려하세요.
        KakaoSdk.init(this, "3e467ddd4cc251a02dcd9b746240a5d6")
        Log.d("카카오", "Kakao SDK initialized successfully")
    }

    private fun initializePlacesAPI() {
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("장소", "No API key")
            return
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        placesClient = Places.createClient(this)
        Log.d("장소", "Places API initialized successfully")
    }
}

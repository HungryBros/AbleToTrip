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
        initializeKakaoSDK()
        initializePlacesAPI()
    }

    private fun initializeKakaoSDK() {
        val apikey = BuildConfig.KAKAO_API_KEY
        KakaoSdk.init(this, "$apikey")
        Log.d("카카오", "Kakao SDK initialized successfully")
    }

    private fun initializePlacesAPI() {
        val apiKey = BuildConfig.google_api_key
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

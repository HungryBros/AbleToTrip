package com.hungrybrothers.abletotrip

import android.app.Application
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class PlaceApplication : Application() {
    lateinit var placesClient: PlacesClient

    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.PLACES_API_KEY

        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("장소", "No api key")
            return
        }
        Log.d("test성공", "테스트 성공")

        Places.initialize(applicationContext, apiKey)

        val placesClient = Places.createClient(this)
    }
}

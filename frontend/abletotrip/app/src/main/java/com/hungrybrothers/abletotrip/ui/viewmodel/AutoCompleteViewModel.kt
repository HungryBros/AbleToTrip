package com.hungrybrothers.abletotrip.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hungrybrothers.abletotrip.BuildConfig

class AutoCompleteViewModel(application: Application) : AndroidViewModel(application) {
    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places
    private var placesClient: PlacesClient = Places.createClient(application)

    fun queryPlaces(query: String) {
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("장소", "No api key")
            return
        }
        Log.d("test성공", "테스트 성공")

        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .setCountries("KR")
                .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val placesList =
                response.autocompletePredictions.map { prediction ->
                    // 각 prediction 정보에서 ID와 이름을 추출하여 Place 객체 생성
                    Place.builder()
                        .setId(prediction.placeId)
                        .setName(prediction.getPrimaryText(null).toString())
                        .setAddress(prediction.getSecondaryText(null).toString())
                        .build()
                }
            Log.d("Places API", "Predictions received: $placesList")
            _places.postValue(placesList)
        }.addOnFailureListener { exception ->
            Log.e("Places API", "Error fetching predictions", exception)
        }
    }
}

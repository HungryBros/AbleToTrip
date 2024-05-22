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

class PlaceCompleteViewModel(application: Application) : AndroidViewModel(application) {
    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places
    private val placesClient: PlacesClient by lazy {
        Places.createClient(application)
    }

    fun queryPlaces(query: String) {
        val apiKey = BuildConfig.google_api_key
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            return
        }

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
                    Place.builder()
                        .setId(prediction.placeId)
                        .setName(prediction.getPrimaryText(null).toString())
                        .setAddress(prediction.getSecondaryText(null).toString())
                        .build()
                }
            _places.postValue(placesList)
        }.addOnFailureListener { exception ->
            Log.e("Places: PlaceModelView Error", "Error fetching predictions", exception)
        }
        Log.d("Places : _places값 ", "업데이트된 값 ${_places.value}")
    }
}

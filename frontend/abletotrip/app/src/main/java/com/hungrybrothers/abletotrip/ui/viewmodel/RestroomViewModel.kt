package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RestroomViewModel : ViewModel() {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    private val _restrooms = MutableLiveData<List<Restroom>>()
    val restrooms: LiveData<List<Restroom>> = _restrooms

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchRestrooms() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response: RestroomResponse =
                    client
                        .get("http://k10a607.p.ssafy.io:8087/navigation/restroom/").body()
                _restrooms.postValue(response.restrooms)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}

@Serializable
data class RestroomCoordinate(
    val longitude: Double,
    val latitude: Double,
)

@Serializable
data class Restroom(
    val station_fullname: String,
    val is_outside: Boolean,
    val floor: String,
    val restroom_location: String,
    val coordinate: RestroomCoordinate,
)

@Serializable
data class RestroomResponse(
    val restrooms: List<Restroom>,
)

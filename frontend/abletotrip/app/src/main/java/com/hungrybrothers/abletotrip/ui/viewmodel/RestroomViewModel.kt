package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.network.KtorClient.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RestroomViewModel : ViewModel() {
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
//                        .get("http://10.0.2.2:8000/navigation/restroom/").body()
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

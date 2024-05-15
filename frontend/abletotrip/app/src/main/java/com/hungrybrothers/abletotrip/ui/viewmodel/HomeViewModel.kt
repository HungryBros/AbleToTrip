package com.hungrybrothers.abletotrip.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.Attractions
import com.hungrybrothers.abletotrip.ui.network.AttractionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AttractionsRepository) : ViewModel() {
    private val _placeData = MutableLiveData<Attractions?>()
    val placeData: LiveData<Attractions?> = _placeData

    fun loadPlaceData(
        latitude: String,
        longitude: String,
    ) {
        Log.d("HomeViewModel", "latitude = $latitude,longitude = $longitude")
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.fetchPlaceData(latitude, longitude)
            _placeData.postValue(data)
        }
    }
}

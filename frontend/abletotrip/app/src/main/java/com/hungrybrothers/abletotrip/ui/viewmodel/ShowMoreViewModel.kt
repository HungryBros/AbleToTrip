package com.hungrybrothers.abletotrip.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attractions
import com.hungrybrothers.abletotrip.ui.network.ShowMoreInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowMoreViewModel(private val repository: ShowMoreInfoRepository) : ViewModel() {
    private val _showmoreData = MutableStateFlow<Catalog2Attractions?>(null)
    val showmoreData: StateFlow<Catalog2Attractions?> = _showmoreData

    fun ShowMoreData(
        latitude: String,
        longitude: String,
        category: String,
        page: Int,
    ) {
        Log.d("ShowMoreViewModel", "latitude = $latitude,longitude = $longitude,category = $category,page = $page")
        viewModelScope.launch {
            val result = repository.fetchShowMoreInfoData(latitude, longitude, category, page)
            _showmoreData.value = result
        }
    }
}
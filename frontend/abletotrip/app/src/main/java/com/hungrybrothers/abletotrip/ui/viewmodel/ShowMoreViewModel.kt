package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attractions
import com.hungrybrothers.abletotrip.ui.network.ShowMoreInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowMoreViewModel(private val repository: ShowMoreInfoRepository) : ViewModel() {
    private val _showmoreData = MutableStateFlow<Catalog2Attractions?>(null)
    val showmoreData: StateFlow<Catalog2Attractions?> = _showmoreData

    private var currentPage = 1
    private var lastPageReached = false
    private var isLoading = false

    fun loadInitialData(
        latitude: String,
        longitude: String,
        category: String,
    ) {
        currentPage = 1
        lastPageReached = false
        fetchData(latitude, longitude, category, currentPage, reset = true)
    }

    fun loadMoreData(
        latitude: String,
        longitude: String,
        category: String,
    ) {
        if (lastPageReached || isLoading) return
        fetchData(latitude, longitude, category, currentPage)
    }

    private fun fetchData(
        latitude: String,
        longitude: String,
        category: String,
        page: Int,
        reset: Boolean = false,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val result = repository.fetchShowMoreInfoData(latitude, longitude, category, page)
            isLoading = false
            if (result != null) {
                if (reset) {
                    _showmoreData.value = result
                } else {
                    val currentData = _showmoreData.value
                    if (currentData == null) {
                        _showmoreData.value = result
                    } else {
                        val updatedAttractions = currentData.attractions + result.attractions
                        _showmoreData.value = currentData.copy(attractions = updatedAttractions)
                    }
                }
                if (result.attractions.isEmpty()) {
                    lastPageReached = true
                } else {
                    currentPage++
                }
            }
        }
    }
}

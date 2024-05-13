package com.hungrybrothers.abletotrip.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attractions
import com.hungrybrothers.abletotrip.ui.network.Catalog2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class Catalog2ViewModel(private val repository: Catalog2Repository) : ViewModel() {
    private val _catalog2Data = MutableStateFlow<Catalog2Attractions?>(null)
    val catalog2Data: StateFlow<Catalog2Attractions?> = _catalog2Data

    private var currentPage = 1
    var isLastPage = false
    var isLoading = false

    fun fetchInitialData(
        latitude: String,
        longitude: String,
        category: String,
    ) {
        currentPage = 1
        isLastPage = false
        fetchData(latitude, longitude, category, currentPage, true)
    }

    var currentLatitude: String? = null
    var currentLongitude: String? = null
    var currentCategory: String? = null

    fun fetchMoreData() {
        if (isLastPage || isLoading) return

        if (currentLatitude == null || currentLongitude == null || currentCategory == null) {
            Log.e("Catalog2ViewModel", "fetchMoreData called with null latitude, longitude, or category")
            return
        }

        fetchData(currentLatitude!!, currentLongitude!!, currentCategory!!, ++currentPage)
    }

    private fun fetchData(
        latitude: String,
        longitude: String,
        category: String,
        page: Int,
        reset: Boolean = false,
    ) {
        if (category.isBlank()) {
            Log.d("Catalog2ViewModel", "카테고리가 비어 있습니다. 데이터 요청을 중단합니다.")
            _catalog2Data.value = null // null 또는 기본 상태로 설정
            return
        }
        viewModelScope.launch {
            isLoading = true
            val result = repository.fetchCatalog2Data(latitude, longitude, category, page)
            Log.d(
                "Catalog2ViewModel",
                "Fetching data for page: $page, lat: $latitude, long: $longitude, category: $category",
            )
            if (reset) {
                _catalog2Data.value = result
            } else {
                result?.attractions?.let { newAttractions ->
                    val currentAttractions = _catalog2Data.value?.attractions.orEmpty()
                    _catalog2Data.value = result.copy(attractions = currentAttractions + newAttractions)
                }
            }
            isLoading = false
            isLastPage = result?.attractions.isNullOrEmpty()
        }
    }
}

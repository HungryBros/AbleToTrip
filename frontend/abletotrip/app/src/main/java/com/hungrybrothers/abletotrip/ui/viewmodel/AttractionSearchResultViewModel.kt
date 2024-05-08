package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.AttractionSearchResult
import com.hungrybrothers.abletotrip.ui.network.AttractionSearchResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttractionSearchResultViewModel(private val repository: AttractionSearchResultRepository) : ViewModel() {
    private val _attractionSearchResultData = MutableStateFlow<AttractionSearchResult?>(null)
    val attractionSearchResultData: StateFlow<AttractionSearchResult?> = _attractionSearchResultData

    private var currentPage = 1
    private var lastPageReached = false

    fun fetchInitialData(
        latitude: String,
        longitude: String,
        keyword: String,
    ) {
        currentPage = 1
        lastPageReached = false
        fetchData(
            latitude,
            longitude,
            keyword,
            currentPage,
            reset = true,
        )
    }

    fun fetchMoreData(
        latitude: String,
        longitude: String,
        keyword: String,
    ) {
        if (lastPageReached) return
        fetchData(
            latitude,
            longitude,
            keyword,
            currentPage,
        )
    }

    private fun fetchData(
        latitude: String,
        longitude: String,
        keyword: String,
        page: Int,
        reset: Boolean = false,
    ) {
        viewModelScope.launch {
            val result =
                repository.fetchAttractionSearchResultData(
                    latitude,
                    longitude,
                    keyword,
                    page,
                )
            if (result != null) {
                if (reset) {
                    _attractionSearchResultData.value = result
                } else {
                    _attractionSearchResultData.value =
                        _attractionSearchResultData.value?.copy(
                            attractions = _attractionSearchResultData.value?.attractions.orEmpty() + (result.attractions ?: emptyList()),
                            counts = result.counts,
                        )
                }
                if (result.attractions.isNullOrEmpty()) {
                    lastPageReached = true
                } else {
                    currentPage++
                }
            }
        }
    }
}

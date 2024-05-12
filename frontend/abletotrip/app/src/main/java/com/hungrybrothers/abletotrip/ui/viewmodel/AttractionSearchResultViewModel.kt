package com.hungrybrothers.abletotrip.ui.viewmodel

import android.util.Log
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
    private var isLastPage = false
    var isLoading = false

    fun fetchInitialData(
        latitude: String,
        longitude: String,
        keyword: String,
    ) {
        currentPage = 1
        isLastPage = false
        fetchData(latitude, longitude, keyword, currentPage, true)
    }

    fun fetchMoreData(
        latitude: String,
        longitude: String,
        keyword: String,
    ) {
        if (isLastPage || isLoading) return
        fetchData(latitude, longitude, keyword, ++currentPage)
    }

    private fun fetchData(
        latitude: String,
        longitude: String,
        keyword: String,
        page: Int,
        reset: Boolean = false,
    ) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.fetchAttractionSearchResultData(latitude, longitude, keyword, page)
            Log.d("SearchResult", "page = $page")
            if (reset) {
                _attractionSearchResultData.value = result
            } else {
                result?.attractions?.let { newAttractions ->
                    val currentAttractions = _attractionSearchResultData.value?.attractions.orEmpty()
                    _attractionSearchResultData.value = result.copy(attractions = currentAttractions + newAttractions)
                }
            }
            isLoading = false
            isLastPage = result?.attractions.isNullOrEmpty()
        }
    }
}

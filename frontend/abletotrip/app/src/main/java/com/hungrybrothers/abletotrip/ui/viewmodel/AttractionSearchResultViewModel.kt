package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.AttractionSearchResult
import com.hungrybrothers.abletotrip.ui.datatype.SearchResult
import com.hungrybrothers.abletotrip.ui.network.AttractionSearchResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttractionSearchResultViewModel(private val repository: AttractionSearchResultRepository) : ViewModel() {
    private val _attractionSearchResultData = MutableStateFlow<AttractionSearchResult?>(null)
    val attractionSearchResultData: StateFlow<AttractionSearchResult?> = _attractionSearchResultData

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false

    fun fetchAttractionSearchResultData(
        keyword: String?,
        newSearch: Boolean = false,
    ) {
        if (newSearch) {
            currentPage = 1
            isLastPage = false
            _attractionSearchResultData.value = null
        }
        if (isLastPage || isLoading || keyword == null) return

        isLoading = true
        viewModelScope.launch {
            val result = repository.fetchAttractionSearchResultData(keyword, currentPage)
            if (result?.attractions.isNullOrEmpty()) {
                isLastPage = true
            } else {
                val oldAttractions = _attractionSearchResultData.value?.attractions.orEmpty()
                val newAttractions = oldAttractions + (result?.attractions as List<SearchResult>)
                _attractionSearchResultData.value = result.copy(attractions = newAttractions)
                currentPage++
            }
            isLoading = false
        }
    }
}

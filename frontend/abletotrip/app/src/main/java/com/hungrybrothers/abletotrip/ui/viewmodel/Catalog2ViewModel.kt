package com.hungrybrothers.abletotrip.ui.viewmodel

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

    fun fetchCatalog2Data(
        latitude: String,
        longitude: String,
        category: String,
        page: Int,
    ) {
        viewModelScope.launch {
            val result = repository.fetchCatalog2Data(latitude, longitude, category, page)
            _catalog2Data.value = result
        }
    }
}

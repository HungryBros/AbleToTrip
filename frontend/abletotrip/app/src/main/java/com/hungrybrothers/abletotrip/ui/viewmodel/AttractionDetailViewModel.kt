package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.datatype.AttractionDetail
import com.hungrybrothers.abletotrip.ui.network.AttractionDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttractionDetailViewModel(private val repository: AttractionDetailRepository) : ViewModel() {
    private val _attractionDetailData = MutableStateFlow<AttractionDetail?>(null)
    val attractionDetailData: StateFlow<AttractionDetail?> = _attractionDetailData

    fun fetchAttractionDetailData(itemId: Int?) {
        itemId?.let {
            viewModelScope.launch {
                val result = repository.fetchAttractionDetailData(it)
                _attractionDetailData.value = result
            }
        }
    }
}

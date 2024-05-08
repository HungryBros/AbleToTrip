package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrentLocationViewModel : ViewModel() {
    private val _latitude = MutableLiveData<String?>()
    val latitude: LiveData<String?>
        get() = _latitude

    private val _longitude = MutableLiveData<String?>()
    val longitude: LiveData<String?>
        get() = _longitude

    /**
     * `null` 값이 전달된 경우, 기존 값을 유지하고 `null`이 아닌 경우에만 업데이트합니다.
     */
    fun setLocation(
        latitude: String?,
        longitude: String?,
    ) {
        if (latitude != null) {
            _latitude.value = latitude
        }
        if (longitude != null) {
            _longitude.value = longitude
        }
    }
}

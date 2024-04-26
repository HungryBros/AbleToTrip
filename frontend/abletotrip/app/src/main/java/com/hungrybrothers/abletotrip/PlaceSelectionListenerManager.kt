package com.hungrybrothers.abletotrip

import android.util.Log
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class PlaceSelectionListenerManager(private val autocompleteFragment: AutocompleteSupportFragment) {
    private val TAG = "PlaceSelectionListenerManager"

    init {
        setupPlaceSelectionListener()
    }

    private fun setupPlaceSelectionListener() {
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        autocompleteFragment.setOnPlaceSelectedListener(
            object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    // Handle the selected place info here.
                    Log.i(TAG, "Place: ${place.name}, ${place.id}")
                }

                override fun onError(status: Status) {
                    // Handle the error here.
                    Log.i(TAG, "An error occurred: $status")
                }
            },
        )
    }
}

package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hungrybrothers.abletotrip.ui.viewmodel.AutoCompleteViewModel

@Composable
fun AutocompleteTextField(autocompleteViewModel: AutoCompleteViewModel) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            autocompleteViewModel.queryPlaces(newText)
        },
        label = { Text("장소 검색") },
    )
}

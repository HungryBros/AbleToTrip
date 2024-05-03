package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.PlacesList
import com.hungrybrothers.abletotrip.ui.components.SearchBar
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute

@Composable
fun SearchScreen(navController: NavController) {
    // 관광지 검색 키워드
    var text by remember { mutableStateOf("키워드") }
    // 키보드
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderBar(navController = navController, showBackButton = false)

        // 검색창
        SearchBar(
            text = text,
            onValueChange = { newText ->
                text = newText
            },
            placeholder = "관광지를 검색해보세요",
            onClear = {
                text = ""
                keyboardController?.hide()
            },
            onSearch = {
                if (text.isNotEmpty()) {
                    navController.navigate(NavRoute.SEARCH.routeName)
                }
            },
        )

        // 검색 결과 표시
        PlacesList(
            places = listOf(),
            onPlaceClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    SearchScreen(navController = rememberNavController())
}

package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.components.PlacesList
import com.hungrybrothers.abletotrip.ui.components.SearchBar

@Composable
fun SearchScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 검색창
        SearchBar(
            text = "",
            onValueChange = { /* 검색어 변경 시 수행할 동작 */ },
            placeholder = "검색어를 입력하세요",
            onClear = { /* 검색어 지우기 수행할 동작 */ },
            onSearch = {},
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

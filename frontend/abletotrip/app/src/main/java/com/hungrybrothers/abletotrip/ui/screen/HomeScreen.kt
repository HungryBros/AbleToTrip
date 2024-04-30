package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.CategorySecond
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess

@Composable
fun HomeScreen(navController: NavController) {
    var data by remember { mutableStateOf<Any?>(null) }
    // TODO: 로딩 애니메이션 넣을 때 사용할 코드(아직 사용 안 함)
    var isLoading by remember { mutableStateOf(true) }

    // TODO: 데이터 연결 시 주석 해제
//    LaunchedEffect(key1 = true) {
//        data = fetchData()
//        isLoading = false
//    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderBar(navController = navController, showBackButton = false)
// TODO: 데이터 연결하면 isLoading 테스트
//        if (isLoading) {
//            Text("로딩 중입니다.")
//        } else {
//          (여기에 아래 코드를 넣어주세요, 검색창, CategorySecond는 그대로 두고)
//        }

        Text(text = "검색 창 들어갈 곳")

        Box(
            modifier = Modifier.size(608.dp, 96.dp),
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
//                item {
//                    CategorySecond(
//                        icon = painterResource(id = R.drawable.ticket),
//                        label = "영화/공연",
//                    )
//                }
                val categories =
                    listOf(
                        Pair(R.drawable.ticket, "영화/공연"),
                        Pair(R.drawable.palette, "전시/기념관"),
                        Pair(R.drawable.leisure, "레저"),
                        Pair(R.drawable.stadium, "스포츠"),
                        Pair(R.drawable.park, "공원"),
                        Pair(R.drawable.framed_picture, "관광지"),
                        Pair(R.drawable.sunrise, "명승지"),
                    )
                // TODO: 이렇게 불러오면 PNG 파일을 가끔 불러오지 못하는 오류 발생
                categories.forEach { item ->
                    item {
                        CategorySecond(
                            icon = painterResource(id = item.first),
                            label = item.second,
                        )
                    }
                }
            }
        }
    }
}

suspend fun fetchData(): Any? {
    val response = KtorClient.client.get("attraction/")
    return if (response.status.isSuccess()) {
        val tempData = response.body<Any>()
        Log.d("income", "아 들어왔다$tempData")
        tempData
    } else {
        Log.e("pathdata", "아 안들어 오자나")
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    HomeScreen(navController = rememberNavController())
}

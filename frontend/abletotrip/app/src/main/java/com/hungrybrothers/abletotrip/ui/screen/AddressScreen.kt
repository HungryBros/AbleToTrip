package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import com.hungrybrothers.abletotrip.ui.network.User
import com.hungrybrothers.abletotrip.ui.network.UserResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//suspend fun fetchUserData(): List<User> {
//    return withContext(Dispatchers.IO) {
//        try {
//            Log.d("UserData", "요청보내는중...")
//            val response = KtorClient.client.get("member/users")
//            Log.d("UserData", "응답성공: ${response.status}")
//
//            if (response.status.isSuccess()) {
//                val responseData = response.body<UserResponse>().users
//                Log.d("UserData", "디코딩 성공: $responseData")
//                responseData
//            } else {
//                Log.e("UserData", "통신실패: ${response.status}")
//                emptyList()
//            }
//        } catch (e: Exception) {
//            Log.e("UserData", "에러가 나버렸다", e)
//            emptyList()
//        }
//    }
//}

@Composable
fun AddressScreen(navController: NavController) {
//    val (userData, setUserData) = remember { mutableStateOf(emptyList<User>()) }
//
//    LaunchedEffect(Unit) {
//        val fetchedData = fetchUserData()
//        setUserData(fetchedData) // 이렇게 상태를 업데이트합니다.
//    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "주소 정보를 여기에 입력하세요.")

        // 사용자 데이터를 화면에 표시
//        userData.forEach { user ->
//            Text(text = "이름: ${user.name}, 이메일: ${user.email}")
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddressScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    AddressScreen(navController = rememberNavController())
}

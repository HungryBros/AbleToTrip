package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import com.hungrybrothers.abletotrip.ui.network.KtorClient.client
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.theme.CustomWhite
import com.hungrybrothers.abletotrip.ui.theme.CustomWhiteSmoke
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// suspend fun fetchUserData(): List<User> {
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
// }
@Serializable
data class AddressData(val address: String)

suspend fun postAddress(address: String) {
    return withContext(Dispatchers.IO) {
        KtorClient.client.post("member/info/") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "address" to AddressData(address),
                ),
            )
        }
    }
}

@Composable
fun AddressScreen(navController: NavController) {
    var addressInput by remember { mutableStateOf(TextFieldValue()) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.Blue),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderBar(navController = navController, false)
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .background(Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "원활한 사용을 위해 집 주소를 등록해 주세요")
                Spacer(Modifier.height(16.dp))
                SearchBar()
            }
            Spacer(Modifier.weight(1f)) // 나머지 공간을 차지하도록 Spacer 설정
            CompleteButton(addressInput.text) {
                navController.navigate(NavRoute.HOME.routeName)
            }
        }
    }
}

@Composable
fun CompleteButton(
    addressInput: String,
    onCompleted: () -> Unit,
) {
    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = CustomPrimary, // 일반 상태의 배경 색상
            contentColor = Color.White, // 일반 상태의 텍스트 색상
            disabledContainerColor = CustomPrimary.copy(alpha = 0.3f), // 비활성화 상태의 배경 색상
            disabledContentColor = Color.White.copy(alpha = 0.6f), // 비활성화 상태의 텍스트 색상
        )
    Button(
        onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    postAddress(addressInput)
                    onCompleted() // 성공 처리
                } catch (e: Exception) {
                    // 오류 처리
                    Log.e("CompleteButton", "Failed to post address", e)
                }
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp)
                .height(48.dp),
        colors = buttonColors,
    ) {
        Text("완료", fontSize = 24.sp)
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var addressInput by remember { mutableStateOf(TextFieldValue()) }
    TextField(
        value = addressInput,
        onValueChange = { newValue -> addressInput = newValue },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        colors =
            TextFieldDefaults.colors(
                unfocusedContainerColor = CustomWhite,
                focusedContainerColor = CustomWhiteSmoke,
            ),
        placeholder = {
            Text(stringResource(id = R.string.find_address))
        },
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAddressScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    AddressScreen(navController = rememberNavController())
}

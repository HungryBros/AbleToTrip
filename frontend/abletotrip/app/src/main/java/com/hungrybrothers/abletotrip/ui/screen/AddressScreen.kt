package com.hungrybrothers.abletotrip.ui.screen

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.components.AutocompleteTextField
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.PlacesList
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.viewmodel.PlaceCompleteViewModel
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun postAddress(address: String): Boolean {
    return withContext(Dispatchers.IO) {
        Log.d("Places: button post", "POST 요청 시작: $address") // 요청 시작 로그
        try {
            KtorClient.client.post("member/info/") {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "address" to address,
                    ),
                )
            }
            Log.d("Places: button post", "POST 요청 완료")
            true
        } catch (e: Exception) {
            Log.e("Places: button post", "POST 요청 실패", e)
            false
        }
    }
}

@Composable
fun AddressScreen(
    navController: NavController,
    autocompleteViewModel: PlaceCompleteViewModel,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val places by autocompleteViewModel.places.observeAsState(initial = emptyList())
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var textFieldValue by remember { mutableStateOf("") }
    var showPlacesList by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderBar(navController = navController, false)
            Column(
                modifier =
                    Modifier
                        .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "원활한 사용을 위해 집 주소를 등록해 주세요", modifier = Modifier.padding(8.dp))
                Spacer(Modifier.height(16.dp))
                AutocompleteTextField(
                    text = textFieldValue,
                    onValueChange = { newText ->
                        textFieldValue = newText
                        autocompleteViewModel.queryPlaces(newText)
                        showPlacesList = true
                    },
                    placeholder = selectedAddress ?: "장소 검색",
                    onClear = {
                        textFieldValue = ""
                        selectedAddress = null
                        showPlacesList = false
                        keyboardController?.hide() // 키보드를 숨깁니다.
                    },
                )
                if (showPlacesList) {
                    PlacesList(places = places) { Place ->
                        selectedAddress = Place.address
                        textFieldValue = Place.name
                        showPlacesList = false
                        keyboardController?.hide() // 주소 선택시 키보드 숨기기
                    }
                }
                Log.d("Places : AddressScreen", "places = ${autocompleteViewModel.places.value}")
            }
            Spacer(Modifier.weight(1f))
            CompleteButton(navController, nameInput = textFieldValue, addressInput = selectedAddress)
        }
    }
}

@Composable
fun CompleteButton(
    navController: NavController,
    nameInput: String?,
    addressInput: String?,
) {
    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = if (addressInput != null) CustomPrimary else CustomPrimary.copy(alpha = 0.3f),
            contentColor = Color.White,
            disabledContainerColor = CustomPrimary.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.6f),
        )

    Button(
        onClick = {
            if (addressInput != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val isSuccess = postAddress("$nameInput, $addressInput")
                    if (isSuccess) {
                        navController.navigate(NavRoute.HOME.routeName)
                    }
                }
            }
        },
        enabled = addressInput != null, // 버튼 활성화 상태를 결정
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp)
                .height(48.dp),
        colors = buttonColors,
    ) {
        Text(
            text = if (addressInput != null) "완료" else "주소를 입력하세요",
            fontSize = 24.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddressScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    AddressScreen(
        navController = rememberNavController(),
        autocompleteViewModel =
            PlaceCompleteViewModel(
                application = @Suppress("ktlint:standard:max-line-length")
                Application(),
            ),
    )
}

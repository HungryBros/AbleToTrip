package com.hungrybrothers.abletotrip.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hungrybrothers.abletotrip.ui.components.AutocompleteTextField2
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.PlacesList
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomDisable
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.theme.CustomTertiary
import com.hungrybrothers.abletotrip.ui.viewmodel.PlaceCompleteViewModel

@Composable
fun DepartureScreen(
    navController: NavController,
    autocompleteViewModel: PlaceCompleteViewModel,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = CustomBackground) {
        Column(modifier = Modifier.fillMaxSize().background(CustomPrimary)) {
            HeaderBar(navController = navController, true)
            DepartureTopBox(autocompleteViewModel)
            PinGoogleMap()
        }
    }
}

@Composable
fun DepartureTopBox(autocompleteViewModel: PlaceCompleteViewModel) {
    val myendpoint: String = "경복궁" // 밑에 텍스트칸
    val keyboardController = LocalSoftwareKeyboardController.current // 키보드 컨트롤
    val places by autocompleteViewModel.places.observeAsState(initial = emptyList()) // 받아온 전체 플레이스
    var selectedAddress by remember { mutableStateOf<String?>(null) } // 선택한 주소의 상세주소
    var textFieldValue by remember { mutableStateOf("") } // 텍스트 필드 초기
    var showPlacesList by remember { mutableStateOf(true) } // 받아온데이터 보여줄지 말지
    val textState2 = remember { mutableStateOf(TextFieldValue()) } // 밑에 텍스트칸

    Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp)) {
        AutocompleteTextField2(
            text = textFieldValue,
            onValueChange = { newText ->
                textFieldValue = newText
                autocompleteViewModel.queryPlaces(newText)
                showPlacesList = true
            },
            placeholder = selectedAddress ?: "출발지를 입력해주세요.",
            onClear = {
                textFieldValue = ""
                selectedAddress = null
                showPlacesList = false
                keyboardController?.hide()
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding()
                    .clip(RoundedCornerShape(8.dp)),
        )
        if (showPlacesList) {
            PlacesList(places = places) { Place ->
                selectedAddress = Place.address
                textFieldValue = Place.name
                showPlacesList = false
                keyboardController?.hide()
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = textState2.value,
            onValueChange = { textState2.value = it },
            colors =
                TextFieldDefaults.colors(
                    unfocusedContainerColor = CustomBackground,
                    focusedContainerColor = CustomBackground,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedPlaceholderColor = Color.LightGray,
                ),
            placeholder = {
                Text("$myendpoint")
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding()
                    .clip(RoundedCornerShape(8.dp)),
        )
        ActionsRow()
    }
}

@Composable
fun ActionsRow() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = CustomTertiary),
            onClick = { /* Handle Left Button Click */ },
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("재입력", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CustomBackground))
        }
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = CustomDisable),
            onClick = { /* Handle Right Button Click */ },
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("길찾기", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CustomBackground))
        }
    }
}

@Composable
fun PinGoogleMap() {
    val myendpoint = LatLng(37.579617, 126.977041) // 예시 위경도, 실제 위경도로 변경 필요
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(myendpoint, 15f)
        }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(CustomBackground),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            Marker(
                state = rememberMarkerState(position = myendpoint),
                title = "도착지",
                snippet = "경복궁",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDepartureScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    DepartureScreen(
        navController = rememberNavController(),
        autocompleteViewModel =
            PlaceCompleteViewModel(
                application = @Suppress("ktlint:standard:max-line-length")
                (Application()),
            ),
    )
}

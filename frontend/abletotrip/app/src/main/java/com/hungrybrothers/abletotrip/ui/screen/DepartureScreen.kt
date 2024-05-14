package com.hungrybrothers.abletotrip.ui.screen

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.hungrybrothers.abletotrip.ui.theme.CustomWhite
import com.hungrybrothers.abletotrip.ui.theme.CustomWhiteSmoke
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.PlaceCompleteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DepartureScreen(
    navController: NavController,
    autocompleteViewModel: PlaceCompleteViewModel,
    arrivallatitude: Double,
    arrivallongitude: Double,
    arrivaladdress: String,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                HeaderBar(navController = navController, true, true)
            }
            Box(
                modifier = Modifier.background(CustomPrimary).padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                DepartureTopBox(navController, autocompleteViewModel, arrivaladdress, currentLocationViewModel)
            }
            PinGoogleMap(arrivallatitude, arrivallongitude, arrivaladdress)
        }
    }
}

@Composable
fun DepartureTopBox(
    navController: NavController,
    autocompleteViewModel: PlaceCompleteViewModel,
    arrivaladdress: String,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    val myendpoint: String = arrivaladdress // 밑에 텍스트칸
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current // 키보드 컨트롤
    val places by autocompleteViewModel.places.observeAsState(initial = emptyList()) // 받아온 전체 플레이스
    var selectedAddress by remember { mutableStateOf<String?>(null) } // 선택한 주소의 상세주소

    val departureAddress =
        produceState<String?>(initialValue = null) {
            val address = getCurrentLocationAddress(context, currentLocationViewModel)
            value = address
        }

    val textFieldValue = remember { mutableStateOf(departureAddress.value ?: "") } // 텍스트 필드 초기값 설정

    var showPlacesList by remember { mutableStateOf(true) } // 받아온데이터 보여줄지 말지
    val textState2 = remember { mutableStateOf(TextFieldValue()) } // 밑에 텍스트칸

    // departureAddress가 업데이트 될 때 textFieldValue도 업데이트
    LaunchedEffect(departureAddress.value) {
        textFieldValue.value = departureAddress.value ?: ""
    }

    Log.d("departureAddress", "departureAddress: ${departureAddress.value}")

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AutocompleteTextField2(
                text = textFieldValue.value,
                onValueChange = { newText ->
                    textFieldValue.value = newText
                    autocompleteViewModel.queryPlaces(newText)
                    showPlacesList = true
                },
                placeholder = "출발지를 입력해주세요.",
                onClear = {
                    textFieldValue.value = ""
                    selectedAddress = null
                    showPlacesList = false
                    keyboardController?.hide()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
            )
            PlacesList(places = places) { place, isValid ->
                if (isValid) {
                    selectedAddress = place.address
                    textFieldValue.value = place.name
                    showPlacesList = false
                    keyboardController?.hide()
                } else {
                    textFieldValue.value = ""
                    selectedAddress = null
                    showPlacesList = true
                }
            }
            TextField(
                singleLine = true,
                value = textState2.value,
                onValueChange = { textState2.value = it },
                enabled = false,
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = CustomWhite,
                        focusedContainerColor = CustomWhiteSmoke,
                        unfocusedPlaceholderColor = CustomWhiteSmoke,
                        focusedPlaceholderColor = CustomWhite,
                    ),
                placeholder = {
                    Text("$myendpoint")
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
            )
        }
        ActionsRow(navController, textFieldValue, selectedAddress, myendpoint)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getCurrentLocationAddress(
    context: Context,
    currentLocationViewModel: CurrentLocationViewModel,
): String? {
    val latitude = currentLocationViewModel.latitude.value?.toDoubleOrNull()
    val longitude = currentLocationViewModel.longitude.value?.toDoubleOrNull()

    if (latitude != null && longitude != null) {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    if (addresses.isNotEmpty()) {
                                        continuation.resume(addresses[0].getAddressLine(0)) {}
                                    } else {
                                        continuation.resume(null) {}
                                    }
                                }

                                override fun onError(errorMessage: String?) {
                                    Log.e("GeocodeListener", "Error: $errorMessage")
                                    continuation.resume(null) {}
                                }
                            },
                        )
                    }
                } else {
                    // 이전 버전에서는 동기식 API 사용
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.getOrNull(0)?.getAddressLine(0)
                }
            } catch (e: Exception) {
                Log.e("getCurrentLocationAddress", "Error converting coordinates to address", e)
                null
            }
        }
    }
    return null
}

@Composable
fun ActionsRow(
    navController: NavController,
    textFieldValue: MutableState<String>,
    selectedAddress: String?,
    arrival: String,
) {
    val isRouteButtonEnabled = textFieldValue.value.isNotEmpty()
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = CustomTertiary),
            onClick = { textFieldValue.value = "" },
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("재입력", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CustomBackground))
        }
        Button(
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isRouteButtonEnabled) CustomTertiary else CustomDisable,
                ),
            onClick = {
                if (selectedAddress == null) {
                    navController.navigate("TOTAL_ROUTE/${textFieldValue.value}/$arrival")
                } else {
                    navController.navigate("TOTAL_ROUTE/$selectedAddress ${textFieldValue.value}/$arrival")
                }
            },
            enabled = isRouteButtonEnabled,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("길찾기", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CustomBackground))
        }
    }
}

@Composable
fun PinGoogleMap(
    arrivallatitude: Double,
    arrivallongitude: Double,
    arrivaladdress: String,
) {
    val myendpoint = LatLng(arrivallatitude, arrivallongitude) // 예시 위경도, 실제 위경도로 변경 필요
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
                snippet = arrivaladdress,
            )
        }
    }
}

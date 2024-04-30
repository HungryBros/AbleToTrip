package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomDisable
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.theme.CustomTertiary

@Composable
fun DepartureScreen(navController: NavController) {
    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HeaderBar(navController = navController, true)
            DepartureTopBox(modifier = Modifier.weight(1f))
            PinGoogleMap(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun DepartureTopBox(modifier: Modifier) {
    var mystartpoint: String = "출발지를 입력해주세요."
    val myendpoint: String = "경복궁"

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = CustomPrimary),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            val textState1 = remember { mutableStateOf(TextFieldValue()) }
            val textState2 = remember { mutableStateOf(TextFieldValue()) }

            TextField(
                value = textState1.value,
                onValueChange = { textState1.value = it },
                trailingIcon = {
                    Row {
                        Icon(
                            modifier =
                                Modifier
                                    .padding(end = 8.dp)
                                    .size(25.dp)
                                    .clickable {},
                            painter = painterResource(id = R.drawable.target),
                            contentDescription = "target",
                        )
                        Icon(
                            modifier =
                                Modifier
                                    .padding(end = 8.dp)
                                    .size(25.dp)
                                    .clickable {},
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "home",
                        )
                    }
                },
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = CustomBackground,
                        focusedContainerColor = CustomBackground,
                        unfocusedPlaceholderColor = Color.Gray,
                        focusedPlaceholderColor = Color.LightGray,
                    ),
                placeholder = {
                    Text("$mystartpoint")
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 32.dp, end = 32.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )

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
                        .padding(top = 4.dp, start = 32.dp, end = 32.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = CustomTertiary,
                        ),
                    onClick = { /* Handle Left Button Click */ },
                    shape = RoundedCornerShape(8.dp),
                    content = {
                        Text(
                            "재입력",
                            style =
                                TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(500),
                                    color = CustomBackground,
                                ),
                        )
                    },
                )
                Button(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = CustomDisable,
                        ),
                    onClick = { /* Handle Right Button Click */ },
                    shape = RoundedCornerShape(8.dp),
                    content = {
                        Text(
                            "길찾기",
                            style =
                                TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(500),
                                    color = CustomBackground,
                                ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun PinGoogleMap(modifier: Modifier) {
    val myendpoint = LatLng(37.579617, 126.977041) // 예시 위경도, 실제 위경도로 변경 필요
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(myendpoint, 15f)
        }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        Marker(
            state = rememberMarkerState(position = myendpoint),
            title = "도착지",
            snippet = "Marker in Big Ben", // 마커의 설명 역시 적절히 변경 필요
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDepartureScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    DepartureScreen(navController = rememberNavController())
}

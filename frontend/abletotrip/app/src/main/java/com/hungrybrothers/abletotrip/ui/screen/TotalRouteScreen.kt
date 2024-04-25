package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.theme.CustomTertiary

@Composable
fun TotalRouteScreen(navController: NavController) {
    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TotalRouteGoogleMap(modifier = Modifier.weight(7f))
            TotalRouteBottomBox(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TotalRouteBottomBox(modifier: Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(CustomTertiary)
                    .clickable(onClick = { /* TODO: Define what happens when the box is clicked */ }),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = "50분",
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = CustomBackground,
                        ),
                )
            },
        )
        Box(
            modifier =
                Modifier
                    .weight(2f)
                    .fillMaxSize()
                    .background(CustomPrimary)
                    .clickable(onClick = { /* TODO: Define what happens when the box is clicked */ }),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = "따라가기",
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = CustomBackground,
                        ),
                )
            },
        )
    }
}

val jsonData =
    """
    [
        [
            37.50129,
            127.0396
        ],
        [
            37.50112,
            127.03723
        ],
        [
            37.50064,
            127.03638
        ],
        [
            37.50064,
            127.03637
        ],
        [
            37.49909,
            127.03132
        ],
        [
            37.49499,
            127.01794
        ],
        [
            37.4939,
            127.0144
        ],
        [
            37.4938,
            127.01366
        ],
        [
            37.49398,
            127.01362
        ],
        [
            37.49561,
            127.01326
        ],
        [
            37.49744,
            127.01284
        ],
        [
            37.49788,
            127.01275
        ],
        [
            37.49809,
            127.01267
        ],
        [
            37.49827,
            127.01257
        ],
        [
            37.49851,
            127.01239
        ],
        [
            37.49889,
            127.01203
        ],
        [
            37.49916,
            127.0117
        ],
        [
            37.4994,
            127.01131
        ],
        [
            37.49961,
            127.01085
        ],
        [
            37.49999,
            127.00979
        ],
        [
            37.50058,
            127.00815
        ],
        [
            37.50082,
            127.00765
        ],
        [
            37.50111,
            127.00721
        ],
        [
            37.50153,
            127.00674
        ],
        [
            37.50201,
            127.00637
        ],
        [
            37.50237,
            127.00617
        ],
        [
            37.50264,
            127.00606
        ],
        [
            37.50317,
            127.00582
        ],
        [
            37.50385,
            127.0055
        ],
        [
            37.50602,
            127.00441
        ],
        [
            37.50677,
            127.00405
        ],
        [
            37.50712,
            127.00395
        ],
        [
            37.50748,
            127.00391
        ],
        [
            37.50784,
            127.00393
        ],
        [
            37.5082,
            127.004
        ],
        [
            37.50854,
            127.00412
        ],
        [
            37.50887,
            127.00429
        ],
        [
            37.50918,
            127.00451
        ],
        [
            37.50949,
            127.0048
        ],
        [
            37.50974,
            127.00509
        ],
        [
            37.50985,
            127.00525
        ],
        [
            37.50996,
            127.00543
        ],
        [
            37.51046,
            127.00644
        ],
        [
            37.51125,
            127.00806
        ],
        [
            37.51194,
            127.00945
        ],
        [
            37.51234,
            127.01031
        ],
        [
            37.51359,
            127.01295
        ],
        [
            37.51418,
            127.01424
        ],
        [
            37.51467,
            127.01541
        ],
        [
            37.51498,
            127.01626
        ],
        [
            37.51527,
            127.01711
        ],
        [
            37.51594,
            127.01903
        ],
        [
            37.51628,
            127.01987
        ],
        [
            37.51698,
            127.02164
        ],
        [
            37.51766,
            127.02326
        ],
        [
            37.51849,
            127.02521
        ],
        [
            37.5191,
            127.02667
        ],
        [
            37.51959,
            127.02772
        ],
        [
            37.51972,
            127.02795
        ],
        [
            37.51988,
            127.02807
        ],
        [
            37.52001,
            127.0281
        ],
        [
            37.52016,
            127.02813
        ],
        [
            37.52088,
            127.02818
        ],
        [
            37.52254,
            127.02828
        ],
        [
            37.52546,
            127.02845
        ],
        [
            37.52633,
            127.02852
        ],
        [
            37.52651,
            127.0285
        ],
        [
            37.5267,
            127.0285
        ],
        [
            37.52704,
            127.02843
        ],
        [
            37.52735,
            127.02828
        ],
        [
            37.52755,
            127.02815
        ],
        [
            37.52778,
            127.02796
        ],
        [
            37.52814,
            127.02758
        ],
        [
            37.52928,
            127.02622
        ],
        [
            37.52974,
            127.02571
        ],
        [
            37.53009,
            127.02539
        ],
        [
            37.5306,
            127.02499
        ],
        [
            37.53171,
            127.02422
        ],
        [
            37.53487,
            127.02204
        ],
        [
            37.5404,
            127.01815
        ],
        [
            37.54117,
            127.01762
        ],
        [
            37.54161,
            127.01738
        ],
        [
            37.54188,
            127.01723
        ],
        [
            37.54236,
            127.01703
        ],
        [
            37.54297,
            127.01687
        ],
        [
            37.54377,
            127.01677
        ],
        [
            37.54436,
            127.01673
        ],
        [
            37.54494,
            127.0167
        ],
        [
            37.54624,
            127.0166
        ],
        [
            37.54648,
            127.01657
        ],
        [
            37.54668,
            127.01652
        ],
        [
            37.54686,
            127.01645
        ],
        [
            37.548,
            127.01594
        ],
        [
            37.5486,
            127.01563
        ],
        [
            37.54939,
            127.0151
        ],
        [
            37.55033,
            127.01431
        ],
        [
            37.5522,
            127.01278
        ],
        [
            37.553,
            127.01211
        ],
        [
            37.55339,
            127.01177
        ],
        [
            37.55434,
            127.011
        ],
        [
            37.55518,
            127.01032
        ],
        [
            37.55544,
            127.01013
        ],
        [
            37.5562,
            127.00955
        ],
        [
            37.55664,
            127.00926
        ],
        [
            37.55716,
            127.00899
        ],
        [
            37.55751,
            127.00877
        ],
        [
            37.55767,
            127.00863
        ],
        [
            37.5579,
            127.00838
        ],
        [
            37.55805,
            127.0082
        ],
        [
            37.55841,
            127.00762
        ],
        [
            37.55874,
            127.00705
        ],
        [
            37.55882,
            127.00689
        ],
        [
            37.55891,
            127.0066
        ],
        [
            37.55901,
            127.00622
        ],
        [
            37.55907,
            127.0058
        ],
        [
            37.55907,
            127.00564
        ],
        [
            37.55902,
            127.00436
        ],
        [
            37.559,
            127.00371
        ],
        [
            37.55903,
            127.00308
        ],
        [
            37.55987,
            126.99795
        ],
        [
            37.56006,
            126.99698
        ],
        [
            37.5603,
            126.99613
        ],
        [
            37.5605,
            126.99568
        ],
        [
            37.56069,
            126.9953
        ],
        [
            37.561,
            126.99473
        ],
        [
            37.56137,
            126.99422
        ],
        [
            37.56171,
            126.99388
        ],
        [
            37.56209,
            126.99356
        ],
        [
            37.56233,
            126.9934
        ],
        [
            37.56264,
            126.99325
        ],
        [
            37.56298,
            126.99311
        ],
        [
            37.56429,
            126.99291
        ],
        [
            37.5654,
            126.99274
        ],
        [
            37.56572,
            126.99271
        ],
        [
            37.56683,
            126.99256
        ],
        [
            37.56826,
            126.99238
        ],
        [
            37.57044,
            126.99214
        ],
        [
            37.57083,
            126.99208
        ],
        [
            37.57099,
            126.99205
        ],
        [
            37.57132,
            126.99195
        ],
        [
            37.57198,
            126.99173
        ],
        [
            37.5727,
            126.9915
        ],
        [
            37.57361,
            126.99121
        ],
        [
            37.57554,
            126.99063
        ],
        [
            37.57604,
            126.99049
        ],
        [
            37.57634,
            126.99035
        ],
        [
            37.57662,
            126.99018
        ],
        [
            37.57683,
            126.99001
        ],
        [
            37.57703,
            126.98974
        ],
        [
            37.57714,
            126.98955
        ],
        [
            37.57734,
            126.9891
        ],
        [
            37.57741,
            126.98889
        ],
        [
            37.57746,
            126.98859
        ],
        [
            37.57749,
            126.98831
        ],
        [
            37.57749,
            126.98798
        ],
        [
            37.57747,
            126.98773
        ],
        [
            37.57739,
            126.98739
        ],
        [
            37.57725,
            126.98704
        ],
        [
            37.57654,
            126.98551
        ],
        [
            37.57613,
            126.98465
        ],
        [
            37.57578,
            126.98382
        ],
        [
            37.57561,
            126.98331
        ],
        [
            37.57556,
            126.98303
        ],
        [
            37.57557,
            126.98275
        ],
        [
            37.5758,
            126.9813
        ],
        [
            37.57589,
            126.9805
        ],
        [
            37.5759,
            126.98007
        ],
        [
            37.57585,
            126.97959
        ],
        [
            37.57564,
            126.97819
        ],
        [
            37.57554,
            126.97733
        ],
        [
            37.57552,
            126.97699
        ],
        [
            37.57554,
            126.97652
        ],
        [
            37.57563,
            126.97556
        ],
        [
            37.57579,
            126.97414
        ],
        [
            37.57588,
            126.97357
        ],
        [
            37.57602,
            126.97326
        ],
        [
            37.57962,
            126.97704
        ]
    ]
    """.trimIndent()

fun parseCoordinates(jsonData: String): List<LatLng> {
    val gson = Gson() // Gson 인스턴스를 사용하여 JSON을 파싱합니다.
    val type = object : TypeToken<List<List<Double>>>() {}.type
    val rawCoordinates: List<List<Double>> = gson.fromJson(jsonData, type)

    // 좌표 목록을 LatLng 객체 목록으로 변환합니다.
    return rawCoordinates.map { LatLng(it[0], it[1]) }
}

@Composable
fun TotalRouteGoogleMap(modifier: Modifier) {
//    val incodedpolyline = "an{cFolkfW`@xM~AhD?@tHp^rXrrAxEbURrCc@FeIfAmJrAwAPi@Nc@Ro@b@kAfAu@`Ao@lAi@zAkArEuBfIo@bBy@vAsA|A_BhAgAf@u@TiBn@gC~@qLxEuCfAeARgAFgACgAMcAWaAa@}@k@}@y@q@y@U_@Uc@cBiE}CcIiCuGoAkDyFoOuBaGaBiF}@iDy@iDeC_KcAgDkCaJgCcIeDeKyBcHaBqEYm@_@WYE]EoCIkISgQa@mDMc@Be@?cAL}@\\\\g@Xm@d@gAjAcFnG{AdBeA~@eBnA}ExCwRrLqa@hWyChBwAn@u@\\\\_Bf@yB^_DRuBFsBDcGRo@Dg@Hc@LcFdBwB|@}ChB{D|CuJpH_DdCmAbA}DxCgDfCs@d@wCrBwAx@gBt@eAj@_@Zm@p@]b@gArBaApBO^Qx@SjAKrA?^H~FB`CE|BgD`_@e@`Eo@hDg@xAe@jA}@pBiAdBcAbAkA~@o@^}@\\\\cAZeGf@}E`@_AD}E\\\\}Gb@sLn@mAJ_@DaARcCj@oCl@uDx@aKrBcBZ{@Zw@`@i@`@g@t@Ud@g@xAMh@Iz@Ev@?`ABp@NbAZdAlCpHpAjDdAdD`@dBHv@Av@m@`HQ~CAtAH~Ah@vGRjDBbAC|AQ~D_@zGQpB[|@oUsV"
    val decodedpolyline = parseCoordinates(jsonData)
    val mystartpoint = LatLng(37.501286, 127.0396029)
    val myendpoint = LatLng(37.579617, 126.977041)
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(myendpoint, 12f)
        }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        Polyline(
            points = decodedpolyline,
            color = Color.Blue,
            width = 10f,
        )

        Marker(
            state = rememberMarkerState(position = mystartpoint),
            title = "출발지",
            snippet = "Here is the start point",
        )
        Marker(
            state = rememberMarkerState(position = myendpoint),
            title = "도착지",
            snippet = "Here is the end point",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTotalRouteScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    TotalRouteScreen(navController = rememberNavController())
}

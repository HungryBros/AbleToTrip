@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.hungrybrothers.abletotrip.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NavigationViewModel : ViewModel() {
    val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        // JSON 설정: 예를 들어, 직렬화 기능을 커스터마이즈할 수 있습니다.
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }
    private val _navigationData = MutableLiveData<Resource<NavigationData>>()
    val navigationData: LiveData<Resource<NavigationData>> = _navigationData

    val polylineDataList = MutableLiveData<List<PolylineData>>()

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> = _duration

    private val _detailRouteInfo = MutableLiveData<List<DetailRouteInfo>>()
    val detailRouteInfo: LiveData<List<DetailRouteInfo>> = _detailRouteInfo

    fun fetchNavigationData(
        departure: String?,
        arrival: String?,
    ) {
        _navigationData.value = Resource.loading(null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val requestBody =
                    buildJsonObject {
                        put("departure", departure ?: "")
                        put("arrival", arrival ?: "")
                    }
                val response: HttpResponse =
                    client.post("http://k10a607.p.ssafy.io:8087/navigation/search-direction/") {
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }
                if (response.status == HttpStatusCode.OK) {
                    val responseBody = response.bodyAsText()
                    val data = Json { ignoreUnknownKeys = true }.decodeFromString<NavigationData>(responseBody)
                    _navigationData.postValue(Resource.success(data))

                    _duration.postValue(data.duration) // duration 업데이트
                    _detailRouteInfo.postValue(data.detail_route_info) // detail_route_info 업데이트

                    coroutineScope {
                        val walkoneData = mutableListOf<LatLng>()
                        val walktwoData = mutableListOf<LatLng>()
                        var flag = false
                        val polylineData =
                            data.polyline_info.flatMap { polylineInfo ->
                                polylineInfo.info.map { info ->
                                    async {
                                        // 노선 타입에 따라 색상을 결정합니다.
                                        val color =
                                            when (polylineInfo.type) {
                                                // 지하철 노선에 따른 색상 적용
                                                "subway" -> {
                                                    val line = info.line ?: ""
                                                    subwayColor[line] ?: Color.Red
                                                }
                                                // 걷기 경로에 파란색 적용
                                                "walk" -> Color.Blue
                                                else -> Color.Green // 기타 경로는 초록색 적용
                                            }

                                        val points =
                                            if (info.polyline != null) {
                                                flag = true
                                                val decodedPoints = fetchPolylineData(info.polyline).data
                                                decodedPoints.map { LatLng(it[0], it[1]) }
                                            } else if (info.latitude != null && info.longitude != null) {
                                                if (!flag) {
                                                    walkoneData.add(LatLng(info.latitude, info.longitude))
                                                } else {
                                                    walktwoData.add(LatLng(info.latitude, info.longitude))
                                                }
                                                listOf(LatLng(info.latitude, info.longitude))
                                            } else {
                                                emptyList()
                                            }
                                        PolylineData(points, color)
                                    }
                                }
                            }.awaitAll()
                        val combinedPolylineData =
                            mutableListOf<PolylineData>().apply {
                                add(PolylineData(walkoneData, Color.Blue))
                                addAll(polylineData)
                                add(PolylineData(walktwoData, Color.Blue))
                            }
                        polylineDataList.postValue(combinedPolylineData)
                    }
                } else {
                    _navigationData.postValue(Resource.error("Failed to load data", null))
                }
            } catch (e: Exception) {
                _navigationData.postValue(Resource.error("Error occurred: ${e.message}", null))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Resource<T> = Resource(Status.SUCCESS, data, null)

        fun <T> error(
            msg: String,
            data: T?,
        ): Resource<T> = Resource(Status.ERROR, data, msg)

        fun <T> loading(data: T?): Resource<T> = Resource(Status.LOADING, data, null)
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING,
    }
}

@Serializable
data class NavigationData(
    val duration: Int,
    val is_bus_exist: Boolean,
    val is_subway_exist: Boolean,
    val is_pedestrian_route: Boolean,
//    val is_pedestrian_route: Boolean,
    val polyline_info: List<PolylineInfo>,
    val detail_route_info: List<DetailRouteInfo>,
)

@Serializable
data class PolylineInfo(
    val type: String,
    val info: List<Info>,
)

@Serializable
data class Info(
    val longitude: Double? = null,
    val latitude: Double? = null,
    val line: String? = null,
    val polyline: String? = null,
)

@Serializable
data class DetailRouteInfo(
    val type: String,
    val info: List<String>,
)

@Serializable
data class PolylineResponse(
    val success: Int,
    val data: List<List<Double>>,
)

data class PolylineData(
    val points: List<LatLng>,
    val color: Color,
)

suspend fun fetchPolylineData(incodedpolyline: String?): PolylineResponse {
    val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        // JSON 설정: 예를 들어, 직렬화 기능을 커스터마이즈할 수 있습니다.
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }

    println("Received data: $incodedpolyline")
    val requestBody =
        buildJsonObject {
            put("input", incodedpolyline)
            put("type", "decode")
        }
    println("Received data: aaaa  $requestBody")
    val response: HttpResponse =
        client.post("https://apihut.in/api/polyline") {
            setBody(requestBody)
            headers {
                append("X-Avatar-Key", "bd92b6bd-fbcc-4dfd-a68d-d225d2c7f8c3")
                append(HttpHeaders.ContentType, "application/json")
                append(HttpHeaders.Accept, "application/json")
            }
        }
    println("Received data: $response")
    val responseBody = response.bodyAsText()
    val data = Json { ignoreUnknownKeys = true }.decodeFromString<PolylineResponse>(responseBody)
    return data
}

val subwayColor =
    mutableMapOf(
        "1호선" to Color(0xFF002060),
        "2호선" to Color(0xFF00AF4F),
        "3호선" to Color(0xFFFF9900),
        "4호선" to Color(0xFF0099FF),
        "5호선" to Color(0xFF9160AC),
        "6호선" to Color(0xFFA06134),
        "7호선" to Color(0xFF77C000),
        "8호선" to Color(0xFFEC008C),
        "9호선" to Color(0xFFB1A152),
    )

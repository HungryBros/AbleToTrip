package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"
    var authToken: String? = null

    val client =
        HttpClient(CIO) {
            // JSON 처리를 위한 설정
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
            // 요청과 응답에 대한 로깅
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            defaultRequest {
                // 모든 요청에 공통적으로 적용되는 설정
                if (authToken != null) {
                    header(HttpHeaders.Authorization, "Bearer $authToken")
                }
                Log.d("Kakao헤더확인", "Bearer $authToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                url.takeFrom(BASE_URL + url.encodedPath)
            }
        }
}

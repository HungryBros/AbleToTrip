package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.User
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class UserInfoRepository {
    suspend fun fetchUserInfoData(): User? {
        val url = "member/info/"
        return try {
            val response = KtorClient.client.get(url)
            when {
                response.status == HttpStatusCode.OK -> {
                    response.body<User>()
                }
                response.status == HttpStatusCode.NoContent -> {
                    null // 주소가 없으므로 null 반환
                }
                response.status == HttpStatusCode.NotFound -> {
                    null // 사용자 정보가 없으므로 null 반환
                }
                else -> {
                    null // 그 외 실패 응답 처리
                }
            }
        } catch (e: Exception) {
            null // 예외 발생 시 null 반환
        }
    }
}

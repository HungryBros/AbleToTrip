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
                    Log.d("UserData", "Response success: ${response.status}")
                    response.body<User>()
                }
                response.status == HttpStatusCode.NoContent -> {
                    Log.d("UserData", "User has no address: ${response.status}")
                    null // 주소가 없으므로 null 반환
                }
                response.status == HttpStatusCode.NotFound -> {
                    Log.e("UserData", "User not found: ${response.status}")
                    null // 사용자 정보가 없으므로 null 반환
                }
                else -> {
                    Log.e("UserData", "Communication failure: ${response.status}")
                    null // 그 외 실패 응답 처리
                }
            }
        } catch (e: Exception) {
            Log.e("UserData", "Error occurred", e)
            null // 예외 발생 시 null 반환
        }
    }
}

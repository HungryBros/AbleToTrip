package com.hungrybrothers.abletotrip

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.launch

class KakaoAuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "KakaoAuthViewModel"
    }

    private val context = application.applicationContext
    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean> = _loggedIn

    // 로그인 상태를 변경하는 메서드
    fun setLoggedIn(loggedIn: Boolean) {
        _loggedIn.value = loggedIn
    }

    // 카카오 사용자 정보 요청 함수
    fun fetchKakaoUserInfo(token: OAuthToken) {
        UserApiClient.instance.me { user: User?, error: Throwable? ->
            if (error != null) {
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(TAG, "카카오 사용자 정보 요청 성공")
                val email = user.kakaoAccount?.email ?: ""
                sendTokenAndEmailToServer(token.accessToken, email)
            }
        }
    }

    fun handleKakaoLogin() {
        // 로그인 조합 예제

// 카카오계정으로 로그인 공통 callback 구성
// 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                KtorClient.authToken = "${token.accessToken}"
                setLoggedIn(true)
                fetchKakaoUserInfo(token = token)
            }
        }

// 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    private fun sendTokenAndEmailToServer(
        accessToken: String,
        email: String,
    ) {
        viewModelScope.launch {
            try {
                val response =
                    KtorClient.client.post("가짜/") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "accessToken" to accessToken,
                                "email" to email,
                            ),
                        )
                    }
                if (response.status == HttpStatusCode.OK) {
                    Log.d(TAG, "서버로 토큰과 이메일 전송 성공")
                } else {
                    Log.e(TAG, "서버로 토큰과 이메일 전송 실패: 상태 코드 ${response.status}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "서버로 토큰과 이메일 전송 중 오류 발생: ${e.message}")
            }
        }
    }
}

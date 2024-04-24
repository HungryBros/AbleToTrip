package com.hungrybrothers.abletotrip

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class KakaoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "14d175f0a7d8e81e92488ae6e5d9bdc8")
    }
}

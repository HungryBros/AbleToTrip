package com.hungrybrothers.abletotrip

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class KakaoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "3e467ddd4cc251a02dcd9b746240a5d6")
    }
}

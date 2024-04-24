package com.hungrybrothers.abletotrip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.navigation.Navigation
import com.hungrybrothers.abletotrip.ui.theme.AbletotripTheme
import com.kakao.sdk.common.util.Utility

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbletotripTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    Navigation(navController)
//                    Navigation()

                    val keyHash = Utility.getKeyHash(this)
                    // 로그캣에서 확인 가능
                    Log.d("KeyHash", "KeyHash: $keyHash")
                }
            }
        }
    }
}

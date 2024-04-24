package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute

@Composable
fun HeaderBar(
    navController: NavController,
    showBackButton: Boolean = false, // 뒤로 가기 버튼을 표시할지 여부
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // 뒤로 가기 버튼, 필요한 경우에만 표시
            if (showBackButton) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier =
                        Modifier
                            .padding(start = 16.dp)
                            .size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.navigate_before),
                        contentDescription = "Back",
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp + 16.dp))
            }

            // 로고 이미지
            IconButton(
                onClick = { navController.navigate(NavRoute.HOME.routeName) },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.abletotrip_logo),
                    contentDescription = "Logo",
                )
            }

            Spacer(modifier = Modifier.size(16.dp))
        }
    }

    // 아래에 1px 높이의 선
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray), // CustomWhiteSmoke 색상 사용하는 게 에러가 나서 일단 사용
    )
}

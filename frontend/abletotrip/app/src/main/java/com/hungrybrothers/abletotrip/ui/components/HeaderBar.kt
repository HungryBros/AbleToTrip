package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
    showBackButton: Boolean = false,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
        color = Color.White,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.navigate_before),
                        contentDescription = "Back",
                    )
                }
            }

            IconButton(
                onClick = { navController.navigate(NavRoute.HOME.routeName) },
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.abletotrip_logo),
                    tint = Color.Unspecified,
                    contentDescription = "Logo",
                )
            }
        }
    }

    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray),
    )
}

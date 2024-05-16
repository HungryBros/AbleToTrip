package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.*
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.data.preferences.PreferencesManager
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(top = 72.dp, bottom = 32.dp),
            activeColor = CustomPrimary,
            inactiveColor = Color.Gray,
        )
        HorizontalPager(
            count = 4, // 페이지 수를 여기서 지정
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPage(page, navController)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingPage(
    page: Int,
    navController: NavController,
) {
    val scope = rememberCoroutineScope()

    when (page) {
        0 ->
            OnboardingContent(
                imageResId = R.drawable.onboard1,
                text =
                    buildAnnotatedString {
                        append("내 주변 ")
                        withStyle(style = SpanStyle(color = CustomPrimary, fontWeight = FontWeight.Bold)) {
                            append("무장애 관광지")
                        }
                        append("를 찾아보세요")
                        append("\n")
                    },
            )
        1 ->
            OnboardingContent(
                imageResId = R.drawable.onboard2,
                text =
                    buildAnnotatedString {
                        append("도보 - 지하철 길안내는 물론\n")
                        withStyle(style = SpanStyle(color = CustomPrimary, fontWeight = FontWeight.Bold)) {
                            append("엘리베이터 설치 경로를 우선으로")
                        }
                    },
            )
        2 ->
            OnboardingContent(
                imageResId = R.drawable.onboard3,
                text =
                    buildAnnotatedString {
                        append("지하철 장애인 화장실 안내와\n경로 상세 안내까지")
                    },
            )
        3 ->
            OnboardingContent(
                imageResId = R.drawable.onboard4,
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = CustomPrimary, fontWeight = FontWeight.Bold)) {
                            append("여행가기쉬운지도")
                        }
                        append("와 함께\n집까지 길 안내")
                    },
                isLastPage = true,
                onButtonClick = {
                    scope.launch {
                        PreferencesManager.setOnboardingComplete(navController.context, true)
                        navController.navigate(NavRoute.LOGIN.routeName) {
                            popUpTo(NavRoute.HOME.routeName) { inclusive = true }
                        }
                    }
                },
            )
    }
}

@Composable
fun OnboardingContent(
    imageResId: Int,
    text: AnnotatedString,
    isLastPage: Boolean = false,
    onButtonClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
            // 버튼 공간 확보
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(), // 텍스트를 가운데 정렬하기 위해 전체 너비 사용
            )
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
            )
        }
        if (isLastPage) {
            Button(
                onClick = { onButtonClick?.invoke() },
                colors = ButtonDefaults.buttonColors(CustomPrimary),
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
            ) {
                Text("시작하기")
            }
        }
    }
}


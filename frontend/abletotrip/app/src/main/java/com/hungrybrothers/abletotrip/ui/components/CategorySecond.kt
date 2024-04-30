package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategorySecond(
    icon: Painter,
    label: String,
) {
    // TODO: [필수] 데이터 불러오고 나서 필터 기능 추가하기!
    var isSelected by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
                    // TODO: [CSS] background 컬러 Theme에 있는 CustomWhiteSmoke로 해줘야 함
                    .background(
                        if (isSelected) Color(0xFF8AD6CB) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isSelected = !isSelected }
                    // TODO: [CSS] Padding 좌우 끝쪽 여백 32 주는 디자인 추가하기
                    .padding(start = 32.dp, end = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }

        Text(
            text = label,
            fontSize = 12.sp,
            // TODO: [CSS] 컬러 커스텀 색깔로 수정
            color = Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

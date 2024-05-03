package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground

@Composable
fun AutocompleteTextField2(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CustomBackground)
                .padding(end = if (text.isNotEmpty()) 48.dp else 0.dp), // 수정된 부분
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        trailingIcon = {
            Row(
                Modifier.padding(end = 8.dp), // 아이콘 간격 조정
            ) {
                if (text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                } else {
                    Icon(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .clickable { /* Handle target icon click */ },
                        painter = painterResource(id = R.drawable.target),
                        contentDescription = "Target",
                    )
                    Icon(
                        modifier =
                            Modifier
                                .size(16.dp)
                                .clickable { /* Handle home icon click */ },
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Home",
                    )
                }
            }
        },
    )
}

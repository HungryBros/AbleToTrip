package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary

@Composable
fun SearchBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    onClear: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .requiredHeight(56.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder)
            },
            singleLine = true,
            modifier =
                Modifier
                    .weight(1f).padding(end = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search,
                ),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        onSearch()
                    },
                ),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray,
                    unfocusedContainerColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = Color.White, // 포커스 상태에서 밑줄 제거
                    unfocusedIndicatorColor = Color.Gray, // 비포커스 상태에서
                ),
            shape = RoundedCornerShape(12.dp),
        )
        Button(
            onClick = onSearch,
            modifier = Modifier.height(100.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = CustomPrimary,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "검색", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

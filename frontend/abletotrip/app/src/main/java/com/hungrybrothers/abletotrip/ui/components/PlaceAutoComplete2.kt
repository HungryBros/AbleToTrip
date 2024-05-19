package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomWhite
import com.hungrybrothers.abletotrip.ui.theme.CustomWhiteSmoke

@Composable
fun AutocompleteTextField2(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onClear: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyLarge)
        },
        singleLine = true,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CustomBackground, RoundedCornerShape(8.dp)),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        trailingIcon = {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        (
                            -16
                        ).dp,
                    ),
            ) {
                if (text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "cancel",
                        )
                    }
                }
                IconButton(onClick = onTargetClick) {
                    Icon(painter = painterResource(id = R.drawable.target2), contentDescription = "Target")
                }
            }
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = CustomWhiteSmoke,
                unfocusedContainerColor = CustomWhite,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = CustomWhiteSmoke,
                unfocusedIndicatorColor = CustomWhite,
            ),
        shape = RoundedCornerShape(8.dp),
    )
}

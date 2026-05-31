package com.example.mocopraktikum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchField() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⌕", fontSize = 32.sp)

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Ortssuche",
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 18.sp
            )
        }
    }
}
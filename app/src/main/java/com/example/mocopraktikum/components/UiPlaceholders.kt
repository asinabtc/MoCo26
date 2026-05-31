package com.example.mocopraktikum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

@Composable
fun ScreenTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "←",
            fontSize = 36.sp,
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable { onBackClick() }
        )

        Text(
            text = title,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Profil")
        }
    }
}

@Composable
fun InputPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 8.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 17.sp
        )
    }
}

@Composable
fun ButtonPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 18.sp)
    }
}

@Composable
fun TextAreaPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Text eingeben...\n\n• illegal → man bekommt Strafzettel\n• Privatgrundstück\n• etc.",
            fontSize = 16.sp
        )
    }
}

@Composable
fun SliderPlaceholder() {
    Column {
        Slider(
            value = 0f,
            onValueChange = {},
            valueRange = 0f..2f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Leer")
            Text("mäßig besucht")
            Text("Sehr voll")
        }
    }
}
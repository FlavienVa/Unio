package com.android.unio.ui.event

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventCreationScreen() {
  Scaffold(modifier = Modifier.testTag("EventCreationScreen")) { Text("Event creation screen") }
}

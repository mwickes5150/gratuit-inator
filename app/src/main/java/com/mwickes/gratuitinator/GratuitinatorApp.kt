package com.mwickes.gratuitinator

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme

/**
 * Top-level app composable. Currently just a placeholder screen with a debug
 * paper/steel toggle to verify the theme; the real Scaffold + NavHost lands in Phase 4.
 */
@Composable
fun GratuitinatorApp() {
    val systemDark = isSystemInDarkTheme()
    var darkSteel by remember { mutableStateOf(systemDark) }

    GratuitinatorTheme(darkSteel = darkSteel) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Gratuit-inator", style = MaterialTheme.typography.titleMedium)
                Switch(checked = darkSteel, onCheckedChange = { darkSteel = it })
                Text(text = if (darkSteel) "Steel" else "Paper", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GratuitinatorAppPreview() {
    GratuitinatorApp()
}

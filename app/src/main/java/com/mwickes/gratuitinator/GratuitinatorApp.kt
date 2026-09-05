package com.mwickes.gratuitinator

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mwickes.gratuitinator.ui.screens.main.MainScreen
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors

/**
 * Top-level app composable: a header (title + paper/steel toggle) over the Bill screen.
 * The Scaffold + NavHost with bottom nav (Bill/Split/Scan) lands in Phase 4; for now
 * [MainScreen] is hosted directly as the only screen.
 */
@Composable
fun GratuitinatorApp() {
    val systemDark = isSystemInDarkTheme()
    var darkSteel by remember { mutableStateOf(systemDark) }

    GratuitinatorTheme(darkSteel = darkSteel) {
        val tape = LocalTapeColors.current
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tape.stepBg)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "GRATUIT-INATOR",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        letterSpacing = 1.5.sp,
                        color = tape.stepFg,
                    )
                    IconButton(
                        onClick = { darkSteel = !darkSteel },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Text(text = if (darkSteel) "☀" else "☾", color = tape.stepFg)
                    }
                }

                val mainViewModel: MainViewModel = viewModel()
                MainScreen(viewModel = mainViewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GratuitinatorAppPreview() {
    GratuitinatorApp()
}

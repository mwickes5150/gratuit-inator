package com.mwickes.gratuitinator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mwickes.gratuitinator.navigation.Destination
import com.mwickes.gratuitinator.navigation.GratuitinatorNavGraph
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors
import com.mwickes.gratuitinator.ui.theme.TapeColors

/**
 * Top-level app composable: a header (title + paper/steel toggle) over a NavHost, with a minimal
 * Bill/Scan bottom bar. [MainViewModel] is owned here (once) and shared across destinations so
 * screens like Review can commit into the same bill state Main displays. Full pixel-accurate
 * Bill/Split/Scan bottom-nav polish is deferred — today's bar only reaches Bill/Scan (Review is
 * reachable only from Scan, not a tab), and Split isn't wired in yet.
 */
@Composable
fun GratuitinatorApp() {
    val systemDark = isSystemInDarkTheme()
    var darkSteel by remember { mutableStateOf(systemDark) }
    val mainViewModel: MainViewModel = viewModel()

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

                val navController = rememberNavController()
                GratuitinatorNavGraph(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                )

                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tape.cardBg),
                ) {
                    BottomTab(
                        label = "BILL",
                        selected = currentRoute == Destination.Main.route,
                        tape = tape,
                        onClick = {
                            navController.navigate(Destination.Main.route) {
                                popUpTo(Destination.Main.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                    BottomTab(
                        label = "SCAN",
                        selected = currentRoute == Destination.Scan.route,
                        tape = tape,
                        onClick = {
                            navController.navigate(Destination.Scan.route) {
                                popUpTo(Destination.Main.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomTab(
    label: String,
    selected: Boolean,
    tape: TapeColors,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = if (selected) tape.accent else tape.dim,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = 44.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun GratuitinatorAppPreview() {
    GratuitinatorApp()
}

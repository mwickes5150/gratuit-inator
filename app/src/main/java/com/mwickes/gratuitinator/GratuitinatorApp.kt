package com.mwickes.gratuitinator

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mwickes.gratuitinator.data.ThemePreferences
import com.mwickes.gratuitinator.navigation.Destination
import com.mwickes.gratuitinator.navigation.GratuitinatorNavGraph
import com.mwickes.gratuitinator.ui.components.MoonOutlineIcon
import com.mwickes.gratuitinator.ui.components.SunOutlineIcon
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.screens.splash.SplashScreen
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors
import com.mwickes.gratuitinator.ui.theme.TapeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Top-level app composable: a header (title + paper/steel toggle) over a NavHost, with a flat
 * Bill/Split/Scan bottom nav bar matching the mockup. [MainViewModel] is owned here (once) and
 * shared across destinations so screens like Review and Split can commit into / read the same
 * bill state Main displays. (Review is reachable only from Scan, not a bottom-nav tab; the
 * mockup's "TIP US" donation-link tab is a deprioritized future enhancement — see
 * DEVELOPMENT_PLAN.md.)
 */
@Composable
fun GratuitinatorApp() {
    val systemDark = isSystemInDarkTheme()
    var darkSteel by remember { mutableStateOf(systemDark) }
    val mainViewModel: MainViewModel = viewModel()
    var showSplash by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // A persisted choice overrides the system-dark seed above; no persisted value yet (first
        // launch) leaves the system-dark default in place.
        themePreferences.darkSteelFlow.first()?.let { persisted -> darkSteel = persisted }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
    }

    GratuitinatorTheme(darkSteel = darkSteel) {
        Crossfade(targetState = showSplash, label = "splash") { splash ->
            if (splash) {
                SplashScreen()
            } else {
                GratuitinatorContent(
                    mainViewModel = mainViewModel,
                    darkSteel = darkSteel,
                    onToggleDarkSteel = {
                        val newValue = !darkSteel
                        darkSteel = newValue
                        coroutineScope.launch { themePreferences.setDarkSteel(newValue) }
                    },
                )
            }
        }
    }
}

@Composable
private fun GratuitinatorContent(
    mainViewModel: MainViewModel,
    darkSteel: Boolean,
    onToggleDarkSteel: () -> Unit,
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CLEAR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = tape.stepFg,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                            .clickable { mainViewModel.onClear() }
                            .padding(horizontal = 8.dp)
                            .wrapContentSize(Alignment.Center),
                    )
                    IconButton(
                        onClick = onToggleDarkSteel,
                        modifier = Modifier
                            .size(44.dp)
                            .semantics {
                                contentDescription = if (darkSteel) "Switch to paper theme" else "Switch to steel theme"
                            },
                    ) {
                        if (darkSteel) {
                            SunOutlineIcon(color = tape.stepFg)
                        } else {
                            MoonOutlineIcon(color = tape.stepFg)
                        }
                    }
                }
            }

            val navController = rememberNavController()
            GratuitinatorNavGraph(
                navController = navController,
                mainViewModel = mainViewModel,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )

            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            fun navigateToTab(destination: Destination) {
                navController.navigate(destination.route) {
                    popUpTo(Destination.Main.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tape.cardBg),
            ) {
                BottomTab(
                    label = "BILL",
                    selected = currentRoute == Destination.Main.route,
                    tape = tape,
                    onClick = { navigateToTab(Destination.Main) },
                )
                BottomTab(
                    label = "SPLIT",
                    selected = currentRoute == Destination.Split.route,
                    tape = tape,
                    onClick = { navigateToTab(Destination.Split) },
                )
                BottomTab(
                    label = "SCAN",
                    selected = currentRoute == Destination.Scan.route,
                    tape = tape,
                    onClick = { navigateToTab(Destination.Scan) },
                )
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
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = if (selected) tape.stepFg else tape.dim,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = 56.dp)
            .background(if (selected) tape.stepBg else Color.Transparent)
            .clickable(onClick = onClick)
            .wrapContentSize(Alignment.Center),
    )
}

@Preview(showBackground = true)
@Composable
private fun GratuitinatorAppPreview() {
    GratuitinatorApp()
}

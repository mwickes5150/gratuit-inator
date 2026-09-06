package com.mwickes.gratuitinator.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mwickes.gratuitinator.ocr.TextRecognitionClient
import com.mwickes.gratuitinator.ui.screens.main.MainScreen
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.screens.review.ReviewScreen
import com.mwickes.gratuitinator.ui.screens.review.ReviewViewModel
import com.mwickes.gratuitinator.ui.screens.scan.ScanScreen
import com.mwickes.gratuitinator.ui.screens.scan.ScanViewModel

/**
 * Top-level nav graph. [mainViewModel] is owned once by the caller (GratuitinatorApp) and shared
 * across destinations so Review's "Use These" can commit into the same bill state Main displays.
 */
@Composable
fun GratuitinatorNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Main.route,
        modifier = modifier,
    ) {
        composable(Destination.Main.route) {
            MainScreen(viewModel = mainViewModel, modifier = Modifier.fillMaxSize())
        }

        composable(Destination.Scan.route) {
            val scanViewModel: ScanViewModel = viewModel()
            ScanScreen(
                viewModel = scanViewModel,
                onImageReady = { uri ->
                    navController.navigate(Destination.Review.route(uri))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(
            route = Destination.Review.route,
            arguments = listOf(navArgument(Destination.Review.ARG_URI) { type = NavType.StringType }),
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString(Destination.Review.ARG_URI).orEmpty()
            val uri = Uri.parse(Uri.decode(encodedUri))
            val context = LocalContext.current
            val reviewViewModel: ReviewViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ReviewViewModel(TextRecognitionClient(context.applicationContext)) }
                },
            )

            LaunchedEffect(uri) {
                reviewViewModel.loadAndParse(uri)
            }

            ReviewScreen(
                viewModel = reviewViewModel,
                onRetake = { navController.popBackStack() },
                onUseThese = { subtotal, tax ->
                    mainViewModel.onSubtotalChanged(subtotal)
                    mainViewModel.onTaxChanged(tax)
                    navController.navigate(Destination.Main.route) {
                        popUpTo(Destination.Main.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

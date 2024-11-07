package com.android.unio

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.authentication.AccountDetails
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.user.UserProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var imageRepository: ImageRepositoryFirebaseStorage


  @SuppressLint("SourceLockedOrientationActivity")
  override fun onCreate(savedInstanceState: Bundle?) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    super.onCreate(savedInstanceState)
    setContent {
      Surface(modifier = Modifier.fillMaxSize()) {
        ProvidePreferenceLocals { AppTheme { UnioApp(imageRepository) } }
      }
    }
  }
}

@HiltAndroidApp class UnioApplication : Application() {}

@Composable
fun UnioApp(imageRepository: ImageRepositoryFirebaseStorage) {
  val navController = rememberNavController()

  val navigationActions = NavigationAction(navController)

  val associationViewModel = hiltViewModel<AssociationViewModel>()
  val eventListViewModel = hiltViewModel<EventListViewModel>()
  val userViewModel = hiltViewModel<UserViewModel>()
  val searchViewModel = hiltViewModel<SearchViewModel>()
  val authViewModel = hiltViewModel<AuthViewModel>()

  val context = LocalContext.current

  // Observe the authentication state
  val authState by authViewModel.authState.collectAsState()
  var previousAuthState by rememberSaveable { mutableStateOf<String?>(null) }

  Log.d("UnioApp", "Auth state: $authState")

  LaunchedEffect(authState) {
    authState?.let { screen ->
      // Only navigate if the screen has changed
      Log.d("UnioApp", "Navigating to $screen")
      if (screen != previousAuthState) {
        navigationActions.navigateTo(screen)
        previousAuthState = screen
      }
    }
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(startDestination = Screen.WELCOME, route = Route.AUTH) {
      composable(Screen.WELCOME) { WelcomeScreen() }
      composable(Screen.EMAIL_VERIFICATION) { EmailVerificationScreen(navigationActions) }
      composable(Screen.ACCOUNT_DETAILS) {
        AccountDetails(navigationActions, userViewModel, imageRepository)
      }
    }
    navigation(startDestination = Screen.HOME, route = Route.HOME) {
      composable(Screen.HOME) { HomeScreen(navigationActions, eventListViewModel, userViewModel) }
      composable(Screen.MAP) { MapScreen(navigationActions, eventListViewModel) }
    }
    navigation(startDestination = Screen.EXPLORE, route = Route.EXPLORE) {
      composable(Screen.EXPLORE) {
        ExploreScreen(navigationActions, associationViewModel, searchViewModel)
      }
      composable(Screen.ASSOCIATION_PROFILE) { navBackStackEntry ->
        // Get the association UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")

        // Create the AssociationProfile screen with the association UID
        uid?.let {
          AssociationProfileScreen(navigationActions, it, associationViewModel, userViewModel)
        }
            ?: run {
              Log.e("AssociationProfile", "Association UID is null")
              Toast.makeText(context, "Association UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }
    navigation(startDestination = Screen.SAVED, route = Route.SAVED) {
      composable(Screen.SAVED) { SavedScreen(navigationActions) }
    }
    navigation(startDestination = Screen.MY_PROFILE, route = Route.MY_PROFILE) {
      composable(Screen.MY_PROFILE) { UserProfileScreen(navigationActions, userViewModel) }
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
    }
  }
}

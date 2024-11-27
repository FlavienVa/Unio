package com.android.unio.components.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.TearDown
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.model.strings.test_tags.SettingsTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.settings.SettingsScreen
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlin.reflect.full.memberProperties
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsTest : TearDown() {
  @MockK private lateinit var navigationAction: NavigationAction

  private lateinit var authViewModel: AuthViewModel
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    authViewModel = AuthViewModel(mockk(), mockk())
    userViewModel = UserViewModel(mockk(), mockk())
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent {
      ProvidePreferenceLocals { SettingsScreen(navigationAction, authViewModel, userViewModel) }
    }

    composeTestRule.onNodeWithTag(SettingsTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.CONTAINER).assertIsDisplayed()

    // Iterate through the values of AppPreferences and thus check that each setting exists
    AppPreferences::class.memberProperties.forEach { key ->
      composeTestRule.onNodeWithTag(key.call() as String).assertExists()
    }
  }
}

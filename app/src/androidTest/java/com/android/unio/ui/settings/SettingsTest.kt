package com.android.unio.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.full.memberProperties

class SettingsTest {
  @MockK private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent { ProvidePreferenceLocals { SettingsScreen(navigationAction) } }

    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SettingsContainer").assertIsDisplayed()

    // Iterate through the values of AppPreferences and thus check that each setting exists
    AppPreferences::class.memberProperties.forEach { key ->
      composeTestRule.onNodeWithTag(key.call() as String).assertExists()
    }
  }
}

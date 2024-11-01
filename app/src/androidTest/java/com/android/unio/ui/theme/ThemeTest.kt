package com.android.unio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.unio.model.preferences.PreferenceKeys
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Rule
import org.junit.Test

class ThemeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testTheme() {
    val preferencesFlow: MutableStateFlow<Preferences> =
        MutableStateFlow(MapPreferences(mapOf(PreferenceKeys.THEME to Theme.LIGHT)))

    composeTestRule.setContent {
      ProvidePreferenceLocals(flow = preferencesFlow) {
        AppTheme { assertEquals(primaryLight, MaterialTheme.colorScheme.primary) }
      }
    }
  }

  class MapMutablePreferences(private val map: MutableMap<String, Any> = mutableMapOf()) :
      MutablePreferences {
    @Suppress("UNCHECKED_CAST") override fun <T> get(key: String): T? = map[key] as T?

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences =
        MapMutablePreferences(map.toMutableMap())

    override fun <T> set(key: String, value: T?) {
      if (value != null) {
        map[key] = value
      } else {
        map -= key
      }
    }

    override fun clear() {
      map.clear()
    }
  }

  class MapPreferences(private val map: Map<String, Any> = emptyMap()) : Preferences {
    @Suppress("UNCHECKED_CAST") override fun <T> get(key: String): T? = map[key] as T?

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences =
        MapMutablePreferences(map.toMutableMap())
  }
}

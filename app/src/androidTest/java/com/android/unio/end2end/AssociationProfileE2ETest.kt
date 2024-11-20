package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.SomeoneElseUserProfileTestTags
import com.android.unio.ui.assertDisplayComponentInScroll
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class AssociationProfileE2ETest : EndToEndTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testAssociationProfileCanGoToSomeoneElseUserProfile() {
    signInWithUser(composeTestRule, User1.EMAIL, User1.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(ASSOCIATION_NAME))
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS))
    composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.NAME).assertIsDisplayed()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    // had to go back mutliple times in order to sign out (because we need to be in of a the
    // principal screens to sign out)
    signOutWithUser(composeTestRule)
  }

  private companion object AssociationTarget {
    const val ASSOCIATION_NAME = "Ebou"
    const val ASSOCIATION_MEMBERS = "Renata Mendoza Flores"
  }
}

package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.UserClaimAssociationPresidentialRightsTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

/**
 * The goal of this e2e test is to complete a whole action of claiming one association's
 * presidential rights
 */
@LargeTest
@HiltAndroidTest
class ClaimAdminRightsTest : EndToEndTest() {
  @Test
  fun testUserClaimRightsAccess() {
    /** Create an account on the welcome screen */
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    // Wait until "HomeScreen" is displayed
    composeTestRule.waitUntil(10001) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Wait until the bottom nav bar is displayed
    composeTestRule.waitUntil(10002) {
      composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

    composeTestRule.waitUntil(10003) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    /** Navigate to the claiming button screen */
    composeTestRule.onNodeWithTag(UserProfileTestTags.CLAIMING_BUTTON).performClick()

    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_INPUT)
        .performTextInput(ASSOCIATION_SEARCH_INPUT)

    // Wait for the server's response to get the association
    composeTestRule.waitUntil(10004) {
      composeTestRule
          .onNodeWithTag(
              ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(
            ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
        .performClick()

    composeTestRule.waitUntil(5005) {
      composeTestRule
          .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.SCREEN)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.EMAIL_ADDRESS)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.EMAIL_ADDRESS)
        .performTextInput(PRESIDENTIAL_EMAIL_ADDRESS)

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.VERIFY_EMAIL_BUTTON)
        .performClick()

    composeTestRule.waitUntil(5006) {
      composeTestRule
          .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.CODE)
          .isDisplayed()
    }

    Thread.sleep(8007) // wait a few seconds according to
    // https://firebase.google.com/docs/emulator-suite/connect_firestore#how_the_emulator_differs_from_production

    var finalCode = ""

    // In order not to catch a real email, we will just check what code is updated in the database
    // with admin access
    Firebase.firestore
        .collection("emailVerifications")
        .document(EXPECTED_ASSOCIATION_UID)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.exists()) {
            val code: String? = document.getString("code")
            if (code != null) {
              finalCode = code
            } else {
              throw IllegalStateException("Code field is missing in the document")
            }
          } else {
            throw IllegalStateException("Document does not exist")
          }
        }
        .addOnFailureListener { exception ->
          throw IllegalStateException("Failed to fetch verification code: ${exception.message}")
        }

    composeTestRule.waitUntil(30008) {
      finalCode.isNotEmpty()
    } // otherwise it directly goes to the rest of the code

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.CODE)
        .performTextInput(finalCode)

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.SUBMIT_CODE_BUTTON)
        .performClick()

    composeTestRule.waitUntil(10009) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    signOutWithUser(composeTestRule)
  }

  private companion object {
    const val ASSOCIATION_SEARCH_INPUT = "music"
    const val EXPECTED_ASSOCIATION_NAME = "Musical"
    const val PRESIDENTIAL_EMAIL_ADDRESS = "mock.mock@icloud.com"
    const val EXPECTED_ASSOCIATION_UID = "P0eaFO5qG9y9lK46x8nf"
  }
}

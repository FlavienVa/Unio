package com.android.unio.ui.user

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import java.util.GregorianCalendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileTest {

  @MockK private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  private val user = MockUser.createMockUser()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent { UserProfileScreenContent(navigationAction, user) }

    composeTestRule.onNodeWithTag("UserProfilePicture").assertExists()

    composeTestRule.onNodeWithTag("UserProfileName").assertExists()
    composeTestRule
        .onNodeWithTag("UserProfileName")
        .assertTextEquals("${user.firstName} ${user.lastName}")

    composeTestRule.onNodeWithTag("UserProfileBiography").assertExists()
    composeTestRule.onNodeWithTag("UserProfileBiography").assertTextEquals(user.biography)

    composeTestRule
        .onAllNodesWithTag("UserProfileSocialButton")
        .assertCountEquals(user.socials.size)
    composeTestRule.onAllNodesWithTag("UserProfileInterest").assertCountEquals(user.interests.size)
  }

  @Test
  fun testBottomSheet() {

    composeTestRule.setContent { UserProfileBottomSheet(true, navigationAction) {} }

    composeTestRule.onNodeWithTag("UserProfileBottomSheet").assertIsDisplayed()
  }
}

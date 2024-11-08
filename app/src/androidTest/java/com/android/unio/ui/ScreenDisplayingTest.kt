package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScaffold
import com.android.unio.ui.authentication.AccountDetails
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.event.EventCreationScreen
import com.android.unio.ui.event.EventScreenScaffold
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import com.android.unio.ui.user.SomeoneElseUserProfileScreen
import com.android.unio.ui.user.UserProfileScreenScaffold
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@HiltAndroidTest
class ScreenDisplayingTest {

  @MockK lateinit var navigationAction: NavigationAction

  private lateinit var userViewModel: UserViewModel
  @MockK lateinit var userRepository: UserRepositoryFirestore

  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var eventViewModel: EventViewModel

  @MockK private lateinit var imageRepositoryFirestore: ImageRepositoryFirebaseStorage

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    hiltRule.inject()

    associationViewModel =
        spyk(
            AssociationViewModel(
                mock(), mockk<EventRepositoryFirestore>(), imageRepositoryFirestore))
    eventViewModel = spyk(EventViewModel(mock(), mock()))
    userViewModel = UserViewModel(userRepository)
    val user = MockUser.createMockUser()
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(user, {})

    val associations = MockAssociation.createAllMockAssociations(size = 2)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    every { associationViewModel.findAssociationById(any()) } returns associations.first()
    every { associationViewModel.getEventsForAssociation(any(), any()) } answers
        {
          val onSuccess = args[1] as (List<Event>) -> Unit
          onSuccess(emptyList())
        }

    // Mocking the Firebase.auth object and it's behaviour
    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser
  }

  @Test
  fun testWelcomeDisplayed() {
    composeTestRule.setContent { WelcomeScreen() }
    composeTestRule.onNodeWithTag("WelcomeScreen").assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationDisplayed() {
    composeTestRule.setContent { EmailVerificationScreen(navigationAction) }
    composeTestRule.onNodeWithTag("EmailVerificationScreen").assertIsDisplayed()
  }

  @Test
  fun testAccountDetailsDisplayed() {
    composeTestRule.setContent {
      AccountDetails(navigationAction, userViewModel, imageRepositoryFirestore)
    }
    composeTestRule.onNodeWithTag("AccountDetails").assertIsDisplayed()
  }

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBarInput").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent { MapScreen(navigationAction, eventViewModel) }
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent {
      EventScreenScaffold(
          navigationAction = navigationAction, event = MockEvent.createMockEvent(), true, {})
    }
    composeTestRule.onNodeWithTag("EventScreen").assertIsDisplayed()
  }

  @Test
  fun testEventCreationDisplayed() {
    composeTestRule.setContent { EventCreationScreen() }
    composeTestRule.onNodeWithTag("EventCreationScreen").assertIsDisplayed()
  }

  @Test
  fun testAssociationProfileDisplayed() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
    }
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen(navigationAction) }
    composeTestRule.onNodeWithTag("SavedScreen").assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { ProvidePreferenceLocals { SettingsScreen(navigationAction) } }
    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
  }

  @Test
  fun testUserProfileDisplayed() {
    composeTestRule.setContent {
      UserProfileScreenScaffold(MockUser.createMockUser(), navigationAction, false) {}
    }
    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
  }

  @Test
  fun testSomeoneElseUserProfileDisplayed() {
    composeTestRule.setContent { SomeoneElseUserProfileScreen() }
    composeTestRule.onNodeWithTag("SomeoneElseUserProfileScreen").assertIsDisplayed()
  }
}

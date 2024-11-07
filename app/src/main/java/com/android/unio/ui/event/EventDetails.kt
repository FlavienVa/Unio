package com.android.unio.ui.event

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.android.unio.utils.EventUtils.formatTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private const val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"
private val DEBUG_LAMBDA: () -> Unit = {
  scope!!.launch {
    testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
  }
}

private val PLACEHOLDER_IMAGE_URL =
    "https://sidebarsydney.com.au/wp-content/themes/yootheme/cache/6d/Project_X_1920x1080-6d0c5833.jpeg"
private val ASSOCIATION_ICON_SIZE = 24.dp

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel // will be used later to show whether the event is saved){}
) {

  val event by eventViewModel.selectedEvent.collectAsState()

  // mock associations before linking to backend
  val associations =
      listOf(
          Association(
              uid = "1",
              url = "https://www.acm.org/",
              name = "ACM",
              fullName = "Association for Computing Machinery",
              category = AssociationCategory.SCIENCE_TECH,
              description =
                  "ACM is the world's largest educational and scientific computing society.",
              members = User.firestoreReferenceListWith(listOf("1", "2")),
              image = "https://www.example.com/image.jpg",
              followersCount = 0),
          Association(
              uid = "2",
              url = "https://www.ieee.org/",
              name = "IEEE",
              fullName = "Institute of Electrical and Electronics Engineers",
              category = AssociationCategory.SCIENCE_TECH,
              description =
                  "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
              members = User.firestoreReferenceListWith(listOf("3", "4")),
              image = "https://www.example.com/image.jpg",
              followersCount = 0))

  val context = LocalContext.current
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  Scaffold(
      modifier = Modifier.testTag("EventScreen"),
      snackbarHost = {
        SnackbarHost(
            hostState = testSnackbar!!,
            modifier = Modifier.testTag("eventSnackbarHost"),
            snackbar = { data ->
              Snackbar {
                TextButton(
                    onClick = { testSnackbar!!.currentSnackbarData?.dismiss() },
                    modifier = Modifier.testTag("snackbarActionButton")) {
                      Text(text = DEBUG_MESSAGE)
                    }
              }
            })
      },
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            },
            actions = {
              IconButton(modifier = Modifier.testTag("eventSaveButton"), onClick = DEBUG_LAMBDA) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = context.getString(R.string.event_save_button_description))
              }
              IconButton(modifier = Modifier.testTag("eventShareButton"), onClick = DEBUG_LAMBDA) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = context.getString(R.string.event_share_button_description))
              }
            })
      },
      content = { padding ->
        if (event == null) {
          Text(context.getString(R.string.event_location_button_description))
        } else {

          EventScreenContent(event!!, padding)
        }
      })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventScreenContent(event: Event, padding: PaddingValues) {
  val context = LocalContext.current
  Column(
      modifier =
          Modifier.testTag("eventDetailsPage")
              .verticalScroll(rememberScrollState())
              .padding(padding)) {
        AsyncImage(
            event.image.toUri(),
            context.getString(R.string.event_image_description),
            placeholder = painterResource(R.drawable.weskic),
            modifier = Modifier.fillMaxSize().testTag("eventDetailsImage"))

        Column(
            modifier =
                Modifier.testTag("eventDetailsInformationCard")
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp)
                    .fillMaxWidth()) {
              Text(
                  event.title,
                  modifier = Modifier.testTag("eventTitle").align(Alignment.CenterHorizontally),
                  style = AppTypography.headlineLarge,
                  color = MaterialTheme.colorScheme.onPrimary)

              Row(modifier = Modifier.align(Alignment.Start)) {
                val associations by event.organisers.list.collectAsState()
                for (i in associations.indices) {
                  Row(
                      modifier =
                          Modifier.testTag("eventOrganisingAssociation$i").padding(end = 6.dp),
                      horizontalArrangement = Arrangement.Center) {
                        AsyncImage(
                            associations[i].image.toUri(),
                            context.getString(R.string.event_association_icon_description),
                            placeholder = painterResource(R.drawable.weskic),
                            modifier =
                                Modifier.size(ASSOCIATION_ICON_SIZE)
                                    .clip(CircleShape)
                                    .align(Alignment.CenterVertically)
                                    .testTag("associationLogo$i"),
                            contentScale = ContentScale.Crop,
                        )

                        Text(
                            associations[i].name,
                            modifier = Modifier.testTag("associationName$i").padding(start = 3.dp),
                            style = AppTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary)
                      }
                }
              }
              Row {
                Text(
                    formatTimestamp(event.date, SimpleDateFormat("HH:mm", Locale.getDefault())),
                    modifier = Modifier.testTag("eventStartHour").weight(1f),
                    color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    formatTimestamp(event.date, SimpleDateFormat("dd/MM", Locale.getDefault())),
                    modifier = Modifier.testTag("eventDate"),
                    color = MaterialTheme.colorScheme.onPrimary)
              }
            }
        Column(modifier = Modifier.testTag("eventDetailsBody").padding(9.dp)) {
          Text(
              "X places remaining",
              modifier = Modifier.testTag("placesRemainingText"),
              style = AppTypography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary)
          Text(
              event.description,
              modifier = Modifier.testTag("eventDescription").padding(6.dp),
              style = AppTypography.bodyMedium)

          Spacer(modifier = Modifier.height(10.dp))
          Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally).wrapContentWidth()) {
                  Text(
                      event.location.name,
                      modifier = Modifier.testTag("eventLocation").padding(end = 5.dp))
                  Button(
                      onClick = DEBUG_LAMBDA,
                      modifier = Modifier.testTag("mapButton").size(48.dp),
                      shape = CircleShape,
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.inversePrimary,
                              contentColor = MaterialTheme.colorScheme.primary),
                      contentPadding = PaddingValues(0.dp)) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription =
                                context.getString(R.string.event_location_button_description),
                        )
                      }
                }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = DEBUG_LAMBDA,
                modifier =
                    Modifier.testTag("signUpButton")
                        .align(Alignment.CenterHorizontally)
                        .wrapContentWidth()
                        .height(56.dp),
                shape = CircleShape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        contentColor = MaterialTheme.colorScheme.primary)) {
                  Icon(
                      Icons.AutoMirrored.Filled.DirectionsWalk,
                      contentDescription =
                          context.getString(R.string.event_signup_button_description),
                  )
                  Text(context.getString(R.string.event_sign_up))
                }
          }
        }
      }
}

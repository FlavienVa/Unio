package com.android.unio.ui.event

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventUtils.addAlphaToColor
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.notification.UnioNotification
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.android.unio.model.strings.NotificationStrings.EVENT_REMINDER_CHANNEL_ID
import com.android.unio.model.strings.test_tags.EventCardTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Locale

const val SECONDS_IN_AN_HOUR = 3600

@Composable
fun EventCard(
    navigationAction: NavigationAction,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    shouldBeEditable: Boolean =
        false // To be changed in the future once permissions are implemented
) {
  val context = LocalContext.current
  val user by userViewModel.user.collectAsState()
  val associations by event.organisers.list.collectAsState()
  var isNotificationsEnabled by remember { mutableStateOf(false) }
  when (PackageManager.PERMISSION_GRANTED) {
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) -> {
      isNotificationsEnabled = true
    }
  }

  if (user == null) {
    Log.e("EventCard", "User not found.")
    Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  var isSaved by remember { mutableStateOf(user!!.savedEvents.contains(event.uid)) }

  val scheduleReminderNotification = {
    NotificationWorker.schedule(
        context,
        UnioNotification(
            title = event.title,
            message = context.getString(R.string.notification_event_reminder),
            icon = R.drawable.other_icon,
            channelId = EVENT_REMINDER_CHANNEL_ID,
            channelName = EVENT_REMINDER_CHANNEL_ID,
            notificationId = event.uid.hashCode(),
            // Schedule a notification a few hours before the event's startDate
            timeMillis = (event.startDate.seconds - 2 * SECONDS_IN_AN_HOUR) * SECONDS_IN_AN_HOUR))
  }

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        when {
          permission -> {
            isNotificationsEnabled = true
            scheduleReminderNotification()
          }
          else -> {
            isNotificationsEnabled = false
            Log.e("EventCard", "Notification permission is not granted.")
          }
        }
      }
  val onClickSaveButton = {
    if (isSaved) {
      userViewModel.unsaveEvent(event) {
        if (isNotificationsEnabled) {
          NotificationWorker.unschedule(context, event.uid.hashCode())
        }
      }
    } else {
      userViewModel.saveEvent(event) {
        if (!isNotificationsEnabled) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // this permission requires api 33
            // We should check how to make notifications work with lower api versions
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        } else {
          scheduleReminderNotification()
        }
      }
    }
    isSaved = !isSaved
  }

  EventCardScaffold(
      event,
      associations,
      isSaved,
      onClickEventCard = {
        eventViewModel.selectEvent(event.uid)
        navigationAction.navigateTo(Screen.EVENT_DETAILS)
      },
      onClickSaveButton = onClickSaveButton,
      onClickEditButton = {
        eventViewModel.selectEvent(event.uid)
        navigationAction.navigateTo(Screen.EDIT_EVENT)
      },
      shouldBeEditable = shouldBeEditable)
}

@Composable
fun EventCardScaffold(
    event: Event,
    organisers: List<Association>,
    isSaved: Boolean,
    onClickEventCard: () -> Unit,
    onClickSaveButton: () -> Unit,
    onClickEditButton: () -> Unit,
    shouldBeEditable: Boolean
) {
  val context = LocalContext.current
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(EventCardTestTags.EVENT_ITEM)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onClickEventCard() }) {

        // Event image section, displays the main event image or a placeholder if the URL is invalid

        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
          AsyncImageWrapper(
              imageUri = event.image.toUri(),
              contentDescription =
                  context.getString(R.string.event_card_content_description_event_image),
              modifier =
                  Modifier.fillMaxWidth()
                      .height(100.dp)
                      .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                      .testTag(EventCardTestTags.EVENT_IMAGE),
              contentScale = ContentScale.Crop // crop the image to fit
              )

          // Save button icon on the top right corner of the image, allows the user to save/unsave
          // the event
          Row(
              modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
              horizontalArrangement = Arrangement.SpaceBetween) {
                if (shouldBeEditable) {
                  IconButton(
                      modifier =
                          Modifier.size(28.dp)
                              .clip(RoundedCornerShape(14.dp))
                              .background(MaterialTheme.colorScheme.inversePrimary)
                              .padding(4.dp)
                              .testTag(EventCardTestTags.EDIT_BUTTON),
                      onClick = { onClickEditButton() }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "editassociation",
                            tint = Color.White)
                      }
                }
                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    modifier =
                        Modifier.size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.inversePrimary)
                            .padding(4.dp)
                            .testTag(EventCardTestTags.EVENT_SAVE_BUTTON),
                    onClick = { onClickSaveButton() }) {
                      Icon(
                          imageVector =
                              if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                          contentDescription =
                              if (isSaved)
                                  context.getString(
                                      R.string.event_card_content_description_saved_event)
                              else
                                  context.getString(
                                      R.string.event_card_content_description_not_saved_event),
                          tint = if (isSaved) Color.Red else Color.White)
                    }
              }
        }

        // Event details section, including title, type, location, and time

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {

          // Row containing event title and type label

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag(EventCardTestTags.EVENT_TITLE)
                          .wrapContentWidth(),
                  text = event.title,
                  style = AppTypography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.width(6.dp))

              // Display event type (e.g., Music, Sports) with colored background

              val type: EventType =
                  if (event.types.isEmpty()) {
                    EventType.OTHER
                  } else event.types[0]
              Box(
                  modifier =
                      Modifier.clip(RoundedCornerShape(4.dp))
                          .background(addAlphaToColor(type.color, 200))
                          .wrapContentWidth()) {
                    Text(
                        text = type.text,
                        modifier =
                            Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                .testTag(EventCardTestTags.EVENT_MAIN_TYPE),
                        color = MaterialTheme.colorScheme.scrim,
                        style = TextStyle(fontSize = 8.sp))
                  }
              Spacer(modifier = Modifier.weight(1f))
              // Organiser associations' logos on the right of the title and type
              for (i in organisers.indices) {
                AsyncImageWrapper(
                    imageUri = organisers[i].image.toUri(),
                    contentDescription =
                        context.getString(R.string.event_card_content_description_association_logo),
                    modifier =
                        Modifier.testTag("${EventCardTestTags.ASSOCIATION_LOGO}$i")
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                            .padding(end = 3.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop,
                    placeholderResourceId = R.drawable.adec,
                    filterQuality = FilterQuality.None)
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          // Row displaying event location and formatted date/time details

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag(EventCardTestTags.EVENT_LOCATION)
                          .wrapContentWidth(), // Make sure the text only takes as much space as
                  // needed
                  text = event.location.name,
                  style = AppTypography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(1.dp))

            // Date display for the event

            Text(
                modifier =
                    Modifier.padding(vertical = 1.dp, horizontal = 0.dp)
                        .testTag(EventCardTestTags.EVENT_DATE),
                text =
                    formatTimestamp(
                        event.startDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.width(2.dp))

            Spacer(
                modifier =
                    Modifier.height(10.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.primary))

            Spacer(modifier = Modifier.width(2.dp))

            // Time display for the event

            Text(
                modifier = Modifier.testTag(EventCardTestTags.EVENT_TIME).wrapContentWidth(),
                text =
                    formatTimestamp(
                        event.startDate, SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)
          }

          // Divider line below location and time row

          Row {
            Spacer(modifier = Modifier.width(4.dp))
            Spacer(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary))
          }

          // Catchy description section with a brief highlight of the event

          Text(
              modifier =
                  Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                      .testTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION)
                      .wrapContentWidth(),
              text = event.catchyDescription,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface)
        }
      }
}

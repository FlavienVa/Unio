package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.io.InputStream

open class EventViewModel(val repository: EventRepository, val userRepository: UserRepository) :
    ViewModel() {

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Check if the requested model class is EventViewModel
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
              return EventViewModel(
                  EventRepositoryFirestore(Firebase.firestore),
                  UserRepositoryFirestore(Firebase.firestore))
                  as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }

  // Image repository - avoid initializing in preview mode
  private val imageRepository by lazy {
    if (!isPreviewMode()) ImageRepositoryFirebaseStorage() else null
  }

  /** Helper function to determine if the app is running in preview mode. */
  private fun isPreviewMode(): Boolean {
    // Check if the current environment is a preview (used in Compose Previews)
    return android.os.Build.FINGERPRINT.contains("generic") ||
        android.os.Build.FINGERPRINT.contains("emulator") ||
        android.os.Build.MODEL.contains("sdk_gphone")
  }

  /** Add a new event to the repository. It uploads the event image first, then adds the event. */
  fun addEvent(
      inputStream: InputStream,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    imageRepository?.uploadImage(
        inputStream,
        "images/events/${event.uid}",
        { uri ->
          event.image = uri
          event.uid = repository.getNewUid()
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })
        ?: Log.e("EventListViewModel", "ImageRepository is not available in preview mode.")
  }

  fun listenToSavedEventsForCurrentUser(onSavedEventsChanged: (List<String>) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
      Log.w("Firestore", "No user logged in, unable to listen to saved events")
      onSavedEventsChanged(emptyList())
      return
    }
    currentUser?.uid?.let { userUid ->
      userRepository.listenToSavedEvents(userUid, onSavedEventsChanged)
    }
        ?: run {
          Log.w("Firestore", "No user logged in, unable to listen to saved events")
          onSavedEventsChanged(emptyList())
        }
  }

  fun saveEventForCurrentUser(
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
      Log.w("Firestore", "No user logged in, unable to save event")
      onFailure(Exception("No user logged in"))
      return
    }
    currentUser?.uid?.let { userUid ->
      userRepository.saveEvent(userUid, eventUid, onSuccess, onFailure)
    }
        ?: run {
          Log.w("Firestore", "No user logged in, unable to save event")
          onFailure(Exception("No user logged in"))
        }
  }

  fun unsaveEventForCurrentUser(
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
      Log.w("Firestore", "No user logged in, unable to unsave event")
      onFailure(Exception("No user logged in"))
      return
    }
    currentUser?.uid?.let { userUid ->
      userRepository.unsaveEvent(userUid, eventUid, onSuccess, onFailure)
    }
        ?: run {
          Log.w("Firestore", "No user logged in, unable to unsave event")
          onFailure(Exception("No user logged in"))
        }
  }

  fun isEventSavedForCurrentUser(eventUid: String, onResult: (Boolean) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
      Log.w("Firestore", "No user logged in")
      onResult(false)
      return
    }
    currentUser?.uid?.let { userUid -> userRepository.isEventSaved(userUid, eventUid, onResult) }
        ?: run {
          Log.w("Firestore", "No user logged in")
          onResult(false)
        }
  }
}

class PreviewEventViewModel(repository: EventRepository, userRepository: UserRepository) :
    EventViewModel(repository, userRepository) {

  var events: List<Event> = mutableListOf()

  init {
    repository.getEvents(onSuccess = { events = it }, onFailure = {})
  }
}

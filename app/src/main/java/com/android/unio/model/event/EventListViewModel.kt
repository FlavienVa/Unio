package com.android.unio.model.event

import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * ViewModel class that manages the event list data and provides it to the UI. It uses an
 * [EventRepository] to load the list of events and exposes them through a [StateFlow] to be
 * observed by the UI.
 *
 * @property repository The [EventRepository] that provides the events.
 */
class EventListViewModel(private val repository: EventRepository) : ViewModel() {

    /**
     * A private mutable state flow that holds the list of events. It is internal to the ViewModel and
     * cannot be modified from the outside.
     */
    private val _events = MutableStateFlow<List<Event>>(emptyList())

    /**
     * A public immutable [StateFlow] that exposes the list of events to the UI. This flow can only be
     * observed and not modified.
     */
    val events: StateFlow<List<Event>> = _events

    // Image repository - avoid initializing in preview mode
    private val imageRepository by lazy {
        if (!isPreviewMode()) ImageRepositoryFirebaseStorage() else null
    }

    /** Initializes the ViewModel by loading the events from the repository. */
    init {
        loadEvents()
    }

    /**
     * Loads the list of events from the repository asynchronously using coroutines and updates the
     * internal [MutableStateFlow].
     */
    private fun loadEvents() {
        // Launch a coroutine in the ViewModel scope to load events asynchronously
        viewModelScope.launch {
            repository.getEvents(
                onSuccess = { eventList ->
                    _events.value = eventList // Update the state flow with the loaded events
                },
                onFailure = { exception ->
                    // Handle error (e.g., log it, show a message to the user)
                    Log.e("EventViewModel", "An error occurred while loading events: $exception")
                    _events.value = emptyList() // Clear events on failure or handle accordingly
                }
            )
        }
    }

    /**
     * Add a new event to the repository. It uploads the event image first, then adds the event.
     */
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
            { e -> Log.e("ImageRepository", "Failed to store image: $e") }
        ) ?: Log.e("EventListViewModel", "ImageRepository is not available in preview mode.")
    }

    /**
     * Helper function to determine if the app is running in preview mode.
     */
    private fun isPreviewMode(): Boolean {
        // Check if the current environment is a preview (used in Compose Previews)
        return android.os.Build.FINGERPRINT.contains("generic") ||
                android.os.Build.FINGERPRINT.contains("emulator") ||
                android.os.Build.MODEL.contains("sdk_gphone")
    }

    /**
     * Companion object that provides a factory for creating instances of [EventListViewModel].
     */
    companion object {
        /** A factory for creating [EventListViewModel] instances with the [EventRepositoryMock]. */
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                /**
                 * Creates an instance of the [EventListViewModel].
                 *
                 * @param modelClass The class of the ViewModel to create.
                 * @return The created ViewModel instance.
                 * @throws IllegalArgumentException if the [modelClass] does not match.
                 */
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventListViewModel(EventRepositoryMock()) as T
                }
            }
    }
}
package com.android.unio.model.event

import androidx.test.core.app.ApplicationProvider
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.io.InputStream
import java.util.GregorianCalendar
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventViewModelTest {
  @Mock private lateinit var repository: EventRepositoryFirestore
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream

  @MockK lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var associationRepositoryFirestore: AssociationRepositoryFirestore

  private lateinit var eventViewModel: EventViewModel

  private val testEvents =
      listOf(
          MockEvent.createMockEvent(
              uid = "1",
              title = "Balelec",
              price = 40.5,
              startDate = Timestamp(GregorianCalendar(2004, 7, 1).time),
              endDate = Timestamp(GregorianCalendar(2005, 7, 1).time)),
          MockEvent.createMockEvent(
              uid = "2",
              title = "Tremplin Sysmic",
              price = 40.5,
              startDate = Timestamp(GregorianCalendar(2004, 7, 1).time),
              endDate = Timestamp(GregorianCalendar(2005, 7, 1).time)))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    MockKAnnotations.init(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    `when`(db.collection(any())).thenReturn(collectionReference)

    every { imageRepository.uploadImage(any(), any(), any(), any()) } answers
        {
          val onSuccess = args[2] as (String) -> Unit
          onSuccess("url")
        }

    every { associationRepositoryFirestore.getAssociations(any(), any()) } answers {}
    every { associationRepositoryFirestore.saveAssociation(any(), any(), any()) } answers {}

    eventViewModel = EventViewModel(repository, imageRepository, associationRepositoryFirestore)
  }

  @Test
  fun addEventandUpdateTest() {
    val event = testEvents.get(0)
    `when`(repository.addEvent(eq(event), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }
    `when`(repository.getNewUid()).thenReturn("1")
    eventViewModel.addEvent(
        inputStream, event, { verify(repository).addEvent(eq(event), any(), any()) }, {})
  }

  @Test
  fun updateEventTest() {
    val event = testEvents.get(0)
    `when`(repository.addEvent(eq(event), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }
    eventViewModel.updateEvent(
        inputStream, event, { verify(repository).addEvent(eq(event), any(), any()) }, {})
  }

  @Test
  fun updateEventWithoutImageTest() {
    val event = testEvents.get(0)
    `when`(repository.addEvent(eq(event), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }
    eventViewModel.updateEventWithoutImage(
        event, { verify(repository).addEvent(eq(event), any(), any()) }, {})
  }

  @Test
  fun deleteEventTest() {
    val event = testEvents.get(0)
    `when`(repository.deleteEventById(eq(event.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }
    eventViewModel.deleteEvent(
        event, { verify(repository).deleteEventById(eq(event.uid), any(), any()) }, {})
  }

  @Test
  fun testFindEventById() {
    `when`(repository.getEvents(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Event>) -> Unit
      onSuccess(testEvents)
    }

    eventViewModel.loadEvents()
    assertEquals(testEvents, eventViewModel.events.value)

    runBlocking {
      val result = eventViewModel.events.first()

      assertEquals(2, result.size)
      assertEquals("Balelec", result[0].title)
      assertEquals("Tremplin Sysmic", result[1].title)
    }

    assertEquals(testEvents[0], eventViewModel.findEventById("1"))
    assertEquals(testEvents[1], eventViewModel.findEventById("2"))
    assertEquals(null, eventViewModel.findEventById("3"))
  }

  @Test
  fun testSelectEvent() {
    `when`(repository.getEvents(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Event>) -> Unit
      onSuccess(testEvents)
    }

    eventViewModel.loadEvents()

    eventViewModel.selectEvent(testEvents[0].uid)
    assertEquals(testEvents[0], eventViewModel.selectedEvent.value)
  }
}

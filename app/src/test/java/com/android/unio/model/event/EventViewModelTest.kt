package com.android.unio.model.event

import androidx.test.core.app.ApplicationProvider
import com.android.unio.mocks.event.MockEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.GregorianCalendar
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

  private lateinit var viewModel: EventListViewModel

  private val event1 =
      MockEvent.createMockEvent(
          uid = "1",
          title = "Balelec",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2004, 7, 1).time))
  private val event3 =
      MockEvent.createMockEvent(
          uid = "3",
          title = "Tremplin Sysmic",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2008, 7, 1).time))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    `when`(db.collection(any())).thenReturn(collectionReference)

    viewModel = EventListViewModel(repository)
  }

  @Test
  fun addEventTest() {
    `when`(repository.addEvent(eq(event1), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }
    viewModel.addEvent(
        inputStream, event1, { verify(repository).addEvent(eq(event1), any(), any()) }, {})
  }
}

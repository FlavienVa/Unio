package com.android.unio.model.firestore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ReferenceList<T> {
  val list: StateFlow<List<T>>

  fun add(uid: String)

  fun addAll(uids: List<String>)

  fun requestAll()
}

class MockReferenceList<T>(associations: List<T> = emptyList()) : ReferenceList<T> {
  private val _list = MutableStateFlow(associations)
  override val list: StateFlow<List<T>> = _list

  override fun add(uid: String) {}

  override fun addAll(uids: List<String>) {}

  override fun requestAll() {}
}

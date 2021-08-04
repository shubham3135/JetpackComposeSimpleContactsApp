package com.shubhamkumarwinner.composecontactsapp

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ContactsViewModel(private val application: Application) : ViewModel() {
    private val _contacts = MutableLiveData<List<Contacts>>()
    val contacts: LiveData<List<Contacts>> get() = _contacts

    private var contentObserver: ContentObserver? = null


    fun loadContacts() {
        viewModelScope.launch {
            _contacts.value = queryContact()
            if (contentObserver == null) {
                contentObserver = application.applicationContext.contentResolver.registerObserver(
                    ContactsContract.Contacts.CONTENT_URI
                ) {
                    loadContacts()
                }
            }
        }
    }

    private fun queryContact(): List<Contacts> {
        val contactList = mutableListOf<Contacts>()
        val contentResolver = application.applicationContext.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val sortOrder =
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY.uppercase()} ASC"

        viewModelScope.launch {
            val query = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder,
            )

            query?.use { cursor ->
                // Cache column indices.
                val nameColumn =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val numberColumn =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)


                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)
                    var number = cursor.getString(numberColumn)
                    number = number.replace("[()\\s-]+", "")
                    if (number.length >= 10) {
                        contactList += Contacts(name, number)
                    }
                }
            }
        }
        return contactList
    }

    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit,
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }
}

class ContactsViewModelProvider(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
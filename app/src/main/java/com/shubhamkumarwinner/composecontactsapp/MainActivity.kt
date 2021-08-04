package com.shubhamkumarwinner.composecontactsapp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.shubhamkumarwinner.composecontactsapp.ui.theme.ComposeContactsAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ContactsViewModel by viewModels()

    @ExperimentalPermissionsApi
    @ExperimentalFoundationApi
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var listContact = mutableListOf<Contacts>()

        setContent {
            var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

            // Contact permission state
            val contactPermissionState = rememberPermissionState(
                Manifest.permission.READ_CONTACTS
            )

            when {
                contactPermissionState.hasPermission -> {
                    viewModel.loadContacts()
                    viewModel.contacts.observe(this, Observer {
                        listContact = it.toMutableList()
                    })
                    ComposeContactsAppTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(color = MaterialTheme.colors.background) {
                            Scaffold(topBar = {
                                TopAppBar(title = { Text(text = "Contacts") })
                            }) {
                                ContactsList(contacts = listContact.toList(),
                                    modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
                contactPermissionState.shouldShowRationale ||
                        !contactPermissionState.permissionRequested -> {
                    if (doNotShowRationale) {
                        Text("Feature not available")
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "The contact is important for this app. Please grant the permission.",
                                modifier = Modifier.align(CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { contactPermissionState.launchPermissionRequest() }) {
                                Text("Request permission")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { doNotShowRationale = true }) {
                                Text("Don't show rationale again")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Contact permission denied. See this FAQ with information about why we " +
                                    "need this permission. Please, grant us access on the Settings screen."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .apply {
                                        data = Uri.parse("package:$packageName")
                                    })
                        }) {
                            Text("Open Settings")
                        }
                    }
                }
            }
        }
    }
}


@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ContactsList(
    contacts: List<Contacts>,
    modifier: Modifier = Modifier,
) {
    val grouped = contacts.groupBy { it.name[0] }
    Box(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            grouped.forEach { (initials, contacts) ->
                stickyHeader {
                    CharacterHeader(char = initials, modifier = Modifier.fillMaxWidth())
                }
                items(contacts) { contact ->
                    ContactListItem(contact = contact, modifier = Modifier.fillMaxWidth())
                }
            }

        }
    }
}

@Composable
fun CharacterHeader(
    char: Char,
    modifier: Modifier,
) {
    Text(
        text = char.toString(),
        modifier = modifier
            .background(Color.LightGray)
            .padding(horizontal = 10.dp),
        color = Color.Black
    )
}

@Composable
fun ContactListItem(
    contact: Contacts,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(10.dp)) {
        Text(text = contact.name)
        Text(text = contact.number)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeContactsAppTheme {

    }
}
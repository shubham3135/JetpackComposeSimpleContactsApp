package com.shubhamkumarwinner.composecontactsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shubhamkumarwinner.composecontactsapp.ui.theme.ComposeContactsAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ContactsViewModel
    private lateinit var viewModelProvider: ContactsViewModelProvider

    @ExperimentalFoundationApi
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelProvider = ContactsViewModelProvider(application)
        viewModel = ViewModelProvider(this, viewModelProvider).get(ContactsViewModel::class.java)
        var listContact = mutableListOf<Contacts>()
        viewModel.loadContacts()
        viewModel.contacts.observe(this, Observer {
            listContact = it.toMutableList()
        })
        setContent {
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
    }
}


@ExperimentalFoundationApi
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ContactsList(
    contacts: List<Contacts>,
    modifier: Modifier = Modifier,
) {

    var permissionGranted = false
    val activityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { result ->
        /*if (result){
            permissionGranted = true

        }
        else if(!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("You have permanently denied contact permission")
            alertDialog.setMessage("Please allow it in your settings")
            alertDialog.setPositiveButton(
                "Settings"
            ) { _, _ ->
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .apply {
                            data = Uri.parse("package"+ applicationContext.packageName)
                        })
            }
            alertDialog.setNegativeButton(
                "Not now"
            ) { _, _ ->

            }
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
        else{
Toast.makeText(LocalContext.current,
                "Permission is denied",
                Toast.LENGTH_SHORT).show()
        }*/
    }
    val context = LocalContext.current

    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) -> {
            // Some works that require permission
            Log.d("ExampleScreen", "Code requires permission")
            permissionGranted = true
        }
        else -> {
            // Asking for permission
            activityResultLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    val grouped = contacts.groupBy { it.name[0] }
    if (permissionGranted) {
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
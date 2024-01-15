package com.michaelcheng924.bibleversesabout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.michaelcheng924.bibleversesabout.ui.theme.BibleVersesAboutTheme
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BibleVersesAboutTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var responseData by remember { mutableStateOf(emptyList<Name>()) }
                    LaunchedEffect(Unit) {
                        responseData = fetchAndParseJson()
                    }

                    // Dummy data list
                    val listData = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")

                    // LazyColumn to display the list
                    LazyColumn {
                        items(responseData) { item ->
                            ListItemText(item)
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchAndParseJson(): List<Name> = withContext(Dispatchers.IO) {

        val url = URL("https://bible-verses-about.vercel.app/slugs-name.json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream
        val jsonString = inputStream.bufferedReader().use { it.readText() }


        Json.decodeFromString<List<Name>>(jsonString)
    }
}

@Serializable
data class Name(val name: String, val slug: String)

@Composable
fun ListItemText(item: Name) {
    Text(
        text = item.name,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        style = TextStyle(fontSize = 32.sp)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BibleVersesAboutTheme {
        Greeting("Android")
    }
}
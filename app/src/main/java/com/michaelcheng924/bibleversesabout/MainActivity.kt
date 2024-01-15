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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BibleVersesAboutTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = BibleVersesAboutDestinations.LIST_SCREEN
                    ) {
                        composable(route = BibleVersesAboutDestinations.LIST_SCREEN) {
                            ListScreen(navController = navController)
                        }
                        composable(route = "$BibleVersesAboutDestinations.DETAILS_SCREEN/{slug}") { backStackEntry ->
                            // Your details screen composable
                            val slug = backStackEntry.arguments?.getString("slug")
                            if (slug != null) {
                                DetailScreen(slug = slug, navController = navController)
                            }
                        }
                    }
                }
            }
        }

    }
}

suspend fun fetchAndParseJson(): List<Name> = withContext(Dispatchers.IO) {

    val url = URL("https://bible-verses-about.vercel.app/slugs-name.json")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connect()

    val inputStream = connection.inputStream
    val jsonString = inputStream.bufferedReader().use { it.readText() }


    Json.decodeFromString<List<Name>>(jsonString)
}

suspend fun fetchAndParseTopicJson(slug: String): Topic = withContext(Dispatchers.IO) {

    val baseUrl = "https://bible-verses-about.vercel.app"
    val url = URL("$baseUrl/verses-json/$slug.json")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connect()

    val inputStream = connection.inputStream
    val jsonString = inputStream.bufferedReader().use { it.readText() }
    Log.d("JsonString", jsonString)

    Json.decodeFromString<Topic>(jsonString)
}

fun filterDataBySearchQuery(data: List<Name>, query: String): List<Name> {
    return data.filter { name ->
        // You can customize the filter logic here based on your requirements
        name.name.contains(query, ignoreCase = true)
    }
}

@Serializable
data class Name(val name: String, val slug: String)

@Serializable
data class Topic(
    val _id: String,
    val slug: String,
    val name: String,
    val verses: List<Verse>
)

@Serializable
data class Verse(
    val _id: String? = "",
    val __v: Int? = 0,
    val topicId: String? = "",
    val verse: String = "",
    val esv: String = "",
    val kjv: String = "",
    val niv: String = "",
    val votes: Int = 0,
    val modified: String = ""
)

@Composable
fun ListItemText(item: Name, navController: NavController) {
    Text(
        text = item.name,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .clickable {
                // Navigate to the details screen and pass the "slug" as an argument
                navController.navigate("$BibleVersesAboutDestinations.DETAILS_SCREEN/${item.slug}")
            },
        style = TextStyle(fontSize = 32.sp)
    )
}

@Composable
fun ListScreen(navController: NavController) {
    var searchText by remember { mutableStateOf("") }
    var responseData by remember { mutableStateOf(emptyList<Name>()) }
    var filteredData by remember { mutableStateOf(emptyList<Name>()) }

    LaunchedEffect(Unit) {
        responseData = fetchAndParseJson()
        filteredData = responseData
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Bible Verses About",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
        )

        Text(
            text = "Select a topic to view the top-rated verses.",
            style = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = searchText,
            onValueChange = {
                // Update the search query and filter the list accordingly
                searchText = it
                filteredData = filterDataBySearchQuery(responseData, it)
            },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        // LazyColumn to display the list
        LazyColumn {
            items(filteredData) { item ->
                ListItemText(item, navController)
            }
        }
    }
}

@Composable
fun DetailScreen(slug: String, navController: NavController) {
    var responseData by remember { mutableStateOf<Topic?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) } // Default to KJV (0)

    LaunchedEffect(Unit) {
        responseData = fetchAndParseTopicJson(slug)
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        // Navigate back to the ListScreen
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }

            Text(
                text = "${responseData?.verses?.size} Bible Verses About ${responseData?.name ?: ""}",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
            )

            // TabRow for toggling between KJV and ESV
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("KJV") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("ESV") }
                )
            }
        }
        items(responseData?.verses ?: emptyList()) { verse ->
            val verseText = if (selectedTabIndex == 0) verse.kjv else verse.esv
            val htmlText = HtmlCompat.fromHtml(verseText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                        append("${verse.verse}\n")
                    }
                    val spanStyle = SpanStyle(fontSize = 18.sp)
                    pushStyle(spanStyle)
                    append(htmlText)
                    pop()
                },
                style = TextStyle(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// Define your navigation routes
object BibleVersesAboutDestinations {
    const val LIST_SCREEN = "list"
    const val DETAILS_SCREEN = "details"
}

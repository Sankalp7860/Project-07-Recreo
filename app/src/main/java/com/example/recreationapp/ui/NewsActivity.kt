package com.example.recreationapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NewsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BooksApp()
                }
            }
        }
    }
}

@Composable
fun BooksApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.BooksList) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }

    when (val screen = currentScreen) {
        is Screen.BooksList -> {
            BooksScreen(
                onBookSelected = { bookId ->
                    selectedBookId = bookId
                    currentScreen = Screen.BookDetail
                }
            )
        }
        is Screen.BookDetail -> {
            selectedBookId?.let { bookId ->
                BookDetailScreen(
                    bookId = bookId,
                    onBackPressed = {
                        currentScreen = Screen.BooksList
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object BooksList : Screen()
    object BookDetail : Screen()
}

@Serializable
data class BookResponse(
    val items: List<BookItem> = emptyList(),
    val totalItems: Int = 0
)

@Serializable
data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val title: String,
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val imageLinks: ImageLinks? = null,
    val publishedDate: String? = null,
    val categories: List<String> = emptyList(),
    val averageRating: Float? = null,
    val pageCount: Int? = null,
    val previewLink: String? = null, // Added for "Read Sample"
    val infoLink: String? = null     // Added for "Buy Book"
)

@Serializable
data class ImageLinks(
    val thumbnail: String? = null,
    val smallThumbnail: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(onBookSelected: (String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var books by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Fiction", "Fantasy", "Science", "Biography", "History", "Romance")
    val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchBooks(query: String = "", category: String = "All"): List<BookItem> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val apiKey = "AIzaSyCKpLO5dKPwx2h7p7KQb7cxhYu7jitxcBM"
                val searchTerm = when {
                    query.isNotEmpty() -> query
                    category != "All" -> category.lowercase()
                    else -> "popular books"
                }
                val url = "https://www.googleapis.com/books/v1/volumes?q=$searchTerm&maxResults=40&key=$apiKey"
                Log.d("BooksScreen", "Fetching books from: $url")
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body?.string() ?: ""
                Log.d("BooksScreen", "Response code: $responseCode, Body length: ${responseBody.length}")

                if (response.isSuccessful && responseBody.isNotEmpty()) {
                    val bookResponse = json.decodeFromString<BookResponse>(responseBody)
                    Log.d("BooksScreen", "Parsed ${bookResponse.items.size} books: ${bookResponse.items.map { it.volumeInfo.title }}")
                    bookResponse.items
                } else {
                    Log.e("BooksScreen", "Failed response: Code $responseCode, Body: $responseBody")
                    emptyList()
                }
            } catch (e: IOException) {
                Log.e("BooksScreen", "Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                emptyList()
            } catch (e: Exception) {
                Log.e("BooksScreen", "Parsing or other error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                emptyList()
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            books = fetchBooks()
            isLoading = false
            Log.d("BooksScreen", "Initial fetch complete, books size: ${books.size}")
        }
    }

    LaunchedEffect(selectedCategory) {
        coroutineScope.launch {
            if (selectedCategory != "All") {
                books = fetchBooks(category = selectedCategory)
            } else {
                books = fetchBooks()
            }
            isLoading = false
            Log.d("BooksScreen", "Category fetch complete, books size: ${books.size}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Books") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Search books...") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotEmpty()) {
                            coroutineScope.launch {
                                isLoading = true
                                books = fetchBooks(query = searchQuery)
                                isLoading = false
                                Log.d("BooksScreen", "Search fetch complete, books size: ${books.size}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (books.isEmpty()) {
                    Text(
                        text = "No books found. Check logs or try a different search.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(books) { book ->
                            BookCard(book) {
                                onBookSelected(book.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(book: BookItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")
                    ?: "https://via.placeholder.com/128x192?text=No+Cover"

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Book cover for ${book.volumeInfo.title}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                book.volumeInfo.averageRating?.let { rating ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "★ $rating",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = book.volumeInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.volumeInfo.authors.isNotEmpty()) {
                    Text(
                        text = book.volumeInfo.authors.first(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: String, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var book by remember { mutableStateOf<BookItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchBookDetails(): BookItem? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val apiKey = "AIzaSyCKpLO5dKPwx2h7p7KQb7cxhYu7jitxcBM"
                val url = "https://www.googleapis.com/books/v1/volumes/$bookId?key=$apiKey"
                Log.d("BookDetailScreen", "Fetching book details from: $url")
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                Log.d("BookDetailScreen", "Response code: ${response.code}, Body length: ${responseBody.length}")

                if (response.isSuccessful && responseBody.isNotEmpty()) {
                    val bookItem = json.decodeFromString<BookItem>(responseBody)
                    Log.d("BookDetailScreen", "Parsed book: ${bookItem.volumeInfo.title}")
                    bookItem
                } else {
                    Log.e("BookDetailScreen", "Failed response: ${response.code}")
                    null
                }
            } catch (e: IOException) {
                Log.e("BookDetailScreen", "Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                null
            } catch (e: Exception) {
                Log.e("BookDetailScreen", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                null
            }
        }
    }

    LaunchedEffect(bookId) {
        if (bookId.isNotEmpty()) {
            book = fetchBookDetails()
            isLoading = false
            Log.d("BookDetailScreen", "Fetch complete, book: ${book?.volumeInfo?.title}")
        } else {
            errorMessage = "Invalid book ID"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = book?.volumeInfo?.title ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { /* Share functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                book = fetchBookDetails()
                                isLoading = false
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                book != null -> {
                    BookDetailContent(book!!)
                }
                else -> {
                    Text(
                        text = "No book data available.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun BookDetailContent(book: BookItem) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        BookHeaderSection(book)
        BookActionButtons(
            onReadSample = {
                book.volumeInfo.previewLink?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                } ?: Toast.makeText(context, "No preview available", Toast.LENGTH_SHORT).show()
            },
            onBuyBook = {
                book.volumeInfo.infoLink?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                } ?: Toast.makeText(context, "No purchase link available", Toast.LENGTH_SHORT).show()
            }
        )
        BookDescriptionSection(book)
        BookDetailsSection(book)
        SimilarBooksSection()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BookHeaderSection(book: BookItem) {
    val info = book.volumeInfo

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 220.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val imageUrl = info.imageLinks?.thumbnail?.replace("http:", "https:")
                    ?: "https://via.placeholder.com/128x192?text=No+Cover"

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Book cover for ${info.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                info.averageRating?.let { rating ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "★ $rating",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = info.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (info.authors.isNotEmpty()) {
                Text(
                    text = info.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BookActionButtons(onReadSample: () -> Unit, onBuyBook: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onReadSample,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Read Sample")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onBuyBook,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Buy Book")
        }
    }
}

@Composable
fun BookDescriptionSection(book: BookItem) {
    val info = book.volumeInfo
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "About This Book",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (info.description != null) {
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis
                )

                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) "Show Less" else "Show More")
                }
            } else {
                Text(
                    text = "No description available for this book.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun BookDetailsSection(book: BookItem) {
    val info = book.volumeInfo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Book Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            info.publishedDate?.let {
                DetailItem(label = "Published", value = it)
            }

            info.pageCount?.let {
                DetailItem(label = "Pages", value = it.toString())
            }

            if (info.categories.isNotEmpty()) {
                DetailItem(label = "Categories", value = info.categories.joinToString(", "))
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SimilarBooksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "You May Also Like",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Similar books would appear here based on this book's genre and author.",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
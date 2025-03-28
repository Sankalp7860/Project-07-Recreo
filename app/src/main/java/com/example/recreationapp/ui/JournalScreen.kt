package com.example.recreationapp.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class JournalEntry(
    val id: String,
    val user_id: String, // Now a text field in Supabase
    val title: String,
    val content: String,
    val timestamp: Long // Matches INT8 (bigint) in Supabase
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewJournalScreen(viewModel: AppViewModel) {
    var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedEntry == null) {
            JournalOverviewScreen(
                snackbarHostState = snackbarHostState,
                onEntrySelected = { entry -> selectedEntry = entry },
                onNewEntry = { selectedEntry = JournalEntry("", "", "", "", System.currentTimeMillis()) },
                viewModel = viewModel
            )
        } else {
            JournalEntryScreen(
                entry = selectedEntry!!,
                onBack = { selectedEntry = null },
                snackbarHostState = snackbarHostState,
                viewModel = viewModel,
                onEntrySaved = { selectedEntry = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalOverviewScreen(
    snackbarHostState: SnackbarHostState,
    onEntrySelected: (JournalEntry) -> Unit,
    onNewEntry: () -> Unit,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    val scope = rememberCoroutineScope()
    val userId = viewModel.user.value?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            journalEntries = loadJournalEntries(userId)
            isLoading = false
        }
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this journal entry?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // Delete entry using OkHttp
                                deleteJournalEntry(entryToDelete!!.id)
                                snackbarHostState.showSnackbar("Entry deleted successfully")
                                journalEntries = loadJournalEntries(userId)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to delete entry: ${e.message}")
                            }
                            entryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Journal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewEntry,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "New Entry", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (journalEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Book,
                            null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No journal entries yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "Tap the + button to write your first entry",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(journalEntries) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onEntrySelected(entry) },
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(entry.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    entry: JournalEntry,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: AppViewModel,
    onEntrySaved: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(entry.title) }
    var content by remember { mutableStateOf(entry.content) }
    val scope = rememberCoroutineScope()
    val isNewEntry = entry.id.isEmpty()
    val userId = viewModel.user.value?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewEntry) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                scope.launch {
                                    try {
                                        if (isNewEntry) {
                                            // Insert new entry using OkHttp
                                            insertJournalEntry(
                                                userId = userId,
                                                title = title,
                                                content = content,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            viewModel.user.value?.uid?.let { uid ->
                                                viewModel.addActivity(
                                                    uid,
                                                    "Journal",
                                                    "Created a new journal entry"
                                                )
                                            }
                                            snackbarHostState.showSnackbar("Entry saved successfully!")
                                        } else {
                                            // Update existing entry using OkHttp
                                            updateJournalEntry(
                                                id = entry.id,
                                                title = title,
                                                content = content,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            viewModel.user.value?.uid?.let { uid ->
                                                viewModel.addActivity(
                                                    uid,
                                                    "Journal",
                                                    "Updated a journal entry"
                                                )
                                            }
                                            snackbarHostState.showSnackbar("Entry updated successfully!")
                                        }
                                        onEntrySaved()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to save entry: ${e.message}")
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Title and content cannot be empty")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Save, "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (content.isEmpty()) {
                                Text(
                                    "Write your thoughts here...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            if (!isNewEntry) {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val creationDate = dateFormat.format(Date(entry.timestamp))
                Text(
                    text = "Created: $creationDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// Constants for Supabase API
private const val SUPABASE_URL = "https://ysavghvmswenmddlnshr.supabase.co/rest/v1"
private const val SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlzYXZnaHZtc3dlbm1kZGxuc2hyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI5OTY4MzIsImV4cCI6MjA1ODU3MjgzMn0.GCQ0xl7wJKI_YB8d3PP1jBDcs-aRJLRLjk9-NdB1_bs"

// Load journal entries using OkHttp
private suspend fun loadJournalEntries(userId: String): List<JournalEntry> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$SUPABASE_URL/journal_entries?user_id=eq.$userId")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Failed to load entries: ${response.code}")

            val json = response.body?.string() ?: return@withContext emptyList()
            val jsonArray = JSONArray(json)
            val entries = mutableListOf<JournalEntry>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                entries.add(
                    JournalEntry(
                        id = jsonObject.getString("id"),
                        user_id = jsonObject.getString("user_id"),
                        title = jsonObject.getString("title"),
                        content = jsonObject.getString("content"),
                        timestamp = jsonObject.getLong("timestamp")
                    )
                )
            }
            entries.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// Insert a new journal entry using OkHttp
private suspend fun insertJournalEntry(userId: String, title: String, content: String, timestamp: Long) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("title", title)
            put("content", content)
            put("timestamp", timestamp)
        }
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$SUPABASE_URL/journal_entries")
            .header("apikey", SUPABASE_API_KEY)
            .header("Authorization", "Bearer $SUPABASE_API_KEY")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Failed to insert entry: ${response.code}")
    }
}

// Update an existing journal entry using OkHttp
private suspend fun updateJournalEntry(id: String, title: String, content: String, timestamp: Long) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val jsonObject = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("timestamp", timestamp)
        }
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$SUPABASE_URL/journal_entries?id=eq.$id")
            .header("apikey", SUPABASE_API_KEY)
            .header("Authorization", "Bearer $SUPABASE_API_KEY")
            .header("Content-Type", "application/json")
            .patch(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Failed to update entry: ${response.code}")
    }
}

// Delete a journal entry using OkHttp
private suspend fun deleteJournalEntry(id: String) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$SUPABASE_URL/journal_entries?id=eq.$id")
            .header("apikey", SUPABASE_API_KEY)
            .header("Authorization", "Bearer $SUPABASE_API_KEY")
            .delete()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Failed to delete entry: ${response.code}")
    }
}
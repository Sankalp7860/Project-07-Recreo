package com.example.recreationapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.recreationapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.PathMeasure
import java.io.File
import java.util.UUID

private object CustomIcons {
    val Draw = Icons.Filled.Edit
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    error = Color(0xFFB00020),
    background = Color(0xFFFFFBFE),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    outline = Color(0xFFCAC4D0)
)

data class SavedDrawing(
    val id: String,
    val user_id: String,
    val file_path: String,
    val timestamp: Long,
    val thumbnail: String
)

data class DrawingPath(
    val path: Path,
    val paint: Paint,
    val isEraser: Boolean
)

@Composable
fun DrawingAppScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    MaterialTheme(colorScheme = LightColorScheme) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DrawingsOverviewScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var savedDrawings by remember { mutableStateOf<List<SavedDrawing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var drawingToDelete by remember { mutableStateOf<SavedDrawing?>(null) }
    val scope = rememberCoroutineScope()
    val userId = viewModel.user.value?.uid ?: ""
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(userId, refreshTrigger) {
        if (userId.isNotEmpty()) {
            isLoading = true
            savedDrawings = loadSavedDrawings(userId)
            isLoading = false
        }
    }

    if (drawingToDelete != null) {
        AlertDialog(
            onDismissRequest = { drawingToDelete = null },
            title = { Text("Delete Drawing") },
            text = { Text("Are you sure you want to delete this drawing?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                deleteDrawing(drawingToDelete!!.id, drawingToDelete!!.file_path)
                                snackbarHostState.showSnackbar("Drawing deleted successfully")
                                // Force refresh the drawings list
                                savedDrawings = loadSavedDrawings(userId)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to delete drawing: ${e.message}")
                                Log.e("DrawingsOverview", "Delete failed", e)
                            } finally {
                                drawingToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { drawingToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "My Drawings",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = { refreshTrigger++ }) {
                    Icon(Icons.Filled.Refresh, "Refresh")
                }
                FloatingActionButton(
                    onClick = { navController.navigate("new_drawing") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, "Create New Drawing", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (savedDrawings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        CustomIcons.Draw,
                        null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No drawings yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "Tap the + button to create your first drawing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedDrawings) { drawing ->
                    DrawingCard(
                        drawing = drawing,
                        onDelete = { drawingToDelete = drawing },
                        onClick = {
                            val encodedPath = Uri.encode(drawing.file_path)
                            navController.navigate("view_drawing/$encodedPath")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingCard(drawing: SavedDrawing, onDelete: () -> Unit, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(drawing.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = "${drawing.thumbnail}?t=${System.currentTimeMillis()}",
                    onError = { result ->
                        Log.e("DrawingCard", "Failed to load image: ${result.result.throwable}")
                    }
                ),
                contentDescription = "Saved Drawing",
                modifier = Modifier.fillMaxSize().padding(2.dp)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, "Delete", tint = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    formattedDate,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ViewDrawingScreen(
    drawingPath: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: AppViewModel
) {
    val decodedPath = Uri.decode(drawingPath)
    var imageRefreshKey by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(
                "View Drawing",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Button(onClick = {
                val encodedPath = Uri.encode(decodedPath)
                navController.navigate("edit_drawing/$encodedPath") {
                    popUpTo("view_drawing/$encodedPath") { inclusive = true }
                    launchSingleTop = true
                }
                imageRefreshKey = System.currentTimeMillis()
            }) {
                Icon(Icons.Filled.Edit, "Edit Drawing")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).shadow(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = "$SUPABASE_STORAGE_URL/drawings/$decodedPath?t=$imageRefreshKey",
                        onSuccess = {
                            Log.d("ViewDrawing", "Image loaded successfully")
                        },
                        onError = { result ->
                            Log.e("ViewDrawing", "Image load failed: ${result.result.throwable}")
                        }
                    ),
                    contentDescription = "Drawing",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Drawing Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Created: [Timestamp not available in view mode]",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Size: [Size not available in view mode]",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    viewModel: AppViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    editMode: Boolean,
    drawingPath: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var brushSize by remember { mutableStateOf(8f) }
    var isEraserMode by remember { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf(Color.Black) }

    val paths = remember { mutableStateListOf<DrawingPath>() }
    val undoStack = remember { mutableStateListOf<DrawingPath>() }
    var currentPath by remember { mutableStateOf(Path()) }

    val decodedDrawingPath = drawingPath?.let { Uri.decode(it) }
    val userId = viewModel.user.value?.uid ?: ""

    LaunchedEffect(Unit) {
        if (editMode && decodedDrawingPath != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Drawing loaded. You can now edit it.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "Edit Drawing" else "New Drawing") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (paths.isNotEmpty() || !currentPath.isEmpty || editMode) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        Log.d("DrawingScreen", "Attempting to save drawing. Edit mode: $editMode, User ID: $userId")
                                        val filePath = if (editMode && decodedDrawingPath != null) {
                                            saveDrawingEditMode(
                                                paths = paths,
                                                currentPath = currentPath,
                                                userId = userId,
                                                originalFilePath = decodedDrawingPath
                                            )
                                        } else {
                                            saveDrawing(
                                                paths = paths,
                                                currentPath = currentPath,
                                                userId = userId
                                            )
                                        }
                                        Log.d("DrawingScreen", "Drawing saved successfully. File path: $filePath")
                                        viewModel.user.value?.uid?.let { uid ->
                                            viewModel.addActivity(uid, "Drawing", "Saved a drawing")
                                        }
                                        snackbarHostState.showSnackbar(
                                            if (editMode) "Drawing updated successfully!" else "Drawing saved successfully!"
                                        )
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        Log.e("DrawingScreen", "Failed to save drawing: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Failed to save drawing: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Save, "Save")
                        }
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).shadow(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (editMode && decodedDrawingPath != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "$SUPABASE_STORAGE_URL/drawings/$decodedDrawingPath?t=${System.currentTimeMillis()}",
                                onError = { result ->
                                    Log.e("DrawingScreen", "Failed to load background image: ${result.result.throwable}")
                                }
                            ),
                            contentDescription = "Background Drawing",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (editMode) Color.Transparent else Color.White)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPath.lineTo(change.position.x, change.position.y)
                                    },
                                    onDragEnd = {
                                        val paint = Paint().apply {
                                            color = if (isEraserMode) Color.White.toArgb() else currentColor.toArgb()
                                            strokeWidth = brushSize
                                            style = Paint.Style.STROKE
                                            strokeCap = Paint.Cap.ROUND
                                            isAntiAlias = true
                                        }
                                        paths.add(DrawingPath(currentPath, paint, isEraserMode))
                                        undoStack.clear()
                                        currentPath = Path()
                                    }
                                )
                            }
                    ) {
                        paths.forEach { drawingPath ->
                            drawPath(
                                path = drawingPath.path,
                                color = Color(drawingPath.paint.color),
                                style = Stroke(width = drawingPath.paint.strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        if (!currentPath.isEmpty) {
                            drawPath(
                                path = currentPath,
                                color = if (isEraserMode) Color.White else currentColor,
                                style = Stroke(width = brushSize, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Colors", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorButton(Color.Black, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                        ColorButton(Color.Red, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                        ColorButton(Color.Blue, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                        ColorButton(Color.Green, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                        ColorButton(Color.Yellow, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                        ColorButton(Color.Magenta, currentColor, isEraserMode) { currentColor = it; isEraserMode = false }
                    }

                    Text("Tools", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { isEraserMode = !isEraserMode }
                                .background(if (isEraserMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                "Eraser",
                                tint = if (isEraserMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Eraser",
                                color = if (isEraserMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                            Text("Brush Size: ${brushSize.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = brushSize,
                                onValueChange = { brushSize = it },
                                valueRange = 2f..30f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Button(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                undoStack.add(paths.removeAt(paths.lastIndex))
                            }
                        },
                        enabled = paths.isNotEmpty(),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Undo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Undo")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                paths.add(undoStack.removeAt(undoStack.lastIndex))
                            }
                        },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Redo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Redo")
                    }
                }

                Button(
                    onClick = {
                        paths.clear()
                        undoStack.clear()
                        currentPath = Path()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Filled.Clear, "Clear")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun ColorButton(
    color: Color,
    selectedColor: Color,
    isEraserMode: Boolean,
    onColorSelected: (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selectedColor == color && !isEraserMode) 3.dp else 1.dp,
                color = if (selectedColor == color && !isEraserMode) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .clickable { onColorSelected(color) }
    )
}

// Constants for Supabase API
private const val SUPABASE_URL = "https://ysavghvmswenmddlnshr.supabase.co/rest/v1"
private const val SUPABASE_STORAGE_URL = "https://ysavghvmswenmddlnshr.supabase.co/storage/v1/object"
private const val SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlzYXZnaHZtc3dlbm1kZGxuc2hyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI5OTY4MzIsImV4cCI6MjA1ODU3MjgzMn0.GCQ0xl7wJKI_YB8d3PP1jBDcs-aRJLRLjk9-NdB1_bs"

// Load saved drawings from Supabase
private suspend fun loadSavedDrawings(userId: String): List<SavedDrawing> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$SUPABASE_URL/drawings?user_id=eq.$userId&select=*")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .header("Cache-Control", "no-cache")
                .build()

            Log.d("DrawingScreen", "Loading drawings for user: $userId")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("DrawingScreen", "Failed to load drawings. Code: ${response.code}, Message: ${response.message}, Body: ${response.body?.string()}")
                throw Exception("Failed to load drawings: ${response.code} - ${response.message}")
            }

            val json = response.body?.string() ?: return@withContext emptyList()
            Log.d("DrawingScreen", "Loaded drawings JSON: $json")
            val jsonArray = JSONArray(json)
            val drawings = mutableListOf<SavedDrawing>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                drawings.add(
                    SavedDrawing(
                        id = jsonObject.getString("id"),
                        user_id = jsonObject.getString("user_id"),
                        file_path = jsonObject.getString("file_path"),
                        timestamp = jsonObject.getLong("timestamp"),
                        thumbnail = "$SUPABASE_STORAGE_URL/drawings/${jsonObject.getString("file_path")}"
                    )
                )
            }
            drawings.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("DrawingScreen", "Error loading drawings: ${e.message}", e)
            emptyList()
        }
    }
}

// Save a new drawing to Supabase
private suspend fun saveDrawing(
    paths: List<DrawingPath>,
    currentPath: Path,
    userId: String
): String {
    return withContext(Dispatchers.IO) {
        try {
            // Step 1: Create the bitmap
            val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)

            paths.forEach { drawingPath ->
                canvas.drawPath(drawingPath.path.asAndroidPath(), drawingPath.paint)
            }
            if (!currentPath.isEmpty) {
                val paint = Paint().apply {
                    color = paths.lastOrNull()?.paint?.color ?: Color.Black.toArgb()
                    strokeWidth = paths.lastOrNull()?.paint?.strokeWidth ?: 8f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                canvas.drawPath(currentPath.asAndroidPath(), paint)
            }

            // Step 2: Convert bitmap to byte array
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            Log.d("DrawingScreen", "Bitmap converted to byte array. Size: ${byteArray.size} bytes")

            // Step 3: Upload the drawing to Supabase Storage
            val timestamp = System.currentTimeMillis()
            val filePath = "drawing_$timestamp.png"
            val client = OkHttpClient()
            val requestBody = byteArray.toRequestBody("image/png".toMediaType())
            val uploadRequest = Request.Builder()
                .url("$SUPABASE_STORAGE_URL/drawings/$filePath")
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .header("Content-Type", "image/png")
                .header("x-upsert", "true")
                .put(requestBody)
                .build()

            Log.d("DrawingScreen", "Uploading drawing to Supabase Storage: $SUPABASE_STORAGE_URL/drawings/$filePath")
            val uploadResponse = client.newCall(uploadRequest).execute()
            if (!uploadResponse.isSuccessful) {
                val errorBody = uploadResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to upload drawing. Code: ${uploadResponse.code}, Message: ${uploadResponse.message}, Body: $errorBody")
                throw Exception("Failed to upload drawing: ${uploadResponse.code} - ${uploadResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Drawing uploaded successfully. Response: ${uploadResponse.body?.string()}")

            // Step 4: Save metadata to the drawings table
            val drawingId = UUID.randomUUID().toString()
            val jsonObject = JSONObject().apply {
                put("id", drawingId)
                put("user_id", userId)
                put("file_path", filePath)
                put("timestamp", timestamp)
            }
            val metadataRequestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
            val metadataRequest = Request.Builder()
                .url("$SUPABASE_URL/drawings")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(metadataRequestBody)
                .build()

            Log.d("DrawingScreen", "Saving drawing metadata to Supabase: $jsonObject")
            val metadataResponse = client.newCall(metadataRequest).execute()
            if (!metadataResponse.isSuccessful) {
                val errorBody = metadataResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to save drawing metadata. Code: ${metadataResponse.code}, Message: ${metadataResponse.message}, Body: $errorBody")
                deleteDrawingFromStorage(filePath)
                throw Exception("Failed to save drawing metadata: ${metadataResponse.code} - ${metadataResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Drawing metadata saved successfully. Response: ${metadataResponse.body?.string()}")

            filePath
        } catch (e: Exception) {
            Log.e("DrawingScreen", "Error saving drawing: ${e.message}", e)
            throw e
        }
    }
}

// Save an edited drawing to Supabase
private suspend fun saveDrawingEditMode(
    paths: List<DrawingPath>,
    currentPath: Path,
    userId: String,
    originalFilePath: String
): String {
    return withContext(Dispatchers.IO) {
        try {
            // Step 1: Download the original drawing from Supabase Storage
            val client = OkHttpClient()
            val downloadRequest = Request.Builder()
                .url("$SUPABASE_STORAGE_URL/drawings/$originalFilePath")
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .build()

            Log.d("DrawingScreen", "Downloading original drawing: $SUPABASE_STORAGE_URL/drawings/$originalFilePath")
            val downloadResponse = client.newCall(downloadRequest).execute()
            if (!downloadResponse.isSuccessful) {
                val errorBody = downloadResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to download original drawing. Code: ${downloadResponse.code}, Message: ${downloadResponse.message}, Body: $errorBody")
                throw Exception("Failed to download original drawing: ${downloadResponse.code} - ${downloadResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Original drawing downloaded successfully")

            val originalBitmap = BitmapFactory.decodeStream(downloadResponse.body?.byteStream())
            val bitmap = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)

            // Step 2: Apply new paths on top of the original bitmap
            val scaleX = bitmap.width.toFloat() / 1000f
            val scaleY = bitmap.height.toFloat() / 1000f

            paths.forEach { drawingPath ->
                val scaledPath = android.graphics.Path()
                val pathMeasure = PathMeasure(drawingPath.path.asAndroidPath(), false)
                val coordinates = FloatArray(2)
                var distance = 0f
                while (distance <= pathMeasure.length) {
                    pathMeasure.getPosTan(distance, coordinates, null)
                    val x = coordinates[0] * scaleX
                    val y = coordinates[1] * scaleY
                    if (distance == 0f) scaledPath.moveTo(x, y) else scaledPath.lineTo(x, y)
                    distance += 1f
                }
                canvas.drawPath(scaledPath, drawingPath.paint)
            }

            if (!currentPath.isEmpty) {
                val paint = Paint().apply {
                    color = paths.lastOrNull()?.paint?.color ?: Color.Black.toArgb()
                    strokeWidth = paths.lastOrNull()?.paint?.strokeWidth ?: 8f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                val scaledPath = android.graphics.Path()
                val pathMeasure = PathMeasure(currentPath.asAndroidPath(), false)
                val coordinates = FloatArray(2)
                var distance = 0f
                while (distance <= pathMeasure.length) {
                    pathMeasure.getPosTan(distance, coordinates, null)
                    val x = coordinates[0] * scaleX
                    val y = coordinates[1] * scaleY
                    if (distance == 0f) scaledPath.moveTo(x, y) else scaledPath.lineTo(x, y)
                    distance += 1f
                }
                canvas.drawPath(scaledPath, paint)
            }

            // Step 3: Convert updated bitmap to byte array
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            Log.d("DrawingScreen", "Updated bitmap converted to byte array. Size: ${byteArray.size} bytes")

            // Step 4: Upload the updated drawing to Supabase Storage
            val requestBody = byteArray.toRequestBody("image/png".toMediaType())
            val uploadRequest = Request.Builder()
                .url("$SUPABASE_STORAGE_URL/drawings/$originalFilePath")
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .header("Content-Type", "image/png")
                .header("x-upsert", "true")
                .put(requestBody)
                .build()

            Log.d("DrawingScreen", "Uploading updated drawing to Supabase Storage: $SUPABASE_STORAGE_URL/drawings/$originalFilePath")
            val uploadResponse = client.newCall(uploadRequest).execute()
            if (!uploadResponse.isSuccessful) {
                val errorBody = uploadResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to update drawing. Code: ${uploadResponse.code}, Message: ${uploadResponse.message}, Body: $errorBody")
                throw Exception("Failed to update drawing: ${uploadResponse.code} - ${uploadResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Updated drawing uploaded successfully. Response: ${uploadResponse.body?.string()}")

            // Step 5: Update the timestamp in the drawings table
            val jsonObject = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
            }
            val metadataRequestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
            val metadataRequest = Request.Builder()
                .url("$SUPABASE_URL/drawings?file_path=eq.$originalFilePath")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .patch(metadataRequestBody)
                .build()

            Log.d("DrawingScreen", "Updating drawing metadata: $jsonObject")
            val metadataResponse = client.newCall(metadataRequest).execute()
            if (!metadataResponse.isSuccessful) {
                val errorBody = metadataResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to update drawing metadata. Code: ${metadataResponse.code}, Message: ${metadataResponse.message}, Body: $errorBody")
                throw Exception("Failed to update drawing metadata: ${metadataResponse.code} - ${metadataResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Drawing metadata updated successfully. Response: ${metadataResponse.body?.string()}")

            originalFilePath
        } catch (e: Exception) {
            Log.e("DrawingScreen", "Error updating drawing: ${e.message}", e)
            throw e
        }
    }
}

// Delete a drawing from Supabase Storage and database
private suspend fun deleteDrawing(drawingId: String, filePath: String) {
    return withContext(Dispatchers.IO) {
        try {
            // First delete from storage
            deleteDrawingFromStorage(filePath)

            // Then delete from database
            val client = OkHttpClient()
            val deleteRequest = Request.Builder()
                .url("$SUPABASE_URL/drawings?id=eq.$drawingId")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .delete()
                .build()

            Log.d("DrawingScreen", "Deleting drawing metadata: id=$drawingId")
            val deleteResponse = client.newCall(deleteRequest).execute()

            if (!deleteResponse.isSuccessful) {
                val errorBody = deleteResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to delete drawing metadata. Code: ${deleteResponse.code}, Message: ${deleteResponse.message}, Body: $errorBody")
                throw Exception("Failed to delete drawing metadata: ${deleteResponse.code} - ${deleteResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Drawing metadata deleted successfully")
        } catch (e: Exception) {
            Log.e("DrawingScreen", "Error deleting drawing", e)
            throw e
        }
    }
}

private suspend fun deleteDrawingFromStorage(filePath: String) {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val deleteRequest = Request.Builder()
                .url("$SUPABASE_STORAGE_URL/drawings/$filePath")
                .header("Authorization", "Bearer $SUPABASE_API_KEY")
                .delete()
                .build()

            Log.d("DrawingScreen", "Deleting drawing from storage: $filePath")
            val deleteResponse = client.newCall(deleteRequest).execute()

            if (!deleteResponse.isSuccessful) {
                val errorBody = deleteResponse.body?.string() ?: "No response body"
                Log.e("DrawingScreen", "Failed to delete drawing from storage. Code: ${deleteResponse.code}, Message: ${deleteResponse.message}, Body: $errorBody")
                throw Exception("Failed to delete drawing from storage: ${deleteResponse.code} - ${deleteResponse.message} - $errorBody")
            }
            Log.d("DrawingScreen", "Drawing deleted from storage successfully")
        } catch (e: Exception) {
            Log.e("DrawingScreen", "Error deleting drawing from storage", e)
            throw e
        }
    }
}

fun Bitmap.toImageBitmap(): ImageBitmap {
    return this.asImageBitmap()
}
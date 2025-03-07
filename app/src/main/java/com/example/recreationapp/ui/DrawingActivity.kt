package com.example.recreationapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.recreationapp.viewmodel.AppViewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    error = Color(0xFFB00020),
    background = Color(0xFFFFFBFE),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

data class SavedDrawing(
    val path: String,
    val timestamp: Long,
    val thumbnail: String
)

class DrawingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = LightColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawingScreenWrapper()
                }
            }
        }
    }
}

@Composable
fun DrawingScreenWrapper(viewModel: AppViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        DrawingScreen(
            viewModel = viewModel,
            padding = padding,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun DrawingScreen(
    viewModel: AppViewModel,
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState
) {
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var savedDrawings by remember { mutableStateOf<List<SavedDrawing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    // Load saved drawings when the screen is first displayed
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            savedDrawings = loadSavedDrawings(context)
            Log.d("DrawingApp", "Loaded ${savedDrawings.size} drawings")
        } catch (e: Exception) {
            Log.e("DrawingApp", "Error loading drawings", e)
            snackbarHostState.showSnackbar("Failed to load drawings: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Show full-screen image dialog if an image is selected
    if (selectedImagePath != null) {
        FullScreenImageDialog(
            imagePath = selectedImagePath!!,
            onDismiss = { selectedImagePath = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        // Drawing Canvas Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Drawing Canvas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath.lineTo(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    paths.add(currentPath to currentColor)
                                }
                            )
                        }
                ) {
                    paths.forEach { (path, color) ->
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 8f)
                        )
                    }
                    drawPath(
                        path = currentPath,
                        color = currentColor,
                        style = Stroke(width = 8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Selection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorButton(Color.Black, currentColor) { currentColor = it }
                ColorButton(Color.Red, currentColor) { currentColor = it }
                ColorButton(Color.Blue, currentColor) { currentColor = it }
                ColorButton(Color.Green, currentColor) { currentColor = it }
                ColorButton(Color.Yellow, currentColor) { currentColor = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    paths.clear()
                    currentPath = Path()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (paths.isEmpty()) {
                                snackbarHostState.showSnackbar("Cannot save empty drawing")
                                return@launch
                            }

                            val path = saveDrawing(paths, context)
                            savedDrawings = loadSavedDrawings(context)
                            viewModel.user.value?.uid?.let { uid ->
                                viewModel.addActivity(uid, "Drawing", "Saved a drawing")
                            }
                            snackbarHostState.showSnackbar("Drawing saved successfully!")
                        } catch (e: Exception) {
                            Log.e("DrawingApp", "Error in save handler", e)
                            snackbarHostState.showSnackbar("Failed to save drawing: ${e.message}")
                        }
                    }
                },
                enabled = paths.isNotEmpty()
            ) {
                Text("Save Drawing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Previous Drawings Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Previous Drawings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (savedDrawings.isEmpty()) {
                    Text(
                        "No saved drawings yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedDrawings.reversed()) { drawing ->
                            Card(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clickable {
                                        selectedImagePath = drawing.path
                                    }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = File(drawing.path),
                                        onError = {
                                            Log.e("DrawingApp", "Error loading image: ${drawing.path}", it.result.throwable)
                                        }
                                    ),
                                    contentDescription = "Saved Drawing",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = File(imagePath)),
                contentDescription = "Full Screen Drawing",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ColorButton(
    color: Color,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(
                width = if (selectedColor == color) 3.dp else 1.dp,
                color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onColorSelected(color) }
    )
}

private fun saveDrawing(paths: List<Pair<Path, Color>>, context: Context): String {
    return try {
        val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        paths.forEach { (path, color) ->
            val paint = Paint().apply {
                this.color = color.toArgb()
                strokeWidth = 8f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawPath(path.asAndroidPath(), paint)
        }

        val directory = File(context.getExternalFilesDir(null), "drawings")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val file = File(directory, "drawing_${timestamp}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        Log.d("DrawingApp", "Drawing saved successfully at: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        Log.e("DrawingApp", "Error saving drawing", e)
        throw e
    }
}

private fun loadSavedDrawings(context: Context): List<SavedDrawing> {
    return try {
        val directory = File(context.getExternalFilesDir(null), "drawings")
        if (!directory.exists()) {
            Log.d("DrawingApp", "Drawings directory doesn't exist")
            return emptyList()
        }

        directory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".png", ignoreCase = true) }
            ?.map { file ->
                SavedDrawing(
                    path = file.absolutePath,
                    timestamp = file.lastModified(),
                    thumbnail = file.absolutePath
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    } catch (e: Exception) {
        Log.e("DrawingApp", "Error loading saved drawings", e)
        emptyList()
    }
}
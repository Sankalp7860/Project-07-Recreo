package com.example.recreationapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.recreationapp.viewmodel.AppViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.PathMeasure
import kotlinx.coroutines.launch

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
    val path: String,
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
    content: @Composable () -> Unit // Add content parameter
) {
    val snackbarHostState = remember { SnackbarHostState() }

    MaterialTheme(colorScheme = LightColorScheme) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content() // Render the content provided by the NavHost
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

    LaunchedEffect(Unit) {
        isLoading = true
        savedDrawings = loadSavedDrawings(context)
        isLoading = false
    }

    if (drawingToDelete != null) {
        AlertDialog(
            onDismissRequest = { drawingToDelete = null },
            title = { Text("Delete Drawing") },
            text = { Text("Are you sure you want to delete this drawing?") },
            confirmButton = {
                Button(
                    onClick = {
                        val file = File(drawingToDelete!!.path)
                        if (file.delete()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Drawing deleted successfully")
                                savedDrawings = loadSavedDrawings(context)
                            }
                        }
                        drawingToDelete = null
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
            FloatingActionButton(
                onClick = { navController.navigate("new_drawing") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Create New Drawing", tint = MaterialTheme.colorScheme.onPrimary)
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
                            val encodedPath = Uri.encode(drawing.path)
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
                painter = rememberAsyncImagePainter(model = File(drawing.path)),
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
                navController.navigate("edit_drawing/$encodedPath")
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
                    painter = rememberAsyncImagePainter(model = File(decodedPath)),
                    contentDescription = "Drawing",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val file = File(decodedPath)
        if (file.exists()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                    val creationDate = dateFormat.format(Date(file.lastModified()))

                    Text(
                        "Drawing Details",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Created: $creationDate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val fileSizeKB = file.length() / 1024
                    Text(
                        "Size: $fileSizeKB KB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

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

    LaunchedEffect(Unit) {
        if (editMode && decodedDrawingPath != null) {
            try {
                val file = File(decodedDrawingPath)
                if (file.exists()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Drawing loaded. You can now edit it.")
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error loading drawing: ${e.message}")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text(
                if (editMode) "Edit Drawing" else "New Drawing",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).shadow(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (editMode && decodedDrawingPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(decodedDrawingPath)),
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

            Row {
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
                Spacer(modifier = Modifier.width(8.dp))
                if (paths.isNotEmpty() || !currentPath.isEmpty || editMode) {
                    Button(
                        onClick = {
                            scope.launch {
                                val savePath = if (editMode && decodedDrawingPath != null) {
                                    saveDrawingEditMode(paths, currentPath, context, decodedDrawingPath)
                                } else {
                                    saveDrawing(paths, currentPath, context)
                                }
                                viewModel.user.value?.uid?.let { uid ->
                                    viewModel.addActivity(uid, "Drawing", "Saved a drawing")
                                }
                                snackbarHostState.showSnackbar(
                                    if (editMode) "Drawing updated successfully!" else "Drawing saved successfully!"
                                )
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.Save, "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
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

private fun saveDrawingEditMode(
    paths: List<DrawingPath>,
    currentPath: Path,
    context: Context,
    originalPath: String
): String {
    val originalBitmap = BitmapFactory.decodeFile(originalPath)
    val bitmap = Bitmap.createBitmap(
        originalBitmap.width,
        originalBitmap.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawBitmap(originalBitmap, 0f, 0f, null)

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

    FileOutputStream(originalPath).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return originalPath
}

private fun saveDrawing(
    paths: List<DrawingPath>,
    currentPath: Path,
    context: Context
): String {
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

    val directory = File(context.getExternalFilesDir(null), "drawings")
    if (!directory.exists()) directory.mkdirs()

    val timestamp = System.currentTimeMillis()
    val file = File(directory, "drawing_$timestamp.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file.absolutePath
}

private fun loadSavedDrawings(context: Context): List<SavedDrawing> {
    val directory = File(context.getExternalFilesDir(null), "drawings")
    if (!directory.exists()) return emptyList()

    return directory.listFiles()
        ?.filter { it.isFile && it.name.endsWith(".png") }
        ?.map { file ->
            SavedDrawing(
                path = file.absolutePath,
                timestamp = file.lastModified(),
                thumbnail = file.absolutePath
            )
        }
        ?.sortedByDescending { it.timestamp }
        ?: emptyList()
}

fun Bitmap.toImageBitmap(): ImageBitmap {
    return this.asImageBitmap()
}
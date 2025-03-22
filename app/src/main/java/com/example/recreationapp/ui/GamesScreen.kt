package com.example.recreationapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel
import kotlin.math.max
import kotlin.math.min

enum class GameType {
    TicTacToe,
    Minesweeper,
    Sudoku
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(viewModel: AppViewModel = viewModel()) { // Renamed from JournalScreen to GamesScreen
    var selectedGame by remember { mutableStateOf<GameType?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = selectedGame == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                Text(
                    "Choose a game to play",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                GameCard(
                    title = "Tic-tac-toe",
                    description = "Classic X and O game",
                    onClick = { selectedGame = GameType.TicTacToe }
                )
                GameCard(
                    title = "Minesweeper",
                    description = "Uncover tiles, avoid mines!",
                    onClick = { selectedGame = GameType.Minesweeper }
                )
                GameCard(
                    title = "Sudoku",
                    description = "Solve the number puzzle!",
                    onClick = { selectedGame = GameType.Sudoku }
                )
            }
        }

        AnimatedVisibility(
            visible = selectedGame != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedGame) {
                    GameType.TicTacToe -> TicTacToeGame { selectedGame = null }
                    GameType.Minesweeper -> MinesweeperGame { selectedGame = null }
                    GameType.Sudoku -> SudokuGame { selectedGame = null }
                    null -> {}
                }
            }
        }
    }
}

@Composable
fun GameCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Play",
                modifier = Modifier.rotate(180f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGame(onBack: () -> Unit) {
    val gameState = remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var gameOver by remember { mutableStateOf(false) }

    fun checkWinner(board: List<String>): String? {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // diagonals
        )

        for (pattern in winPatterns) {
            val (a, b, c) = pattern
            if (board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]) {
                return board[a]
            }
        }

        if (board.none { it.isEmpty() }) {
            return "Draw"
        }

        return null
    }

    fun resetGame() {
        gameState.value = List(9) { "" }
        currentPlayer = "X"
        winner = null
        gameOver = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic-tac-toe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset Game")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (winner == null) "Current Player: $currentPlayer"
                else if (winner == "Draw") "Game Draw!"
                else "Winner: $winner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    winner == "X" -> Color(0xFF0D47A1)
                    winner == "O" -> Color(0xFFB71C1C)
                    winner == "Draw" -> Color.Gray
                    currentPlayer == "X" -> Color(0xFF1976D2)
                    else -> Color(0xFFE53935)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.9f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(9) { index ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                .clickable(
                                    enabled = gameState.value[index].isEmpty() && winner == null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (gameState.value[index].isEmpty() && winner == null) {
                                        val newBoard = gameState.value.toMutableList()
                                        newBoard[index] = currentPlayer
                                        gameState.value = newBoard

                                        winner = checkWinner(newBoard)
                                        if (winner == null) {
                                            currentPlayer = if (currentPlayer == "X") "O" else "X"
                                        } else {
                                            gameOver = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gameState.value[index],
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (gameState.value[index] == "X")
                                    Color(0xFF1976D2) else Color(0xFFE53935)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("New Game")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinesweeperGame(onBack: () -> Unit) {
    val gridSize = 10
    val mineCount = 15
    val gameState = remember {
        mutableStateOf(generateMinefield(gridSize, mineCount))
    }
    var revealedCells by remember { mutableStateOf(setOf<Int>()) }
    var flaggedCells by remember { mutableStateOf(setOf<Int>()) }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    fun resetGame() {
        gameState.value = generateMinefield(gridSize, mineCount)
        revealedCells = emptySet()
        flaggedCells = emptySet()
        gameOver = false
        won = false
    }

    fun revealCell(index: Int) {
        if (gameOver || index in revealedCells || index in flaggedCells) return

        val newRevealed = revealedCells.toMutableSet()
        if (gameState.value[index] == -1) {
            gameOver = true
            newRevealed.addAll(0 until gridSize * gridSize)
        } else {
            floodFill(index, gameState.value, gridSize, newRevealed)
            val nonMineCells = gridSize * gridSize - mineCount
            if (newRevealed.size == nonMineCells) {
                gameOver = true
                won = true
            }
        }
        revealedCells = newRevealed
    }

    fun toggleFlag(index: Int) {
        if (gameOver || index in revealedCells) return
        if (index in flaggedCells) {
            flaggedCells = flaggedCells - index
        } else {
            flaggedCells = flaggedCells + index
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minesweeper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Filled.Refresh, "Reset")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    won -> "You Won!"
                    gameOver -> "Game Over!"
                    else -> "Mines: ${mineCount - flaggedCells.size}"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    won -> Color(0xFF2E7D32)
                    gameOver -> Color(0xFFB71C1C)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.9f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSize),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(gridSize * gridSize) { index ->
                        val isRevealed = index in revealedCells
                        val isFlagged = index in flaggedCells
                        val cellValue = gameState.value[index]
                        var pressStartTime by remember { mutableStateOf<Long?>(null) }
                        val longPressDuration = 500L

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                .background(
                                    if (isRevealed) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable(
                                    enabled = !gameOver && !isRevealed,
                                    interactionSource = MutableInteractionSource(),
                                    indication = null
                                ) { revealCell(index) }
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            when (event.type) {
                                                PointerEventType.Press -> {
                                                    if (!gameOver && !isRevealed) {
                                                        pressStartTime = System.currentTimeMillis()
                                                    }
                                                }
                                                PointerEventType.Release -> {
                                                    if (!gameOver && !isRevealed && pressStartTime != null) {
                                                        val pressDuration = System.currentTimeMillis() - pressStartTime!!
                                                        if (pressDuration >= longPressDuration) {
                                                            toggleFlag(index)
                                                        }
                                                        pressStartTime = null
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isFlagged -> Text("🚩", fontSize = 20.sp)
                                isRevealed && cellValue == -1 -> Text("💣", fontSize = 20.sp)
                                isRevealed && cellValue > 0 -> Text(
                                    cellValue.toString(),
                                    color = numberColor(cellValue),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("New Game")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuGame(onBack: () -> Unit) {
    val gridSize = 9
    val initialBoard = remember { mutableStateOf(generateSudoku()) }
    var userBoard by remember { mutableStateOf(initialBoard.value.map { it.toMutableList() }.toMutableList()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var gameWon by remember { mutableStateOf(false) }

    fun resetGame() {
        val newBoard = generateSudoku()
        initialBoard.value = newBoard
        userBoard = newBoard.map { it.toMutableList() }.toMutableList()
        selectedCell = null
        gameWon = false
    }

    fun checkWin(): Boolean {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (userBoard[row][col] == 0 || !isValidSudoku(userBoard, row, col, userBoard[row][col])) {
                    return false
                }
            }
        }
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sudoku") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Filled.Refresh, "Reset")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (gameWon) "You Won!" else "Fill the grid!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (gameWon) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.9f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSize),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(gridSize * gridSize) { index ->
                        val row = index / gridSize
                        val col = index % gridSize
                        val isInitial = initialBoard.value[row][col] != 0
                        val value = userBoard[row][col]
                        val isSelected = selectedCell == Pair(row, col)

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(
                                    1.dp,
                                    if (row % 3 == 0 || col % 3 == 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        isInitial -> MaterialTheme.colorScheme.surface
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable(
                                    enabled = !gameWon && !isInitial,
                                    interactionSource = MutableInteractionSource(),
                                    indication = null
                                ) { selectedCell = Pair(row, col) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (value == 0) "" else value.toString(),
                                fontSize = 20.sp,
                                fontWeight = if (isInitial) FontWeight.Bold else FontWeight.Normal,
                                color = if (isInitial) Color.Black else Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Number pad
            if (!gameWon) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(9) { num ->
                        val number = num + 1
                        Button(
                            onClick = {
                                selectedCell?.let { (row, col) ->
                                    if (initialBoard.value[row][col] == 0) {
                                        userBoard[row][col] = number
                                        if (checkWin()) gameWon = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .aspectRatio(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(number.toString(), fontSize = 20.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                selectedCell?.let { (row, col) ->
                                    if (initialBoard.value[row][col] == 0) {
                                        userBoard[row][col] = 0
                                    }
                                }
                            },
                            modifier = Modifier
                                .aspectRatio(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("X", fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("New Game")
            }
        }
    }
}

// Sudoku generation and validation functions
fun generateSudoku(): List<List<Int>> {
    val base = listOf(
        listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
        listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
        listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
        listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
        listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
        listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
        listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
        listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
        listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
    )
    return base.map { it.toMutableList() }
}

fun isValidSudoku(board: List<List<Int>>, row: Int, col: Int, num: Int): Boolean {
    // Check row
    for (x in 0 until 9) {
        if (x != col && board[row][x] == num) return false
    }
    // Check column
    for (x in 0 until 9) {
        if (x != row && board[x][col] == num) return false
    }
    // Check 3x3 box
    val startRow = row - row % 3
    val startCol = col - col % 3
    for (i in 0 until 3) {
        for (j in 0 until 3) {
            if (i + startRow != row && j + startCol != col && board[i + startRow][j + startCol] == num) {
                return false
            }
        }
    }
    return true
}

fun generateMinefield(size: Int, mineCount: Int): List<Int> {
    val totalCells = size * size
    val mines = (0 until totalCells).shuffled().take(mineCount).toSet()
    return List(totalCells) { index ->
        if (index in mines) -1
        else {
            val row = index / size
            val col = index % size
            var count = 0
            for (r in maxOf(0, row - 1)..minOf(size - 1, row + 1)) {
                for (c in maxOf(0, col - 1)..minOf(size - 1, col + 1)) {
                    if (r * size + c in mines) count++
                }
            }
            count
        }
    }
}

fun floodFill(index: Int, board: List<Int>, size: Int, revealed: MutableSet<Int>) {
    if (index !in board.indices || index in revealed) return
    revealed.add(index)
    if (board[index] != 0) return

    val row = index / size
    val col = index % size
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1, -1 to -1, -1 to 1, 1 to -1, 1 to 1)
    for ((dr, dc) in directions) {
        val newRow = row + dr
        val newCol = col + dc
        if (newRow in 0 until size && newCol in 0 until size) {
            floodFill(newRow * size + newCol, board, size, revealed)
        }
    }
}

@Composable
fun numberColor(number: Int): Color = when (number) {
    1 -> Color(0xFF1976D2)  // Blue
    2 -> Color(0xFF2E7D32)  // Green
    3 -> Color(0xFFB71C1C)  // Red
    4 -> Color(0xFF0D47A1)  // Dark Blue
    5 -> Color(0xFF4A2C2A)  // Brown
    6 -> Color(0xFF00695C)  // Teal
    7 -> Color(0xFF6A1B9A)  // Purple
    8 -> Color(0xFF424242)  // Gray
    else -> Color.Black
}
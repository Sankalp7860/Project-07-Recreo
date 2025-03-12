package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.model.ActivityRecord
import com.example.recreationapp.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AppViewModel = viewModel()) {
    val user by viewModel.user.observeAsState(initial = null)
    val activities by viewModel.activities.observeAsState(initial = emptyList())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { context.startActivity(Intent(context, MusicActivity::class.java)) }) {
            Text("Music")
        }
        Button(onClick = { context.startActivity(Intent(context, DrawingActivity::class.java)) }) {
            Text("Drawing")
        }
        Button(onClick = { context.startActivity(Intent(context, JournalActivity::class.java)) }) {
            Text("Daily Journal")
        }
        Button(onClick = { context.startActivity(Intent(context, CommunityActivity::class.java)) }) {
            Text("Community Sharing")
        }
        Button(onClick = { context.startActivity(Intent(context, NewsActivity::class.java)) }) {
            Text("Books")
        }
//        Button(onClick = { context.startActivity(Intent(context, BookActivity::class.java)) }) {
//            Text("Books")
//        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(activities) { activity ->
                ActivityItem(activity, user?.isAdmin == true, viewModel::deleteActivity)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityRecord, isAdmin: Boolean, onDelete: (String) -> Unit) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.activityType, style = MaterialTheme.typography.titleMedium)
                Text(activity.content)
            }
            if (isAdmin) {
                Text("Delete", modifier = Modifier.clickable { onDelete(activity.id) })
            }
        }
    }
}
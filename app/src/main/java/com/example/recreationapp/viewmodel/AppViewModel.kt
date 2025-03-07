package com.example.recreationapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recreationapp.model.ActivityRecord
import com.example.recreationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _activities = MutableLiveData<List<ActivityRecord>>(emptyList())
    val activities: LiveData<List<ActivityRecord>> = _activities

    init {
        auth.currentUser?.let {
            _user.value = User(it.uid, it.email ?: "", it.email == "admin@example.com")
            loadActivities(it.uid)
        }
    }

    fun login(email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    if (firebaseUser != null) {
                        val user = User(firebaseUser.uid, email, email == "admin@example.com")
                        _user.value = user
                        loadActivities(user.uid)
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
                .addOnFailureListener {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val firebaseUser = result.user
                            if (firebaseUser != null) {
                                val user = User(firebaseUser.uid, email, email == "admin@example.com")
                                _user.value = user
                                loadActivities(user.uid)
                                onComplete(true)
                            }
                        }
                        .addOnFailureListener { onComplete(false) }
                }
        }
    }

    fun addActivity(userId: String, activityType: String, content: String) {
        viewModelScope.launch {
            val newActivityRef = db.child("activities").push()
            val activity = ActivityRecord(newActivityRef.key ?: "", userId, activityType, content)
            newActivityRef.setValue(activity)
                .addOnSuccessListener { loadActivities(userId) }
        }
    }

    fun deleteActivity(id: String) {
        viewModelScope.launch {
            db.child("activities").child(id).removeValue()
                .addOnSuccessListener { user.value?.uid?.let { loadActivities(it) } }
        }
    }

    private fun loadActivities(userId: String) {
        db.child("activities")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activityList = snapshot.children.mapNotNull { it.getValue(ActivityRecord::class.java) }
                    _activities.value = activityList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }
}
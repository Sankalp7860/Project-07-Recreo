package com.example.recreationapp.viewmodel

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _activities = MutableLiveData<List<ActivityRecord>>(emptyList())
    val activities: LiveData<List<ActivityRecord>> = _activities

    companion object {
        private const val TAG = "AppViewModel"
    }

    init {
        Log.d(TAG, "AppViewModel initialized")
        auth.currentUser?.let { firebaseUser ->
            Log.d(TAG, "Initializing with current user: ${firebaseUser.uid}")
            loadUserData(firebaseUser.uid)
        }
    }

    fun login(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Login attempt with email: $email")
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val firebaseUser = result.user
                        if (firebaseUser != null) {
                            Log.d(TAG, "Login success for UID: ${firebaseUser.uid}")
                            loadUserData(firebaseUser.uid) { user ->
                                viewModelScope.launch(Dispatchers.Main) {
                                    _user.value = user
                                    loadActivities(firebaseUser.uid)
                                    onComplete(true, null)
                                }
                            }
                        } else {
                            Log.e(TAG, "Login failed: FirebaseUser is null")
                            viewModelScope.launch(Dispatchers.Main) {
                                onComplete(false, "Login failed: User not found")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Login failed: ${exception.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onComplete(false, exception.message)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception: ${e.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onComplete(false, "Unexpected error: ${e.message}")
                }
            }
        }
    }

    fun register(email: String, password: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Register attempt with email: $email, name: $name")
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val firebaseUser = result.user
                        if (firebaseUser != null) {
                            Log.d(TAG, "Register success for UID: ${firebaseUser.uid}")
                            val user = User(firebaseUser.uid, email, name, email == "admin@example.com")
                            db.child("users").child(firebaseUser.uid).setValue(user)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User data saved to Firebase: $user")
                                    viewModelScope.launch(Dispatchers.Main) {
                                        _user.value = user
                                        loadActivities(firebaseUser.uid)
                                        onComplete(true, null)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Failed to save user data: ${exception.message}")
                                    viewModelScope.launch(Dispatchers.Main) {
                                        onComplete(false, "Failed to save user data: ${exception.message}")
                                    }
                                }
                        } else {
                            Log.e(TAG, "Register failed: FirebaseUser is null")
                            viewModelScope.launch(Dispatchers.Main) {
                                onComplete(false, "Registration failed: User not found")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Register failed: ${exception.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onComplete(false, exception.message)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Register exception: ${e.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onComplete(false, "Unexpected error: ${e.message}")
                }
            }
        }
    }

    private fun loadUserData(uid: String, onUserLoaded: (User) -> Unit = {}) {
        Log.d(TAG, "Loading user data for UID: $uid")
        db.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Log.d(TAG, "User data loaded: $user")
                    viewModelScope.launch(Dispatchers.Main) {
                        _user.value = user
                    }
                    onUserLoaded(user)
                } else {
                    Log.w(TAG, "No user data found for UID: $uid")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "User data load cancelled: ${error.message}")
            }
        })
    }

    fun updateUserPreferences(activities: List<String>, onComplete: () -> Unit) {
        val currentUser = _user.value ?: return
        val updatedUser = currentUser.copy(preferredActivities = activities)
        db.child("users").child(currentUser.uid).setValue(updatedUser)
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.Main) {
                    _user.value = updatedUser
                    onComplete()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update user preferences: ${exception.message}")
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
        Log.d(TAG, "Loading activities for userId: $userId")
        db.child("activities")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activityList = snapshot.children.mapNotNull { it.getValue(ActivityRecord::class.java) }
                    viewModelScope.launch(Dispatchers.Main) {
                        _activities.value = activityList
                        Log.d(TAG, "Activities loaded: ${activityList.size}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Activities load cancelled: ${error.message}")
                }
            })
    }

    fun addGlobalContent(activityType: String, content: String) {
        viewModelScope.launch {
            if (user.value?.isAdmin == true) {
                val newContentRef = db.child("global_content").push()
                val contentRecord = ActivityRecord(newContentRef.key ?: "", "admin", activityType, content)
                newContentRef.setValue(contentRecord)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
        _activities.value = emptyList()
    }
}
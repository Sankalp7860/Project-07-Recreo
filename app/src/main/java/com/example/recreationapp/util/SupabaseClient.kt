package com.example.recreationapp.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://ysavghvmswenmddlnshr.supabase.co", // Replace with your Supabase URL
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlzYXZnaHZtc3dlbm1kZGxuc2hyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI5OTY4MzIsImV4cCI6MjA1ODU3MjgzMn0.GCQ0xl7wJKI_YB8d3PP1jBDcs-aRJLRLjk9-NdB1_bs" // Replace with your Supabase Anon Key
    ) {
        install(Postgrest) // For database operations
//        install(GoTrue) // For authentication (optional, since you're using Firebase Auth)
    }
}
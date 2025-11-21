package com.example.bunny;


enum class Sender {
    USER, TOKKI, TYPING
}

data class ChatMessage(
        val id: Long = System.currentTimeMillis(),
        val text: String,
val sender: Sender,
val timestamp: Long = System.currentTimeMillis()
)


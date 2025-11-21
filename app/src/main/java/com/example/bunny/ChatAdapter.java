package com.example.bunny;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
        private val items: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

companion object {
private const val VIEW_USER = 1
private const val VIEW_TOKKI = 2
private const val VIEW_TYPING = 3
    }

override fun getItemViewType(position: Int): Int {
    return when (items[position].sender) {
        Sender.USER -> VIEW_USER
        Sender.TOKKI -> VIEW_TOKKI
        Sender.TYPING -> VIEW_TYPING
    }
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
        VIEW_USER -> {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        }
        VIEW_TOKKI -> {
            val view = inflater.inflate(R.layout.item_message_tokki, parent, false)
            TokkiViewHolder(view)
        }
            else -> {
            val view = inflater.inflate(R.layout.layout_typing_indicator, parent, false)
            TypingViewHolder(view)
        }
    }
}

override fun getItemCount(): Int = items.size

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val msg = items[position]
    when (holder) {
        is UserViewHolder -> holder.bind(msg)
        is TokkiViewHolder -> holder.bind(msg)
        is TypingViewHolder -> { /* no dynamic text needed */ }
    }
}

fun addMessage(message: ChatMessage) {
    items.add(message)
    notifyItemInserted(items.size - 1)
}

fun removeTypingIndicator() {
    val index = items.indexOfFirst { it.sender == Sender.TYPING }
    if (index != -1) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }
}

fun showTypingIndicator() {
    // avoid duplicates
    if (items.any { it.sender == Sender.TYPING }) return
            items.add(ChatMessage(text = "", sender = Sender.TYPING))
    notifyItemInserted(items.size - 1)
}

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvText: TextView = itemView.findViewById(R.id.tvText)
    private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

    fun bind(message: ChatMessage) {
        tvText.text = message.text
        tvTime.text = formatTime(message.timestamp)
    }
}

class TokkiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
    private val tvText: TextView = itemView.findViewById(R.id.tvText)
    private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

    fun bind(message: ChatMessage) {
        tvSender.text = "Tokki"
        tvText.text = message.text
        tvTime.text = formatTime(message.timestamp)
    }
}

class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}


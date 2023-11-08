data class ChatMessage(val id: Long, val text: String, val isSent: Boolean) {
    val isSentByMe: Boolean
        get() = isSent
}

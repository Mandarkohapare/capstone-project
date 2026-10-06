package com.example.model

enum class TicketStatus(val key: String, val label: String) {
    NEW("new", "New"),
    DRAFT_READY("draft_ready", "Draft Ready"),
    ESCALATED("escalated", "Escalated"),
    RESOLVED("resolved", "Resolved"),
    SENT("sent", "Sent");

    companion object {
        fun fromKey(key: String?): TicketStatus = entries.find { it.key.equals(key, ignoreCase = true) } ?: NEW
    }
}

enum class TicketPriority(val key: String, val label: String) {
    LOW("low", "Low"),
    MEDIUM("medium", "Medium"),
    HIGH("high", "High"),
    URGENT("urgent", "Urgent");

    companion object {
        fun fromKey(key: String?): TicketPriority = entries.find { it.key.equals(key, ignoreCase = true) } ?: MEDIUM
    }
}

enum class TicketChannel(val key: String, val label: String) {
    EMAIL("email", "Email"),
    CHAT("chat", "Chat"),
    WEB("web", "Web"),
    SLACK("slack", "Slack"),
    PHONE("phone", "Phone");

    companion object {
        fun fromKey(key: String?): TicketChannel = entries.find { it.key.equals(key, ignoreCase = true) } ?: EMAIL
    }
}

enum class DraftStatus(val key: String, val label: String) {
    NONE("none", "None"),
    READY("ready", "Draft Ready"),
    REVIEWING("reviewing", "In Review"),
    APPROVED("approved", "Approved");

    companion object {
        fun fromKey(key: String?): DraftStatus = entries.find { it.key.equals(key, ignoreCase = true) } ?: NONE
    }
}

enum class ResponseSource(val key: String, val label: String) {
    AI_AUTO("ai_auto", "AI Auto"),
    AGENT_REVIEWED("agent_reviewed", "Agent Reviewed"),
    MANUAL("manual", "Manual");

    companion object {
        fun fromKey(key: String?): ResponseSource = entries.find { it.key.equals(key, ignoreCase = true) } ?: MANUAL
    }
}

data class Ticket(
    val id: String,
    val subject: String,
    val body: String,
    val customerName: String,
    val customerEmail: String,
    val category: String,
    val channel: TicketChannel,
    val priority: TicketPriority,
    val status: TicketStatus,
    val aiDraft: String? = null,
    val aiDraftStatus: DraftStatus = DraftStatus.NONE,
    val responseSource: ResponseSource = ResponseSource.MANUAL,
    val aiConfidence: Float = 0f,
    val aiSentiment: String = "Neutral",
    val aiReasoning: String? = null,
    val escalated: Boolean = false,
    val escalationReason: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val resolvedDate: Long? = null
)

data class TicketTimelineItem(
    val id: Long = 0,
    val ticketId: String,
    val action: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

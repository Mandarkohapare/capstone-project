package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DraftStatus
import com.example.model.ResponseSource
import com.example.model.Ticket
import com.example.model.TicketChannel
import com.example.model.TicketPriority
import com.example.model.TicketStatus

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val body: String,
    val customerName: String,
    val customerEmail: String,
    val category: String,
    val channel: String,
    val priority: String,
    val status: String,
    val aiDraft: String?,
    val aiDraftStatus: String,
    val responseSource: String,
    val aiConfidence: Float,
    val aiSentiment: String,
    val aiReasoning: String?,
    val escalated: Boolean,
    val escalationReason: String?,
    val createdDate: Long,
    val resolvedDate: Long?
) {
    fun toDomain(): Ticket = Ticket(
        id = id,
        subject = subject,
        body = body,
        customerName = customerName,
        customerEmail = customerEmail,
        category = category,
        channel = TicketChannel.fromKey(channel),
        priority = TicketPriority.fromKey(priority),
        status = TicketStatus.fromKey(status),
        aiDraft = aiDraft,
        aiDraftStatus = DraftStatus.fromKey(aiDraftStatus),
        responseSource = ResponseSource.fromKey(responseSource),
        aiConfidence = aiConfidence,
        aiSentiment = aiSentiment,
        aiReasoning = aiReasoning,
        escalated = escalated,
        escalationReason = escalationReason,
        createdDate = createdDate,
        resolvedDate = resolvedDate
    )

    companion object {
        fun fromDomain(ticket: Ticket): TicketEntity = TicketEntity(
            id = ticket.id,
            subject = ticket.subject,
            body = ticket.body,
            customerName = ticket.customerName,
            customerEmail = ticket.customerEmail,
            category = ticket.category,
            channel = ticket.channel.key,
            priority = ticket.priority.key,
            status = ticket.status.key,
            aiDraft = ticket.aiDraft,
            aiDraftStatus = ticket.aiDraftStatus.key,
            responseSource = ticket.responseSource.key,
            aiConfidence = ticket.aiConfidence,
            aiSentiment = ticket.aiSentiment,
            aiReasoning = ticket.aiReasoning,
            escalated = ticket.escalated,
            escalationReason = ticket.escalationReason,
            createdDate = ticket.createdDate,
            resolvedDate = ticket.resolvedDate
        )
    }
}

@Entity(tableName = "ticket_timeline")
data class TicketTimelineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: String,
    val action: String,
    val detail: String,
    val timestamp: Long
)

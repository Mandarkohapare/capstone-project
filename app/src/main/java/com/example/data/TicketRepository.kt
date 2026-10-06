package com.example.data

import com.example.ai.TicketAiEngine
import com.example.model.DraftStatus
import com.example.model.ResponseSource
import com.example.model.Ticket
import com.example.model.TicketChannel
import com.example.model.TicketPriority
import com.example.model.TicketStatus
import com.example.model.TicketTimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TicketRepository(private val dao: TicketDao) {

    val ticketsFlow: Flow<List<Ticket>> = dao.getAllTicketsFlow().map { list ->
        list.map { it.toDomain() }
    }

    fun getTicketFlow(id: String): Flow<Ticket?> = dao.getTicketByIdFlow(id).map { it?.toDomain() }

    fun getTimelineFlow(ticketId: String): Flow<List<TicketTimelineItem>> =
        dao.getTimelineForTicketFlow(ticketId).map { list ->
            list.map {
                TicketTimelineItem(
                    id = it.id,
                    ticketId = it.ticketId,
                    action = it.action,
                    detail = it.detail,
                    timestamp = it.timestamp
                )
            }
        }

    suspend fun runAiPipeline(ticketId: String, tone: TicketAiEngine.Tone = TicketAiEngine.Tone.PROFESSIONAL): Result<Ticket> {
        val existing = dao.getTicketById(ticketId)?.toDomain()
            ?: return Result.failure(IllegalArgumentException("Ticket $ticketId not found"))

        val processed = TicketAiEngine.processTicket(existing, tone)
        dao.updateTicket(TicketEntity.fromDomain(processed))

        dao.insertTimelineItem(
            TicketTimelineEntity(
                ticketId = ticketId,
                action = "AI Pipeline Executed",
                detail = "Draft generated with ${(processed.aiConfidence * 100).toInt()}% confidence (${tone.label} tone)",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(processed)
    }

    suspend fun approveAndSend(ticketId: String, finalResponse: String) {
        val existing = dao.getTicketById(ticketId)?.toDomain() ?: return
        val updated = existing.copy(
            aiDraft = finalResponse,
            status = TicketStatus.SENT,
            aiDraftStatus = DraftStatus.APPROVED,
            responseSource = ResponseSource.AGENT_REVIEWED,
            resolvedDate = System.currentTimeMillis()
        )
        dao.updateTicket(TicketEntity.fromDomain(updated))

        dao.insertTimelineItem(
            TicketTimelineEntity(
                ticketId = ticketId,
                action = "Response Sent",
                detail = "Agent reviewed, approved, and dispatched response to ${existing.customerEmail}",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun escalateTicket(ticketId: String, reason: String) {
        val existing = dao.getTicketById(ticketId)?.toDomain() ?: return
        val updated = existing.copy(
            status = TicketStatus.ESCALATED,
            escalated = true,
            escalationReason = reason.ifBlank { "Manually escalated by support agent." }
        )
        dao.updateTicket(TicketEntity.fromDomain(updated))

        dao.insertTimelineItem(
            TicketTimelineEntity(
                ticketId = ticketId,
                action = "Ticket Escalated",
                detail = reason.ifBlank { "Transferred to tier-2 human specialist." },
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateStatus(ticketId: String, newStatus: TicketStatus) {
        val existing = dao.getTicketById(ticketId)?.toDomain() ?: return
        val updated = existing.copy(
            status = newStatus,
            resolvedDate = if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.SENT) System.currentTimeMillis() else existing.resolvedDate
        )
        dao.updateTicket(TicketEntity.fromDomain(updated))

        dao.insertTimelineItem(
            TicketTimelineEntity(
                ticketId = ticketId,
                action = "Status Changed",
                detail = "Status set to ${newStatus.label}",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateDraft(ticketId: String, newDraft: String) {
        val existing = dao.getTicketById(ticketId)?.toDomain() ?: return
        val updated = existing.copy(aiDraft = newDraft)
        dao.updateTicket(TicketEntity.fromDomain(updated))
    }

    suspend fun createTicket(
        subject: String,
        body: String,
        customerName: String,
        customerEmail: String,
        channel: TicketChannel,
        priority: TicketPriority,
        category: String,
        runAiImmediately: Boolean
    ): String {
        val id = "tkt-" + UUID.randomUUID().toString().take(6)
        val initialTicket = Ticket(
            id = id,
            subject = subject,
            body = body,
            customerName = customerName,
            customerEmail = customerEmail,
            category = category.ifBlank { "general" },
            channel = channel,
            priority = priority,
            status = TicketStatus.NEW,
            aiDraft = null,
            aiDraftStatus = DraftStatus.NONE,
            responseSource = ResponseSource.MANUAL,
            createdDate = System.currentTimeMillis()
        )

        dao.insertTicket(TicketEntity.fromDomain(initialTicket))
        dao.insertTimelineItem(
            TicketTimelineEntity(
                ticketId = id,
                action = "Ticket Ingested",
                detail = "Created via ${channel.label} from $customerName",
                timestamp = System.currentTimeMillis()
            )
        )

        if (runAiImmediately) {
            runAiPipeline(id)
        }

        return id
    }

    suspend fun deleteTicket(id: String) {
        dao.deleteTicket(id)
    }

    suspend fun resetWithSeedData() {
        val seedEntities = SeedData.getInitialTickets().map { TicketEntity.fromDomain(it) }
        dao.insertTickets(seedEntities)
    }
}

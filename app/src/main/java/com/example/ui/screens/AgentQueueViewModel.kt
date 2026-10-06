package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.TicketAiEngine
import com.example.data.TicketRepository
import com.example.model.Ticket
import com.example.model.TicketChannel
import com.example.model.TicketPriority
import com.example.model.TicketStatus
import com.example.model.TicketTimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentQueueViewModel(private val repository: TicketRepository) : ViewModel() {

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _filter = MutableStateFlow("all")
    val filter: StateFlow<String> = _filter.asStateFlow()

    private val _viewMode = MutableStateFlow("table") // "table" or "board"
    val viewMode: StateFlow<String> = _viewMode.asStateFlow()

    private val _processingId = MutableStateFlow<String?>(null)
    val processingId: StateFlow<String?> = _processingId.asStateFlow()

    private val _selectedTicketId = MutableStateFlow<String?>(null)
    val selectedTicketId: StateFlow<String?> = _selectedTicketId.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    val allTickets: StateFlow<List<Ticket>> = repository.ticketsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTickets: StateFlow<List<Ticket>> = combine(
        allTickets,
        _search,
        _filter
    ) { tickets, query, filterKey ->
        tickets.filter { t ->
            when (filterKey) {
                "all" -> true
                "resolved" -> t.status == TicketStatus.RESOLVED || t.status == TicketStatus.SENT
                "escalated" -> t.escalated || t.status == TicketStatus.ESCALATED
                "draft_ready" -> t.status == TicketStatus.DRAFT_READY
                "new" -> t.status == TicketStatus.NEW
                else -> t.status.key == filterKey
            }
        }.filter { t ->
            if (query.isBlank()) true
            else {
                val q = query.lowercase().trim()
                t.subject.lowercase().contains(q) ||
                t.customerName.lowercase().contains(q) ||
                t.category.lowercase().contains(q) ||
                t.body.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTicket(id: String): Flow<Ticket?> = repository.getTicketFlow(id)

    fun getTimeline(id: String): Flow<List<TicketTimelineItem>> = repository.getTimelineFlow(id)

    fun setSearch(query: String) {
        _search.value = query
    }

    fun setFilter(filterKey: String) {
        _filter.value = filterKey
    }

    fun setViewMode(mode: String) {
        _viewMode.value = mode
    }

    fun selectTicket(id: String?) {
        _selectedTicketId.value = id
    }

    fun setShowCreateDialog(show: Boolean) {
        _showCreateDialog.value = show
    }

    fun runAiPipeline(ticketId: String, tone: TicketAiEngine.Tone = TicketAiEngine.Tone.PROFESSIONAL) {
        viewModelScope.launch {
            _processingId.value = ticketId
            try {
                repository.runAiPipeline(ticketId, tone)
            } finally {
                _processingId.value = null
            }
        }
    }

    fun runAiOnAllNew() {
        viewModelScope.launch {
            val newTickets = allTickets.value.filter { it.status == TicketStatus.NEW }
            for (t in newTickets) {
                _processingId.value = t.id
                repository.runAiPipeline(t.id)
            }
            _processingId.value = null
        }
    }

    fun approveAndSend(ticketId: String, finalResponse: String) {
        viewModelScope.launch {
            repository.approveAndSend(ticketId, finalResponse)
        }
    }

    fun escalateTicket(ticketId: String, reason: String) {
        viewModelScope.launch {
            repository.escalateTicket(ticketId, reason)
        }
    }

    fun saveDraft(ticketId: String, draft: String) {
        viewModelScope.launch {
            repository.updateDraft(ticketId, draft)
        }
    }

    fun createTicket(
        subject: String,
        body: String,
        customerName: String,
        customerEmail: String,
        channel: TicketChannel,
        priority: TicketPriority,
        category: String,
        runAiImmediately: Boolean
    ) {
        viewModelScope.launch {
            val newId = repository.createTicket(
                subject, body, customerName, customerEmail, channel, priority, category, runAiImmediately
            )
            if (!runAiImmediately) {
                _filter.value = "new"
            }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            repository.deleteTicket(ticketId)
            if (_selectedTicketId.value == ticketId) {
                _selectedTicketId.value = null
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetWithSeedData()
        }
    }

    class Factory(private val repository: TicketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AgentQueueViewModel(repository) as T
        }
    }
}

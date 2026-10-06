package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ticket
import com.example.model.TicketStatus
import com.example.ui.components.CreateTicketDialog
import com.example.ui.components.KanbanBoardView
import com.example.ui.components.QueueStatsHeader
import com.example.ui.components.TicketCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FILTER_ITEMS = listOf(
    Pair("all", "All"),
    Pair("draft_ready", "Draft ready"),
    Pair("escalated", "Escalated"),
    Pair("new", "New"),
    Pair("resolved", "Resolved")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentQueueScreen(
    viewModel: AgentQueueViewModel,
    onNavigateToTicket: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTickets by viewModel.allTickets.collectAsState()
    val filteredTickets by viewModel.filteredTickets.collectAsState()
    val search by viewModel.search.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val processingId by viewModel.processingId.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val draftReadyCount = allTickets.count { it.status == TicketStatus.DRAFT_READY }
    val escalatedCount = allTickets.count { it.status == TicketStatus.ESCALATED || it.escalated }
    val newCount = allTickets.count { it.status == TicketStatus.NEW }
    val resolvedCount = allTickets.count { it.status == TicketStatus.RESOLVED || it.status == TicketStatus.SENT }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreateDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_ticket_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Simulate Inbound Ticket")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Agent Queue",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${allTickets.size} tickets",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6D28D9)
                                    )
                                }
                            }
                            Text(
                                text = "Review AI drafts, handle escalations, and send responses.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // View mode switcher & Refresh
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // View Switcher Box (Table / Board)
                            Box(
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (viewMode == "table") MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { viewModel.setViewMode("table") }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .testTag("table_view_toggle")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FormatListBulleted,
                                            contentDescription = "Table view",
                                            tint = if (viewMode == "table") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (viewMode == "board") MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { viewModel.setViewMode("board") }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .testTag("board_view_toggle")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = "Board view",
                                            tint = if (viewMode == "board") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Refresh button
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        delay(500)
                                        isRefreshing = false
                                    }
                                },
                                modifier = Modifier.testTag("refresh_queue_btn")
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats Bar
            QueueStatsHeader(
                totalCount = allTickets.size,
                draftReadyCount = draftReadyCount,
                escalatedCount = escalatedCount,
                newCount = newCount,
                resolvedCount = resolvedCount,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Search subject, customer, category…", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }

            // Filter Chips (shown when in list/table mode or header)
            if (viewMode == "table") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(FILTER_ITEMS) { (key, label) ->
                        val isSelected = currentFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setFilter(key) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_chip_$key")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Batch AI Banner if new tickets exist
            if (newCount > 0 && currentFilter == "new") {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$newCount inbound ticket(s) awaiting AI triage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.runAiOnAllNew() },
                            modifier = Modifier.testTag("run_ai_batch_btn")
                        ) {
                            Text("Process All", fontSize = 11.sp)
                        }
                    }
                }
            }

            // View Mode Rendering
            if (viewMode == "board") {
                KanbanBoardView(
                    tickets = filteredTickets,
                    processingId = processingId,
                    onRunAi = { id -> viewModel.runAiPipeline(id) },
                    onReviewTicket = { id -> onNavigateToTicket(id) }
                )
            } else {
                // Table / List View
                if (filteredTickets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No tickets match this filter.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = {
                                viewModel.setFilter("all")
                                viewModel.setSearch("")
                            }) {
                                Text("Reset Filters", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTickets, key = { it.id }) { tkt ->
                            TicketCard(
                                ticket = tkt,
                                isProcessing = processingId == tkt.id,
                                onRunAi = { viewModel.runAiPipeline(tkt.id) },
                                onReview = { onNavigateToTicket(tkt.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { viewModel.setShowCreateDialog(false) },
            onSubmit = { subject, body, name, email, channel, priority, category, runAi ->
                viewModel.createTicket(subject, body, name, email, channel, priority, category, runAi)
            }
        )
    }
}

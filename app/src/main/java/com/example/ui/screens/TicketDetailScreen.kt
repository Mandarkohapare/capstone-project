package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ai.TicketAiEngine
import com.example.model.Ticket
import com.example.model.TicketStatus
import com.example.model.TicketTimelineItem
import com.example.ui.components.ChannelBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TicketDetailScreen(
    ticketFlow: Flow<Ticket?>,
    timelineFlow: Flow<List<TicketTimelineItem>>,
    onBack: () -> Unit,
    onApproveAndSend: (ticketId: String, draft: String) -> Unit,
    onEscalate: (ticketId: String, reason: String) -> Unit,
    onSaveDraft: (ticketId: String, draft: String) -> Unit,
    onReRunAi: (ticketId: String, tone: TicketAiEngine.Tone) -> Unit,
    onDeleteTicket: (ticketId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val ticket by ticketFlow.collectAsState(initial = null)
    val timeline by timelineFlow.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var draftContent by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(TicketAiEngine.Tone.PROFESSIONAL) }
    var isRegenerating by remember { mutableStateOf(false) }
    var showEscalateDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Sync draftContent when ticket changes
    LaunchedEffect(ticket?.id, ticket?.aiDraft) {
        ticket?.aiDraft?.let {
            draftContent = it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ticket?.id ?: "Ticket Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Agent Review Console",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Queue"
                        )
                    }
                },
                actions = {
                    ticket?.let { tkt ->
                        StatusBadge(status = tkt.status)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Ticket", color = Color(0xFFDC2626)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteTicket(tkt.id)
                                    onBack()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        val currentTicket = ticket
        if (currentTicket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Information Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentTicket.customerName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentTicket.customerName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = currentTicket.customerEmail,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            ChannelBadge(channel = currentTicket.channel)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PriorityBadge(priority = currentTicket.priority)
                            BadgePill(label = "Category: ${currentTicket.category.replaceFirstChar { it.uppercase() }}")
                            BadgePill(
                                label = "Received: " + SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(
                                    Date(currentTicket.createdDate)
                                )
                            )
                        }
                    }
                }

                // Customer Message Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer Inquiry",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentTicket.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = currentTicket.body,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // AI Analysis & Reasoning Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F3FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Intent & Context Analysis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5B21B6)
                                )
                            }
                            if (currentTicket.aiConfidence > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFC4B5FD), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${(currentTicket.aiConfidence * 100).toInt()}% confidence",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6D28D9)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sentiment & Category Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sentimentColor = when (currentTicket.aiSentiment.lowercase()) {
                                "urgent" -> Color(0xFFDC2626)
                                "frustrated" -> Color(0xFFEA580C)
                                "positive" -> Color(0xFF059669)
                                else -> Color(0xFF475569)
                            }
                            Box(
                                modifier = Modifier
                                    .background(sentimentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Sentiment: ${currentTicket.aiSentiment}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = sentimentColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEDE9FE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Intent: ${currentTicket.category.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6D28D9)
                                )
                            }
                        }

                        if (!currentTicket.aiReasoning.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Policy References & Reasoning:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5B21B6)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentTicket.aiReasoning ?: "",
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFF4C1D95)
                            )
                        }
                    }
                }

                // Escalation Box if escalated
                if (currentTicket.escalated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Escalated Ticket",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentTicket.escalationReason ?: "Handled by specialist tier.",
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                // AI Response Draft Card (Interactive Review & Send)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "AI Response Draft",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Review, customize tone, or edit before sending",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Tone Regenerate button
                            OutlinedButton(
                                onClick = {
                                    isRegenerating = true
                                    onReRunAi(currentTicket.id, selectedTone)
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(1000)
                                        isRegenerating = false
                                        snackbarHostState.showSnackbar("AI Draft refreshed in ${selectedTone.label} tone")
                                    }
                                },
                                enabled = !isRegenerating,
                                modifier = Modifier.testTag("regenerate_draft_btn")
                            ) {
                                if (isRegenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Re-draft", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tone Selector Chips
                        Text(
                            text = "Draft Tone:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TicketAiEngine.Tone.entries.forEach { tone ->
                                val isSelected = selectedTone == tone
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            selectedTone = tone
                                            onReRunAi(currentTicket.id, tone)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = tone.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Editable Draft Area
                        OutlinedTextField(
                            value = draftContent,
                            onValueChange = {
                                draftContent = it
                                onSaveDraft(currentTicket.id, it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("draft_response_input"),
                            minLines = 6,
                            maxLines = 14,
                            placeholder = { Text("AI response draft will appear here...") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Approve & Send, Escalate, Save
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Escalate Button
                            OutlinedButton(
                                onClick = { showEscalateDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("escalate_ticket_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escalate", fontSize = 12.sp)
                            }

                            // Approve & Send Button
                            Button(
                                onClick = {
                                    onApproveAndSend(currentTicket.id, draftContent)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Response approved & sent to ${currentTicket.customerName}!")
                                    }
                                    onBack()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("approve_send_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve & Send", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Audit History Timeline
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audit & Timeline Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (timeline.isEmpty()) {
                            Text(
                                text = "No timeline records yet.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            timeline.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.action,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = item.detail,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Escalation Modal Dialog
    if (showEscalateDialog) {
        var reasonText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showEscalateDialog = false },
            title = {
                Text("Escalate Ticket to Human Specialist")
            },
            text = {
                Column {
                    Text(
                        text = "Specify reason or routing instructions for the escalation team:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        placeholder = { Text("e.g., Enterprise SLA violation review, legal dispute, complex bug...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("escalation_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ticketId = ticket?.id ?: return@Button
                        onEscalate(ticketId, reasonText)
                        showEscalateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_escalate_btn")
                ) {
                    Text("Confirm Escalation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEscalateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BadgePill(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

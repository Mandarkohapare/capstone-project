package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ticket
import com.example.model.TicketStatus

data class KanbanColumnDef(
    val status: TicketStatus,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
)

private val columns = listOf(
    KanbanColumnDef(TicketStatus.NEW, "New", Icons.Default.FiberNew, Color(0xFF2563EB)),
    KanbanColumnDef(TicketStatus.DRAFT_READY, "Draft Ready", Icons.Default.AutoAwesome, Color(0xFF7C3AED)),
    KanbanColumnDef(TicketStatus.ESCALATED, "Escalated", Icons.Default.Warning, Color(0xFFDC2626)),
    KanbanColumnDef(TicketStatus.RESOLVED, "Resolved", Icons.Default.Check, Color(0xFF059669))
)

@Composable
fun KanbanBoardView(
    tickets: List<Ticket>,
    processingId: String?,
    onRunAi: (String) -> Unit,
    onReviewTicket: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColumnIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Column Tabs for quick switching on mobile
        ScrollableTabRow(
            selectedTabIndex = selectedColumnIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            columns.forEachIndexed { index, col ->
                val count = tickets.count { t ->
                    if (col.status == TicketStatus.RESOLVED) {
                        t.status == TicketStatus.RESOLVED || t.status == TicketStatus.SENT
                    } else if (col.status == TicketStatus.ESCALATED) {
                        t.status == TicketStatus.ESCALATED || t.escalated
                    } else {
                        t.status == col.status
                    }
                }
                Tab(
                    selected = selectedColumnIndex == index,
                    onClick = { selectedColumnIndex = index },
                    modifier = Modifier.testTag("kanban_tab_${col.title.lowercase()}"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = col.icon,
                                contentDescription = null,
                                tint = if (selectedColumnIndex == index) col.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = col.title,
                                fontWeight = if (selectedColumnIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedColumnIndex == index) col.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(col.accentColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = col.accentColor
                                )
                            }
                        }
                    }
                )
            }
        }

        val activeCol = columns[selectedColumnIndex]
        val columnTickets = tickets.filter { t ->
            if (activeCol.status == TicketStatus.RESOLVED) {
                t.status == TicketStatus.RESOLVED || t.status == TicketStatus.SENT
            } else if (activeCol.status == TicketStatus.ESCALATED) {
                t.status == TicketStatus.ESCALATED || t.escalated
            } else {
                t.status == activeCol.status
            }
        }

        if (columnTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(activeCol.accentColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = activeCol.icon,
                            contentDescription = null,
                            tint = activeCol.accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tickets in ${activeCol.title}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tickets matching this status will appear in this board column.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(columnTickets, key = { it.id }) { tkt ->
                    KanbanCard(
                        ticket = tkt,
                        isProcessing = processingId == tkt.id,
                        accentColor = activeCol.accentColor,
                        onRunAi = { onRunAi(tkt.id) },
                        onReview = { onReviewTicket(tkt.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanCard(
    ticket: Ticket,
    isProcessing: Boolean,
    accentColor: Color,
    onRunAi: () -> Unit,
    onReview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onReview)
            .padding(14.dp)
            .testTag("kanban_card_${ticket.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChannelBadge(channel = ticket.channel)
                    Spacer(modifier = Modifier.width(6.dp))
                    PriorityBadge(priority = ticket.priority)
                }

                if (ticket.aiConfidence > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF5F3FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${(ticket.aiConfidence * 100).toInt()}% conf",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.subject,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ticket.body,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.customerName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (ticket.status == TicketStatus.NEW) {
                    OutlinedButton(
                        onClick = onRunAi,
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("kanban_run_ai_${ticket.id}")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run AI", fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onReview,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.testTag("kanban_review_${ticket.id}")
                    ) {
                        Text("Review", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TicketChannel
import com.example.model.TicketPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        subject: String,
        body: String,
        customerName: String,
        customerEmail: String,
        channel: TicketChannel,
        priority: TicketPriority,
        category: String,
        runAiImmediately: Boolean
    ) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("technical") }
    var channel by remember { mutableStateOf(TicketChannel.EMAIL) }
    var priority by remember { mutableStateOf(TicketPriority.HIGH) }
    var runAiImmediately by remember { mutableStateOf(true) }

    var channelMenuExpanded by remember { mutableStateOf(false) }
    var priorityMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Simulate Inbound Ticket",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Test queue ingestion & AI drafting pipeline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Templates
                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(label = "Billing Issue") {
                        subject = "Overcharged for add-on seats"
                        body = "Our invoice from yesterday includes charges for 10 extra user seats that we downgraded last week. Please credit back the difference."
                        customerName = "Rachel Green"
                        customerEmail = "rachel@monica-inc.com"
                        category = "billing"
                        channel = TicketChannel.EMAIL
                        priority = TicketPriority.HIGH
                    }
                    PresetChip(label = "API 500 Outage") {
                        subject = "Production webhook endpoint returning 500 errors"
                        body = "All webhook deliveries to our production server started failing 20 minutes ago. Payload signature validation fails."
                        customerName = "Liam O'Connor"
                        customerEmail = "liam@finops.io"
                        category = "technical"
                        channel = TicketChannel.SLACK
                        priority = TicketPriority.URGENT
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_subject_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message / Description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_body_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_name_input")
                    )
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_email_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Channel and Priority selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Channel Exposed Dropdown
                    ExposedDropdownMenuBox(
                        expanded = channelMenuExpanded,
                        onExpandedChange = { channelMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = channel.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Channel") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = channelMenuExpanded,
                            onDismissRequest = { channelMenuExpanded = false }
                        ) {
                            TicketChannel.entries.forEach { ch ->
                                DropdownMenuItem(
                                    text = { Text(ch.label) },
                                    onClick = {
                                        channel = ch
                                        channelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Exposed Dropdown
                    ExposedDropdownMenuBox(
                        expanded = priorityMenuExpanded,
                        onExpandedChange = { priorityMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = priority.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = priorityMenuExpanded,
                            onDismissRequest = { priorityMenuExpanded = false }
                        ) {
                            TicketPriority.entries.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.label) },
                                    onClick = {
                                        priority = p
                                        priorityMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto run AI pipeline toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { runAiImmediately = !runAiImmediately }
                ) {
                    Checkbox(
                        checked = runAiImmediately,
                        onCheckedChange = { runAiImmediately = it },
                        modifier = Modifier.testTag("create_run_ai_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run AI pipeline immediately",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Auto-classify sentiment and generate draft",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit button
                Button(
                    onClick = {
                        val sub = subject.ifBlank { "Support Inquiry: General" }
                        val bod = body.ifBlank { "Hello, I need assistance with our account settings." }
                        val name = customerName.ifBlank { "Alex Morgan" }
                        val email = customerEmail.ifBlank { "alex.m@clientdomain.com" }

                        onSubmit(sub, bod, name, email, channel, priority, category, runAiImmediately)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_create_ticket_btn")
                ) {
                    Text("Ingest Ticket into Queue")
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

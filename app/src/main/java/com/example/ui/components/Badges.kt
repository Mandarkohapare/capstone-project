package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DraftStatus
import com.example.model.ResponseSource
import com.example.model.TicketChannel
import com.example.model.TicketPriority
import com.example.model.TicketStatus
import com.example.ui.theme.ChannelChat
import com.example.ui.theme.ChannelEmail
import com.example.ui.theme.ChannelPhone
import com.example.ui.theme.ChannelSlack
import com.example.ui.theme.ChannelWeb
import com.example.ui.theme.PriorityHighBg
import com.example.ui.theme.PriorityHighText
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityLowText
import com.example.ui.theme.PriorityMediumBg
import com.example.ui.theme.PriorityMediumText
import com.example.ui.theme.PriorityUrgentBg
import com.example.ui.theme.PriorityUrgentText
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusDraftText
import com.example.ui.theme.StatusEscalatedBg
import com.example.ui.theme.StatusEscalatedText
import com.example.ui.theme.StatusNewBg
import com.example.ui.theme.StatusNewText
import com.example.ui.theme.StatusResolvedBg
import com.example.ui.theme.StatusResolvedText

@Composable
fun StatusBadge(status: TicketStatus, modifier: Modifier = Modifier) {
    val (bg, text, border) = when (status) {
        TicketStatus.NEW -> Triple(StatusNewBg, StatusNewText, StatusNewText.copy(alpha = 0.3f))
        TicketStatus.DRAFT_READY -> Triple(StatusDraftBg, StatusDraftText, StatusDraftText.copy(alpha = 0.3f))
        TicketStatus.ESCALATED -> Triple(StatusEscalatedBg, StatusEscalatedText, StatusEscalatedText.copy(alpha = 0.3f))
        TicketStatus.RESOLVED, TicketStatus.SENT -> Triple(StatusResolvedBg, StatusResolvedText, StatusResolvedText.copy(alpha = 0.3f))
    }

    BadgeContainer(
        bgColor = bg,
        borderColor = border,
        modifier = modifier
    ) {
        if (status == TicketStatus.DRAFT_READY) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = text,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        } else if (status == TicketStatus.ESCALATED) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = text,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = status.label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: TicketPriority, modifier: Modifier = Modifier) {
    val (bg, text) = when (priority) {
        TicketPriority.URGENT -> Pair(PriorityUrgentBg, PriorityUrgentText)
        TicketPriority.HIGH -> Pair(PriorityHighBg, PriorityHighText)
        TicketPriority.MEDIUM -> Pair(PriorityMediumBg, PriorityMediumText)
        TicketPriority.LOW -> Pair(PriorityLowBg, PriorityLowText)
    }

    BadgeContainer(bgColor = bg, borderColor = text.copy(alpha = 0.25f), modifier = modifier) {
        if (priority == TicketPriority.URGENT || priority == TicketPriority.HIGH) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = text,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = priority.label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChannelBadge(channel: TicketChannel, modifier: Modifier = Modifier) {
    val (color, icon) = when (channel) {
        TicketChannel.EMAIL -> Pair(ChannelEmail, Icons.Default.Email)
        TicketChannel.CHAT -> Pair(ChannelChat, Icons.Default.ChatBubbleOutline)
        TicketChannel.WEB -> Pair(ChannelWeb, Icons.Default.Language)
        TicketChannel.SLACK -> Pair(ChannelSlack, Icons.Default.Tag)
        TicketChannel.PHONE -> Pair(ChannelPhone, Icons.Default.Phone)
    }

    BadgeContainer(
        bgColor = color.copy(alpha = 0.12f),
        borderColor = color.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = channel.label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = channel.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DraftBadge(status: DraftStatus, modifier: Modifier = Modifier) {
    val (bg, text) = when (status) {
        DraftStatus.READY -> Pair(StatusDraftBg, StatusDraftText)
        DraftStatus.REVIEWING -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        DraftStatus.APPROVED -> Pair(StatusResolvedBg, StatusResolvedText)
        DraftStatus.NONE -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
    }

    BadgeContainer(bgColor = bg, borderColor = text.copy(alpha = 0.25f), modifier = modifier) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = text,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = status.label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SourceBadge(source: ResponseSource, modifier: Modifier = Modifier) {
    val (bg, text) = when (source) {
        ResponseSource.AI_AUTO -> Pair(StatusDraftBg, StatusDraftText)
        ResponseSource.AGENT_REVIEWED -> Pair(StatusResolvedBg, StatusResolvedText)
        ResponseSource.MANUAL -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    BadgeContainer(bgColor = bg, borderColor = text.copy(alpha = 0.2f), modifier = modifier) {
        Text(
            text = source.label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BadgeContainer(
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(0.8.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.TicketRepository
import com.example.ui.screens.AgentQueueScreen
import com.example.ui.screens.AgentQueueViewModel
import com.example.ui.screens.TicketDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TicketRepository(database.ticketDao())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AgentQueueViewModel = viewModel(
                        factory = AgentQueueViewModel.Factory(repository)
                    )

                    val selectedTicketId by viewModel.selectedTicketId.collectAsState()

                    if (selectedTicketId != null) {
                        val ticketId = selectedTicketId!!
                        TicketDetailScreen(
                            ticketFlow = viewModel.getTicket(ticketId),
                            timelineFlow = viewModel.getTimeline(ticketId),
                            onBack = { viewModel.selectTicket(null) },
                            onApproveAndSend = { id, draft -> viewModel.approveAndSend(id, draft) },
                            onEscalate = { id, reason -> viewModel.escalateTicket(id, reason) },
                            onSaveDraft = { id, draft -> viewModel.saveDraft(id, draft) },
                            onReRunAi = { id, tone -> viewModel.runAiPipeline(id, tone) },
                            onDeleteTicket = { id -> viewModel.deleteTicket(id) }
                        )
                    } else {
                        AgentQueueScreen(
                            viewModel = viewModel,
                            onNavigateToTicket = { id -> viewModel.selectTicket(id) }
                        )
                    }
                }
            }
        }
    }
}

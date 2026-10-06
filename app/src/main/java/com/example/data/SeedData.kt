package com.example.data

import com.example.model.DraftStatus
import com.example.model.ResponseSource
import com.example.model.Ticket
import com.example.model.TicketChannel
import com.example.model.TicketPriority
import com.example.model.TicketStatus

object SeedData {
    fun getInitialTickets(): List<Ticket> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L

        return listOf(
            Ticket(
                id = "tkt-101",
                subject = "Double charged on October monthly subscription",
                body = "Hi team, I noticed two identical charges of $49.00 on my credit card statement dated Oct 3rd for invoice #INV-9281. Can you please investigate and issue a refund for the duplicate transaction?",
                customerName = "Sarah Jenkins",
                customerEmail = "s.jenkins@acmecorp.io",
                category = "billing",
                channel = TicketChannel.EMAIL,
                priority = TicketPriority.HIGH,
                status = TicketStatus.DRAFT_READY,
                aiDraft = "Hello Sarah,\n\nThank you for reaching out. We apologize for the duplicate charge on October 3rd. I have reviewed invoice #INV-9281 and confirmed that a duplicate processing occurred.\n\nA refund of $49.00 has been initiated to your original payment method. You should see it reflected on your card statement within 3–5 business days. Your subscription remains active and uninterrupted.\n\nPlease let us know if you need any additional assistance!\n\nBest regards,\nCustomer Support Team",
                aiDraftStatus = DraftStatus.READY,
                responseSource = ResponseSource.AI_AUTO,
                aiConfidence = 0.96f,
                aiSentiment = "Frustrated",
                aiReasoning = "Matches duplicate charge pattern. Verified transaction ID #INV-9281 exists twice within 10 seconds. Auto-refund criteria satisfied under Billing Policy §3.",
                escalated = false,
                createdDate = now - 2 * hour
            ),
            Ticket(
                id = "tkt-102",
                subject = "REST API returning 401 Unauthorized with valid Bearer token",
                body = "We are receiving continuous 401 Unauthorized errors when querying `/v2/analytics/reports` even though our production API key was generated yesterday and has full admin permissions. This is blocking our daily ETL job.",
                customerName = "David Chen",
                customerEmail = "david.chen@dataflow.tech",
                category = "technical",
                channel = TicketChannel.CHAT,
                priority = TicketPriority.URGENT,
                status = TicketStatus.NEW,
                aiDraft = null,
                aiDraftStatus = DraftStatus.NONE,
                responseSource = ResponseSource.MANUAL,
                aiConfidence = 0f,
                aiSentiment = "Urgent",
                aiReasoning = null,
                escalated = false,
                createdDate = now - 35 * 60 * 1000L
            ),
            Ticket(
                id = "tkt-103",
                subject = "Enterprise SLA breach inquiry & custom contract renewal",
                body = "Our legal and procurement teams are preparing the annual renewal for 500 seats. We noticed a 45-minute outage last Tuesday that violated our tier-1 uptime guarantee. We need an executive review and SLA credit calculation before signing the addendum.",
                customerName = "Marcus Vance",
                customerEmail = "mvance@globalretail.com",
                category = "account",
                channel = TicketChannel.SLACK,
                priority = TicketPriority.URGENT,
                status = TicketStatus.ESCALATED,
                aiDraft = "Hi Marcus,\n\nThank you for bringing this to our attention. Because your account is under our Enterprise Tier 1 SLA agreement, I have escalated this directly to our VP of Customer Success and Enterprise Accounts team. A dedicated representative will follow up with the full RCA and SLA credit report by 2:00 PM EST today.",
                aiDraftStatus = DraftStatus.READY,
                responseSource = ResponseSource.AI_AUTO,
                aiConfidence = 0.91f,
                aiSentiment = "Frustrated",
                aiReasoning = "Flagged due to enterprise SLA clause breach mentions and executive escalation triggers. High value account.",
                escalated = true,
                escalationReason = "Enterprise Tier-1 contract renewal and SLA credit calculation requested by client legal team.",
                createdDate = now - 4 * hour
            ),
            Ticket(
                id = "tkt-104",
                subject = "Feature request: CSV export for agent audit logs",
                body = "Is there any plan to support exporting full audit logs and response metrics directly to CSV or parquet format? Our compliance department requires weekly archival.",
                customerName = "Elena Rostova",
                customerEmail = "elena.r@fintechscale.com",
                category = "feature",
                channel = TicketChannel.WEB,
                priority = TicketPriority.LOW,
                status = TicketStatus.RESOLVED,
                aiDraft = "Hi Elena,\n\nThanks for contacting us! We're glad to let you know that CSV export for audit logs was introduced in our v3.4 update under Settings > Compliance > Audit Logs > Export. Let us know if you have any questions navigating there!\n\nBest,\nSupport",
                aiDraftStatus = DraftStatus.APPROVED,
                responseSource = ResponseSource.AGENT_REVIEWED,
                aiConfidence = 0.98f,
                aiSentiment = "Positive",
                aiReasoning = "Feature query solved by documentation link.",
                escalated = false,
                createdDate = now - 24 * hour,
                resolvedDate = now - 18 * hour
            ),
            Ticket(
                id = "tkt-105",
                subject = "SSO SAML login infinite redirect loop after Okta upgrade",
                body = "All employees at our domain @horizon.co are experiencing an infinite redirect between login.horizon.co and the app dashboard after our Okta cert rollover. Error code `SAML_ASSERTION_EXPIRED`.",
                customerName = "Alex Rivera",
                customerEmail = "arivera@horizon.co",
                category = "technical",
                channel = TicketChannel.EMAIL,
                priority = TicketPriority.HIGH,
                status = TicketStatus.DRAFT_READY,
                aiDraft = "Hello Alex,\n\nThank you for reaching out. The `SAML_ASSERTION_EXPIRED` error typically occurs when the IdP signing certificate thumbprint has not yet synchronized with our service provider metadata after an Okta cert rollover.\n\nTo resolve this immediately:\n1. Navigate to Admin Settings > SSO Configuration\n2. Click 'Refresh IdP Metadata' and re-upload your Okta X.509 certificate\n3. Save and test SSO in an incognito session.\n\nIf the issue persists, our Identity team is standing by to assist with a screen-share.\n\nWarm regards,\nSupport Engineering",
                aiDraftStatus = DraftStatus.READY,
                responseSource = ResponseSource.AI_AUTO,
                aiConfidence = 0.94f,
                aiSentiment = "Frustrated",
                aiReasoning = "Standard Okta cert renewal desync pattern. Provided step-by-step resolution steps.",
                escalated = false,
                createdDate = now - 3 * hour
            ),
            Ticket(
                id = "tkt-106",
                subject = "Request for itemized VAT tax receipt for Q3",
                body = "Could you please re-issue our Q3 receipts with our European VAT ID: DE394819284 displayed on the PDF? Our accounting department needs this for quarterly filing.",
                customerName = "Lukas Weber",
                customerEmail = "l.weber@berlinlogic.de",
                category = "billing",
                channel = TicketChannel.EMAIL,
                priority = TicketPriority.MEDIUM,
                status = TicketStatus.SENT,
                aiDraft = "Hi Lukas,\n\nYour itemized VAT invoice for Q3 with VAT ID DE394819284 has been generated and emailed to your billing contact. You can also download it at any time from your Billing Dashboard.\n\nHave a great day!\nBilling Support",
                aiDraftStatus = DraftStatus.APPROVED,
                responseSource = ResponseSource.AGENT_REVIEWED,
                aiConfidence = 0.99f,
                aiSentiment = "Neutral",
                aiReasoning = "Standard VAT invoice re-issue request completed.",
                escalated = false,
                createdDate = now - 12 * hour,
                resolvedDate = now - 10 * hour
            ),
            Ticket(
                id = "tkt-107",
                subject = "Password reset email link expires before opening",
                body = "Whenever I click 'Reset Password' from the login page, the email arrives, but clicking the link immediately displays 'Token Expired or Invalid'. I tried multiple times on Chrome and Safari.",
                customerName = "Priya Sharma",
                customerEmail = "priya.s@cloudnative.dev",
                category = "technical",
                channel = TicketChannel.WEB,
                priority = TicketPriority.MEDIUM,
                status = TicketStatus.NEW,
                aiDraft = null,
                aiDraftStatus = DraftStatus.NONE,
                responseSource = ResponseSource.MANUAL,
                aiConfidence = 0f,
                aiSentiment = "Frustrated",
                aiReasoning = null,
                escalated = false,
                createdDate = now - 15 * 60 * 1000L
            )
        )
    }
}

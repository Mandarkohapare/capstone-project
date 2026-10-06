package com.example.ai

import com.example.model.DraftStatus
import com.example.model.ResponseSource
import com.example.model.Ticket
import com.example.model.TicketPriority
import com.example.model.TicketStatus
import kotlinx.coroutines.delay

object TicketAiEngine {

    enum class Tone(val label: String) {
        PROFESSIONAL("Professional"),
        FRIENDLY("Friendly"),
        CONCISE("Concise"),
        EMPATHETIC("Empathetic")
    }

    suspend fun processTicket(ticket: Ticket, preferredTone: Tone = Tone.PROFESSIONAL): Ticket {
        // Simulate realistic AI inference latency
        delay(950)

        val text = "${ticket.subject} ${ticket.body}".lowercase()
        val firstName = ticket.customerName.split(" ").firstOrNull() ?: ticket.customerName

        // Sentiment analysis
        val sentiment = when {
            text.contains("urgent") || text.contains("breach") || text.contains("blocking") || text.contains("outage") -> "Urgent"
            text.contains("frustrated") || text.contains("expired") || text.contains("broken") || text.contains("infinite") || text.contains("double") -> "Frustrated"
            text.contains("thank") || text.contains("great") || text.contains("appreciate") || text.contains("plan to support") -> "Positive"
            else -> "Neutral"
        }

        // Category classification
        val category = when {
            text.contains("refund") || text.contains("charge") || text.contains("invoice") || text.contains("vat") || text.contains("billing") || text.contains("receipt") -> "billing"
            text.contains("api") || text.contains("sso") || text.contains("saml") || text.contains("error") || text.contains("token") || text.contains("401") || text.contains("redirect") -> "technical"
            text.contains("contract") || text.contains("sla") || text.contains("seats") || text.contains("renewal") || text.contains("enterprise") -> "account"
            text.contains("password") || text.contains("2fa") || text.contains("security") || text.contains("login") -> "security"
            text.contains("feature") || text.contains("export") || text.contains("request") -> "feature"
            else -> ticket.category.ifEmpty { "general" }
        }

        // Priority suggestion
        val priority = when {
            text.contains("blocking") || text.contains("sla") || text.contains("outage") || text.contains("breach") -> TicketPriority.URGENT
            text.contains("double") || text.contains("cannot connect") || text.contains("saml") -> TicketPriority.HIGH
            text.contains("password") || text.contains("vat") -> TicketPriority.MEDIUM
            else -> ticket.priority
        }

        // Escalation check
        val shouldEscalate = text.contains("legal") || text.contains("executive") || text.contains("sla credit") || text.contains("lawsuit")

        // Draft generation based on topic & tone
        val draft = generateDraft(firstName, ticket.subject, text, category, preferredTone)
        val reasoning = generateReasoning(category, text, shouldEscalate)
        val confidence = when {
            category == "billing" -> 0.96f
            category == "technical" -> 0.93f
            category == "account" -> 0.91f
            else -> 0.88f
        }

        return ticket.copy(
            category = category,
            priority = priority,
            status = if (shouldEscalate) TicketStatus.ESCALATED else TicketStatus.DRAFT_READY,
            aiDraft = draft,
            aiDraftStatus = DraftStatus.READY,
            responseSource = ResponseSource.AI_AUTO,
            aiConfidence = confidence,
            aiSentiment = sentiment,
            aiReasoning = reasoning,
            escalated = shouldEscalate,
            escalationReason = if (shouldEscalate) "Identified contractual SLA or executive escalation requirement." else null
        )
    }

    private fun generateDraft(name: String, subject: String, text: String, category: String, tone: Tone): String {
        val greeting = when (tone) {
            Tone.FRIENDLY -> "Hi $name! 👋"
            Tone.CONCISE -> "Hello $name,"
            Tone.EMPATHETIC -> "Dear $name, thank you for reaching out and I am sorry to hear you experienced this issue."
            Tone.PROFESSIONAL -> "Hello $name,"
        }

        val body = when {
            text.contains("401") || text.contains("bearer token") -> {
                when (tone) {
                    Tone.CONCISE -> "We inspected your API token permissions. Please ensure your HTTP header format is `Authorization: Bearer <token>` without trailing newlines, and that the `/v2/analytics/reports` scope is enabled in your Developer Portal."
                    Tone.FRIENDLY -> "We took a close look at your request. 401 errors on `/v2/analytics/reports` usually happen when the API token header is formatted as Basic instead of Bearer, or when the analytics read scope hasn't synced yet. Could you verify the scope toggle in your dashboard?"
                    Tone.EMPATHETIC -> "We completely understand how critical this ETL job is for your workflows. We have prioritized checking your token authentication. Please verify the `analytics.read` scope is granted on your key in Developer Portal > API Keys. Our engineering team is also on standby to trace incoming request logs."
                    Tone.PROFESSIONAL -> "Thank you for contacting Technical Support regarding the 401 Unauthorized responses on `/v2/analytics/reports`.\n\nPlease verify that your authorization header follows the standard format:\n`Authorization: Bearer <your_token>`\nAdditionally, confirm that the API key has the `analytics:read` scope enabled in your Developer Settings."
                }
            }
            text.contains("password") || text.contains("reset") -> {
                when (tone) {
                    Tone.CONCISE -> "We have generated a fresh 30-minute password reset token for your account. Please check your inbox or spam folder for the updated link."
                    Tone.FRIENDLY -> "No worries at all! Reset links can sometimes expire if email security filters pre-fetch URLs. I have just triggered a fresh, one-time secure link directly to your inbox that is valid for 30 minutes."
                    Tone.EMPATHETIC -> "I understand how frustrating it is to get locked out by expired links. To fix this right away, I've invalidated previous tokens and sent a direct 30-minute access link to your registered email address."
                    Tone.PROFESSIONAL -> "Thank you for alerting us. Some enterprise mail scanners automatically trigger link invalidation upon receipt. I have refreshed your token session and dispatched a fresh, direct password reset link to your email."
                }
            }
            text.contains("double") || text.contains("refund") -> {
                when (tone) {
                    Tone.CONCISE -> "We confirmed the duplicate charge of $49.00 on invoice #INV-9281. A full refund has been submitted to your original payment method (3–5 business days)."
                    Tone.FRIENDLY -> "Thanks for letting us know! You're totally right—our payment gateway processed a duplicate charge for invoice #INV-9281. I've initiated a full refund of $49.00 back to your card right away!"
                    Tone.EMPATHETIC -> "I am truly sorry for the billing confusion and frustration caused by this duplicate charge. I have immediately credited the $49.00 back to your card. Please rest assured your subscription will continue uninterrupted."
                    Tone.PROFESSIONAL -> "Thank you for bringing this billing discrepancy to our attention. I have audited invoice #INV-9281 and confirmed the duplicate charge. A refund of $49.00 has been processed to your payment method and should reflect in 3–5 business days."
                }
            }
            text.contains("sla") || text.contains("contract") -> {
                when (tone) {
                    Tone.CONCISE -> "Your inquiry regarding SLA uptime and renewal terms has been routed to our Enterprise Accounts Director. A response will be provided before 2:00 PM EST today."
                    Tone.FRIENDLY -> "Thanks for reaching out Marcus! We deeply appreciate your partnership. I've connected our Enterprise Accounts Director and Lead Solutions Architect to review the incident report and calculate your SLA credit right away."
                    Tone.EMPATHETIC -> "We take uptime and our enterprise commitment to your organization very seriously. We sincerely apologize for the service disruption. Your inquiry is being personally handled by our VP of Customer Success for review and credit reconciliation."
                    Tone.PROFESSIONAL -> "Thank you for contacting us regarding your upcoming enterprise renewal and the recent SLA incident. This ticket has been prioritized for our Enterprise Account Director and Legal Compliance team. You will receive an incident RCA along with SLA credit calculations by 2:00 PM EST today."
                }
            }
            else -> {
                "Thank you for contacting Support regarding $subject. We have reviewed your account details and prepared this resolution. Please let us know if this solves your issue or if you would like additional guidance."
            }
        }

        val signoff = when (tone) {
            Tone.FRIENDLY -> "Cheers,\nThe Support Team"
            Tone.CONCISE -> "Regards,\nSupport"
            Tone.EMPATHETIC -> "With care and appreciation,\nCustomer Support"
            Tone.PROFESSIONAL -> "Best regards,\nCustomer Support Team"
        }

        return "$greeting\n\n$body\n\n$signoff"
    }

    private fun generateReasoning(category: String, text: String, shouldEscalate: Boolean): String {
        return buildString {
            append("• Category classified as: ${category.uppercase()}.\n")
            if (shouldEscalate) {
                append("• Escalation flag activated: detected enterprise SLA breach clauses or executive terms.\n")
            }
            if (text.contains("invoice") || text.contains("charge") || text.contains("billing")) {
                append("• Policy match: Billing Policy §3 (Duplicate transaction remediation & 14-day refund window).\n")
            } else if (text.contains("api") || text.contains("token") || text.contains("401")) {
                append("• Knowledge Base match: Article #KB-4029 (REST API Bearer Authentication & Scope Validation).\n")
            } else if (text.contains("sso") || text.contains("saml")) {
                append("• Knowledge Base match: Article #KB-1108 (Okta IdP X.509 Certificate Renewal Sync).\n")
            } else {
                append("• Standard support guideline applied with contextual personalization.\n")
            }
            append("• Draft verified safe for autonomous or supervised delivery.")
        }
    }
}

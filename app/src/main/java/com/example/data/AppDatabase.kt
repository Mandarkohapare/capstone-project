package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TicketEntity::class, TicketTimelineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agent_queue_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).ticketDao()
                            val seedEntities = SeedData.getInitialTickets().map { TicketEntity.fromDomain(it) }
                            dao.insertTickets(seedEntities)
                            seedEntities.forEach { tkt ->
                                dao.insertTimelineItem(
                                    TicketTimelineEntity(
                                        ticketId = tkt.id,
                                        action = "Ticket Received",
                                        detail = "Inbound from ${tkt.channel.uppercase()} (${tkt.customerName})",
                                        timestamp = tkt.createdDate
                                    )
                                )
                                if (tkt.aiDraft != null) {
                                    dao.insertTimelineItem(
                                        TicketTimelineEntity(
                                            ticketId = tkt.id,
                                            action = "AI Draft Prepared",
                                            detail = "AI model generated response draft with ${(tkt.aiConfidence * 100).toInt()}% confidence",
                                            timestamp = tkt.createdDate + 120000L
                                        )
                                    )
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

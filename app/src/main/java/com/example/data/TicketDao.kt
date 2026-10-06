package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY createdDate DESC")
    fun getAllTicketsFlow(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    fun getTicketByIdFlow(id: String): Flow<TicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicket(id: String)

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getCount(): Int

    // Timeline queries
    @Query("SELECT * FROM ticket_timeline WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getTimelineForTicketFlow(ticketId: String): Flow<List<TicketTimelineEntity>>

    @Insert
    suspend fun insertTimelineItem(item: TicketTimelineEntity)
}

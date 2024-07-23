package edu.vt.cs5254.dreamcatcher.database

import androidx.room.*
import edu.vt.cs5254.dreamcatcher.Dream
import edu.vt.cs5254.dreamcatcher.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface DreamDao {

    //DLF:

    @Query("select * from dream d JOIN dream_entry e ON d.id = e.dreamId order by d.lastUpdated desc")
    fun getDreams(): Flow<Map<Dream, List<DreamEntry>>> // Flow of MultiMap

    //DDF:

    @Query("select * from dream where id=(:id)")
    suspend fun getDream(id: UUID): Dream

    @Query("select * from dream_entry where dreamId=(:dreamId)")
    suspend fun getDreamEntries(dreamId: UUID) : List<DreamEntry>

    @Transaction
    suspend fun getDreamWithEntries(id: UUID) : Dream {
        return getDream(id).apply { entries = getDreamEntries(id) }
    }

    @Update
    suspend fun updateDream(dream: Dream)

    @Insert
    suspend fun insertDreamEntry(dreamEntry: DreamEntry)

    @Query("delete from dream_entry where dreamId=(:dreamId)")
    suspend fun deleteEntriesFromDream(dreamId: UUID)

    @Transaction
    suspend fun updateDreamWithEntries(dream: Dream) {
        deleteEntriesFromDream(dream.id)
        dream.entries.forEach { insertDreamEntry(it) }
        updateDream(dream)
    }

    @Insert
    suspend fun insertDream(dream: Dream)

    @Transaction
    suspend fun insertDreamWithEntries(dream: Dream) {
        insertDream(dream)
        dream.entries.forEach {insertDreamEntry((it))}
    }

    @Delete
    suspend fun deleteDream(dream: Dream)

    @Transaction
    suspend fun deleteDreamWithEntries(dream: Dream) {
        deleteEntriesFromDream(dream.id)
        deleteDream(dream)
    }

}
package edu.vt.cs5254.dreamcatcher

import android.content.Context
import androidx.room.Room
import edu.vt.cs5254.dreamcatcher.database.DreamDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "dream-database"
class DreamRepository(context: Context, private val coroutineScope: CoroutineScope = GlobalScope) {


    private val database = Room.databaseBuilder(
        context.applicationContext,
        DreamDatabase::class.java,
        DATABASE_NAME
    )

        .createFromAsset(DATABASE_NAME)   // <- need to revisit
        .build()

    // DLF:
    fun getDreams(): Flow<List<Dream>> {
        val dreamMultiMapFlow = database.dreamDao().getDreams()
        return dreamMultiMapFlow.map {multiMap ->
            multiMap.keys.map { dream ->
                dream.apply { entries = multiMap.getValue(dream) }
            }
        }

    }

    // DDF Fetch:
    suspend fun getDream(id: UUID) = database.dreamDao().getDreamWithEntries(id)

    //DDF update:

     fun updateDream(dream: Dream) {
        coroutineScope.launch {
            database.dreamDao().updateDreamWithEntries(dream)
        }
    }

    suspend fun addDream(dream: Dream) = database.dreamDao().insertDreamWithEntries(dream)

    suspend fun deleteDream(dream: Dream) = database.dreamDao().deleteDreamWithEntries(dream)

    companion object {

        var INSTANCE: DreamRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null)
                INSTANCE = DreamRepository(context)
        }

        fun get() = checkNotNull(INSTANCE) {
            "DreamRepository Must be initialized!"
        }


    }


}
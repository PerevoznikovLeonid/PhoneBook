package top.college.phonebook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("SELECT * FROM table_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryEntity>>
}
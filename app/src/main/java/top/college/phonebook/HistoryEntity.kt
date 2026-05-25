package top.college.phonebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "table_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val entityId: Long,
    val action: String,
    val oldStateJson: String?,
    val newStateJson: String?,
    val timestamp: Date = Date()
)
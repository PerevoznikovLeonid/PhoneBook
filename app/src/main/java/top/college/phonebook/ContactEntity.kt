package top.college.phonebook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val patronymic: String? = null
)
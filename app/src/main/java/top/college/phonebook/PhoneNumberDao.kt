package top.college.phonebook

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneNumberDao {
    @Query("SELECT * FROM table_phone_numbers WHERE contactId = :contactId")
    fun getForContact(contactId: Long): Flow<List<PhoneNumberEntity>>

    @Insert
    suspend fun insert(phone: PhoneNumberEntity): Long

    @Update
    suspend fun update(phone: PhoneNumberEntity)

    @Delete
    suspend fun delete(phone: PhoneNumberEntity)

    @Query("DELETE FROM table_phone_numbers WHERE contactId = :contactId")
    suspend fun deleteForContact(contactId: Long)
}
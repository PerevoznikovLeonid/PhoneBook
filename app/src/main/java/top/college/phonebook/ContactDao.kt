package top.college.phonebook

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM table_contacts")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM table_contacts WHERE id = :id")
    fun getById(id: Long): ContactEntity?

    @Insert
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("""
        SELECT * FROM table_contacts 
        WHERE firstName LIKE '%' || :query || '%' 
           OR lastName LIKE '%' || :query || '%'
           OR patronymic LIKE '%' || :query || '%'
        ORDER BY lastName, firstName
    """)
    fun searchByName(query: String): Flow<List<ContactEntity>>

    @Query("""
        SELECT DISTINCT table_contacts.* FROM table_contacts 
        JOIN table_phone_numbers ON table_contacts.id = table_phone_numbers.contactId
        WHERE table_phone_numbers.number LIKE '%' || :query || '%'
        ORDER BY table_contacts.lastName, table_contacts.firstName
    """)
    fun searchByPhoneNumber(query: String): Flow<List<ContactEntity>>
}
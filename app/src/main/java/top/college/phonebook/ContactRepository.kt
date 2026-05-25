package top.college.phonebook

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ContactRepository(
    private val contactDao: ContactDao,
    private val phoneNumberDao: PhoneNumberDao,
    private val historyDao: HistoryDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<List<ContactEntity>>.mapToContactsWithPhones(): Flow<List<ContactWithPhones>> {
        return this.flatMapConcat { contacts ->
            contacts.toContactsWithPhonesFlow()
        }
    }

    private fun List<ContactEntity>.toContactsWithPhonesFlow(): Flow<List<ContactWithPhones>> {
        if (isEmpty()) return flowOf(emptyList())

        return combine(
            this.map { contact ->
                phoneNumberDao.getForContact(contact.id)
                    .map { phones -> contact to phones }
            }
        ) { arrays ->
            arrays.toList().map { (contact, phones) ->
                ContactWithPhones(contact, phones)
            }
        }
    }

    fun getAllContactsWithPhones(): Flow<List<ContactWithPhones>> {
        return contactDao.getAll()
            .mapToContactsWithPhones()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchContacts(query: String): Flow<List<ContactWithPhones>> {
        if (query.isBlank()) {
            return getAllContactsWithPhones()
        }

        val byNameFlow = contactDao.searchByName(query)
        val byPhoneFlow = contactDao.searchByPhoneNumber(query)

        return combine(byNameFlow, byPhoneFlow) { byName, byPhone ->
            (byName + byPhone).distinctBy { it.id }
        }.mapToContactsWithPhones()
    }

    suspend fun getContactWithPhones(contactId: Long): ContactWithPhones? {
        val contact = contactDao.getById(contactId) ?: return null
        val phones = phoneNumberDao.getForContact(contactId).first()
        return ContactWithPhones(contact, phones)
    }

    suspend fun insertContactWithPhones(contact: ContactEntity, phones: List<PhoneNumberEntity>) {
        val contactId = contactDao.insert(contact)
        val contactWithId = contact.copy(id = contactId)
        val phonesWithContactId = phones.map { it.copy(contactId = contactId) }
        phonesWithContactId.forEach { phoneNumberDao.insert(it) }

        Log.d("ContactRepository", "INSERT contact: $contactWithId, phones: $phonesWithContactId")
        historyDao.insert(
            HistoryEntity(
                tableName = "table_contacts",
                entityId = contactId,
                action = "INSERT",
                oldStateJson = null,
                newStateJson = Json.encodeToString(contactWithId)
            )
        )
        phonesWithContactId.forEach { phone ->
            historyDao.insert(
                HistoryEntity(
                    tableName = "table_phone_numbers",
                    entityId = phone.id,
                    action = "INSERT",
                    oldStateJson = null,
                    newStateJson = Json.encodeToString(phone)
                )
            )
        }
    }

    suspend fun updateContactWithPhones(contactId: Long, newContact: ContactEntity, newPhones: List<PhoneNumberEntity>) {
        val oldContact = contactDao.getById(contactId) ?: return
        val oldPhones = phoneNumberDao.getForContact(contactId).first()

        contactDao.update(newContact.copy(id = contactId))
        phoneNumberDao.deleteForContact(contactId)
        val phonesWithContactId = newPhones.map { it.copy(contactId = contactId) }
        phonesWithContactId.forEach { phoneNumberDao.insert(it) }

        Log.d("ContactRepository", "UPDATE contact $contactId")
        val oldContactJson = Json.encodeToString(oldContact)
        val newContactJson = Json.encodeToString(newContact.copy(id = contactId))
        historyDao.insert(
            HistoryEntity(
                tableName = "table_contacts",
                entityId = contactId,
                action = "UPDATE",
                oldStateJson = oldContactJson,
                newStateJson = newContactJson
            )
        )
        oldPhones.forEach { phone ->
            historyDao.insert(
                HistoryEntity(
                    tableName = "table_phones",
                    entityId = phone.id,
                    action = "DELETE",
                    oldStateJson = Json.encodeToString(phone),
                    newStateJson = null
                )
            )
        }
        phonesWithContactId.forEach { phone ->
            historyDao.insert(
                HistoryEntity(
                    tableName = "table_phone_numbers",
                    entityId = phone.id,
                    action = "INSERT",
                    oldStateJson = null,
                    newStateJson = Json.encodeToString(phone)
                )
            )
        }
    }

    suspend fun deleteContact(contactId: Long) {
        val contact = contactDao.getById(contactId) ?: return
        val phones = phoneNumberDao.getForContact(contactId).first()

        contactDao.delete(contact)
        phoneNumberDao.deleteForContact(contactId)

        Log.d("ContactRepository", "DELETE contact $contactId")
        val contactJson = Json.encodeToString(contact)
        historyDao.insert(
            HistoryEntity(
                tableName = "table_contacts",
                entityId = contactId,
                action = "DELETE",
                oldStateJson = contactJson,
                newStateJson = null
            )
        )
        phones.forEach { phone ->
            historyDao.insert(
                HistoryEntity(
                    tableName = "table_phone_numbers",
                    entityId = phone.id,
                    action = "DELETE",
                    oldStateJson = Json.encodeToString(phone),
                    newStateJson = null
                )
            )
        }
    }
}
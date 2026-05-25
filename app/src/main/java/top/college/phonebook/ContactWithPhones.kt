package top.college.phonebook

import androidx.lifecycle.LiveData

data class ContactWithPhones(
    val contact: ContactEntity,
    val phones: List<PhoneNumberEntity>
)
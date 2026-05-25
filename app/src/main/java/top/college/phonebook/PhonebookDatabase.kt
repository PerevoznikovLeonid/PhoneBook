package top.college.phonebook

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ContactEntity::class,
    PhoneNumberEntity::class, HistoryEntity::class],
    version = 1)
@TypeConverters(Converter::class)
abstract class PhonebookDatabase: RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun phoneNumberDao(): PhoneNumberDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: PhonebookDatabase? = null
        fun getInstance(context: Context): PhonebookDatabase {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PhonebookDatabase::class.java,
                        "phonebook_db"
                    ).fallbackToDestructiveMigration(false).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
package subtext.yuvallovenotes.crossapplication.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.loveletters.LoveClosure
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.loveletters.LovePhrase

// Annotates class to be a Room Database with a table (entity) of the Quality class
@Database(entities = [LoveItem::class, LoveOpener::class, LovePhrase::class, LoveClosure::class], version = 1, exportSchema = false)
abstract class LoveLocalDatabase : RoomDatabase() {

    abstract fun loveDao(): LoveDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: LoveLocalDatabase? = null

        fun getDatabase(): LoveLocalDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(YuvalLoveNotesApp.context!!, LoveLocalDatabase::class.java, "awareness_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

 /*   private class AwarenessDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let {
                scope.launch {
                }
            }
        }
    }*/
}
package subtext.yuvallovenotes.crossapplication.database

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.loveitems.*

// Annotates class to be a Room Database with a table (entity) of the Quality class
@Database(entities = [LoveLetter::class, LoveOpener::class, LovePhrase::class, LoveClosure::class], version = 3, exportSchema = false)
abstract class LoveLocalDatabase : RoomDatabase() {

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(YuvalLoveNotesApp.context)
    abstract fun loveDao(): LoveDao

    private class LoveLettersDatabaseCallback() : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                GlobalScope.launch(Dispatchers.IO) {
//                    database.loveDao().deleteAllLoveLetters()
//                    database.loveDao().deleteAllLoveOpeners()
//                    database.loveDao().deleteAllLovePhrases()
//                    database.loveDao().deleteAllLoveClosures()
                    val wasDataBasePopulatedFirstTimeKey = YuvalLoveNotesApp.context.getString(R.string.key_was_database_populated_first_time)
                    if (!database.sharedPrefs.getBoolean(wasDataBasePopulatedFirstTimeKey, false)) {
                        database.loveDao().insertAllLoveOpeners(DefaultLoveDataSet.openers)
                        database.loveDao().insertAllLovePhrases(DefaultLoveDataSet.phrases)
                        database.loveDao().insertAllLoveClosures(DefaultLoveDataSet.closures)
                        //Todo: mark true only after success
                        database.sharedPrefs.edit().putBoolean(wasDataBasePopulatedFirstTimeKey, true).apply()
                    }
                }
            }
        }
    }

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
                val instance = Room.databaseBuilder(YuvalLoveNotesApp.context, LoveLocalDatabase::class.java, "love_letters_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(LoveLettersDatabaseCallback())
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
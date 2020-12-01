package subtext.yuvallovenotes.crossapplication.database

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.loveitems.*

@Database(entities = [LoveLetter::class, LoveOpener::class, LovePhrase::class, LoveClosure::class], version = 5, exportSchema = false)
abstract class LoveLocalDatabase : RoomDatabase() {


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
                val instance = Room.databaseBuilder(YuvalLoveNotesApp.context, LoveLocalDatabase::class.java, "love_items_database")
                        .fallbackToDestructiveMigration()
                        .addCallback(LoveLettersDatabaseCallback())
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun loveDao(): LoveDao

    private class LoveLettersDatabaseCallback() : RoomDatabase.Callback() {

        fun generateRandomLetter(lovePhrase: LovePhrase): LoveLetter {
            var text = ""
            var opener = LoveOpener()
            var closure = LoveClosure()

            opener = ArielDefaultLoveDataSet.openers.randomOrNull() ?: opener
            text = text.plus(opener.text + "\n\n")

            /*    val allPhrasesShuffled = DefaultLoveDataSet.phrases.shuffled()
                finalPhrasesPoolForSingleLetter = allPhrasesShuffled.subList(0, lovePhrasesAmountInLetter(allPhrases))
                finalPhrasesPoolForSingleLetter.forEach { phrase ->
                    text = text.plus(phrase.text + "\n\n")
                }*/

            text = text.plus(lovePhrase.text + "\n\n")

            closure = ArielDefaultLoveDataSet.closures.randomOrNull() ?: closure
            text = text.plus(closure.text + "\n\n")

            val id = opener.id.plus(lovePhrase.id).plus(closure.id)

            val letter = LoveLetter(id, text)
            return letter
        }

        fun populateLettersList(db: LoveLocalDatabase) {
            GlobalScope.launch(Dispatchers.IO) {
                ArielDefaultLoveDataSet.phrases.forEach {
                    db.loveDao().insertLoveLetter(generateRandomLetter(it))
                }
            }
        }

        override fun onOpen(database: SupportSQLiteDatabase) {
            super.onOpen(database)
            INSTANCE?.let { db ->

                val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(YuvalLoveNotesApp.context)
                GlobalScope.launch(Dispatchers.IO) {
//                    db.loveDao().deleteAllLoveLetters()
//                    db.loveDao().deleteAllLoveOpeners()
//                    db.loveDao().deleteAllLovePhrases()
//                    db.loveDao().deleteAllLoveClosures()
                    val wasDataBasePopulatedFirstTimeKey = YuvalLoveNotesApp.context.getString(R.string.pref_key_was_database_populated_first_time)
                    //Todo: set to be correct condition
                    if (!sharedPrefs.getBoolean(wasDataBasePopulatedFirstTimeKey, true)) { //Only populate once, after app is installed
//                        db.loveDao().insertAllLoveOpeners(DefaultLoveDataSet.openers)
//                        db.loveDao().insertAllLovePhrases(DefaultLoveDataSet.phrases)
//                        db.loveDao().insertAllLoveClosures(DefaultLoveDataSet.closures)
                        //Todo: mark true only after success
                        populateLettersList(db)
                        sharedPrefs.edit().putBoolean(wasDataBasePopulatedFirstTimeKey, true).apply()
                    }
                }
            }
        }
    }
}
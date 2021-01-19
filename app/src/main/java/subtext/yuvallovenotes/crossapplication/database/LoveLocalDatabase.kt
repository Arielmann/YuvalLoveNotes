package subtext.yuvallovenotes.crossapplication.database

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log.d
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.database.initialdataset.ArielDefaultLoveDataSetInitial
import subtext.yuvallovenotes.crossapplication.database.initialdataset.DefaultLoveDataSet
import subtext.yuvallovenotes.crossapplication.database.initialdataset.InitialLettersDataSet
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveClosure
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveOpener
import subtext.yuvallovenotes.crossapplication.models.loveitems.LovePhrase


@Database(entities = [LoveLetter::class, LoveOpener::class, LovePhrase::class, LoveClosure::class], version = 7, exportSchema = false)
abstract class LoveLocalDatabase : RoomDatabase() {

    abstract fun loveDao(): LoveDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        private val TAG: String = LoveLocalDatabase::class.java.simpleName
        private val loveDataSet = inferDataSet()

        @SuppressLint("HardwareIds")
        private fun inferDataSet(): InitialLettersDataSet {
            val deviceId = Settings.Secure.getString(YuvalLoveNotesApp.context.contentResolver, Settings.Secure.ANDROID_ID);
            if(deviceId == "baaa54ce980799f6") {
            //Todo: cleanup
                return ArielDefaultLoveDataSetInitial
            }
            return DefaultLoveDataSet
        }


        @Volatile
        private var INSTANCE: LoveLocalDatabase? = null

        fun getDatabase(): LoveLocalDatabase {
            d(TAG, "getDatabase")
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                d(TAG, "instance exists")
                return tempInstance
            }
            d(TAG, "Creating new instance")
            synchronized(this) {
                val instance = Room.databaseBuilder(YuvalLoveNotesApp.context, LoveLocalDatabase::class.java, "love_items_database")
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                onDatabaseCreated()
                return instance
            }
        }

        private fun onDatabaseCreated() {
            INSTANCE?.let { database ->
                d(TAG, "Instance created, checking if initial population required")

                val sharedPrefs: SharedPreferences = get(SharedPreferences::class.java)
                GlobalScope.launch(Dispatchers.IO) {
//                    db.loveDao().deleteAllLoveLetters()
                    val wasDataBasePopulatedFirstTimeKey = YuvalLoveNotesApp.context.getString(R.string.pref_key_local_letters_database_populated_after_app_installed)
                    //Todo: set to be correct condition
                    if (sharedPrefs.getBoolean(wasDataBasePopulatedFirstTimeKey, false)) { //Only populate once, after app is installed
                        d(TAG, "Populating letters list")
                        populateLettersList(database)
                        sharedPrefs.edit().putBoolean(wasDataBasePopulatedFirstTimeKey, true).apply()
                    } else {
                        d(TAG, "Initial database population is not required")
                    }
                }
            }
        }

        private fun generateRandomLetter(lovePhrase: LovePhrase): LoveLetter {
            var text = ""
            var opener = LoveOpener()
            var closure = LoveClosure()

            opener = loveDataSet.getOpeners().randomOrNull() ?: opener
            text = text.plus(opener.text + "\n\n")

            text = text.plus(lovePhrase.text + "\n\n")

            closure = loveDataSet.getClosures().randomOrNull() ?: closure
            text = text.plus(closure.text + "\n\n")

            val id = opener.id.plus(lovePhrase.id).plus(closure.id)

            val letter = LoveLetter(id, text)
            return letter
        }

        private fun populateLettersList(db: LoveLocalDatabase) {
            GlobalScope.launch(Dispatchers.IO) {
                loveDataSet.getPhrases().forEach {
                    db.loveDao().insertLoveLetter(generateRandomLetter(it))
                }
            }
        }
    }

}

package subtext.yuvallovenotes.crossapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveLetter
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase


@Dao
interface LoveDao {

    //Love Letter
    @Query("SELECT * FROM love_letter_table WHERE id=:id ")
    fun getLoveLetterById(id: String): LiveData<LoveLetter>

    @Query("SELECT * FROM love_letter_table WHERE id=:id ")
    fun getLoveLetterByIdSync(id: String): LoveLetter?

    @Query("SELECT * FROM love_letter_table WHERE text=:text ")
    fun getLoveLetterByTextSync(text: String) : LoveLetter?

    @Query("SELECT * from love_letter_table")
    fun getAllLoveLetters(): LiveData<MutableList<LoveLetter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveLetter(item: LoveLetter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveLetters(items: List<LoveLetter?>?)

    @Update
    suspend fun updateLoveLetter(item: LoveLetter)

    @Query("SELECT EXISTS (SELECT 1 FROM love_letter_table WHERE id = :id)")
    fun isLoveLetterExists(id: String): Boolean

    @Query("DELETE FROM love_letter_table")
    suspend fun deleteAllLoveLetters()

    @Query("DELETE FROM love_letter_table WHERE id=:id")
    suspend fun deleteLoveLetter(id: String)


  //Love Opener

    @Query("SELECT * from love_opener_table")
    fun getAllLoveOpeners(): LiveData<List<LoveOpener>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveOpener(opener: LoveOpener)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveOpeners(openers: List<LoveOpener?>?)

    @Query("DELETE FROM love_opener_table")
    suspend fun deleteAllLoveOpeners()


    //Love Phrase

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getLovePhraseById(id: String): LiveData<LovePhrase>

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getLovePhraseByIdSync(id: String): LovePhrase?

    @Query("SELECT * from love_phrase_table")
    fun getAllLovePhrases(): LiveData<List<LovePhrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLovePhrase(phrase: LovePhrase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLovePhrases(phrases: List<LovePhrase?>?)

    @Update
    suspend fun updateLovePhrase(phrase: LovePhrase)

    @Query("SELECT EXISTS (SELECT 1 FROM love_phrase_table WHERE objectId = :id)")
    fun isLovePhraseExists(id: String): Boolean

    @Delete
    suspend fun deleteLovePhrase(phrase: LovePhrase)

    @Query("DELETE FROM love_phrase_table")
    suspend fun deleteAllLovePhrases()


    //Love Closure
    @Query("SELECT * from love_closure_table")
    fun getAllLoveClosures(): LiveData<List<LoveClosure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveClosure(closure: LoveClosure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveClosures(closures: List<LoveClosure?>?)

    @Query("DELETE FROM love_closure_table")
    suspend fun deleteAllLoveClosures()
    
}
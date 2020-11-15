package subtext.yuvallovenotes.crossapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase


@Dao
interface LoveDao {

    //Love Item

    @Query("SELECT * FROM love_item_table WHERE id=:id ")
    fun getLoveItemById(id: String): LiveData<LoveItem>

    @Query("SELECT * FROM love_item_table WHERE id=:id ")
    fun getLoveItemByIdSync(id: String): LoveItem?

    @Query("SELECT * from love_item_table")
    fun getAllLoveItems(): LiveData<List<LoveItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveItem(item: LoveItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveItems(items: List<LoveItem?>?)

    @Update
    suspend fun updateLoveItem(item: LoveItem)

    @Query("SELECT EXISTS (SELECT 1 FROM love_item_table WHERE id = :id)")
    fun isLoveItemExists(id: String): Boolean

    @Query("DELETE FROM love_item_table")
    suspend fun deleteAllLoveItems()

    @Query("DELETE FROM love_item_table WHERE id=:id")
    suspend fun deleteLoveItem(id: String)


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
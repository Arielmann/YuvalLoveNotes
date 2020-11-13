package subtext.yuvallovenotes.crossapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase


@Dao
interface LoveDao {
    @Query("SELECT * from love_opener_table")
    fun getAllLoveOpeners(): LiveData<List<LoveOpener>>

    @Query("SELECT * from love_phrase_table")
    fun getAllLovePhrases(): LiveData<List<LovePhrase>>

    @Query("SELECT * from love_closure_table")
    fun getAllLoveClosures(): LiveData<List<LoveClosure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveOpener(opener: LoveOpener)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveOpeners(openers: List<LoveOpener?>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLovePhrase(phrase: LovePhrase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLovePhrases(phrases: List<LovePhrase?>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoveClosure(closure: LoveClosure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLoveClosures(closures: List<LoveClosure?>?)

    @Update
    suspend fun update(phrase: LovePhrase)

    @Delete
    suspend fun delete(phrase: LovePhrase)

    @Query("SELECT EXISTS (SELECT 1 FROM love_phrase_table WHERE objectId = :id)")
    fun exists(id: String): Boolean

    @Query("DELETE FROM love_opener_table")
    suspend fun deleteAllLoveOpeners()

    @Query("DELETE FROM love_phrase_table")
    suspend fun deleteAllLovePhrases()

    @Query("DELETE FROM love_closure_table")
    suspend fun deleteAllLoveClosures()

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getById(id: String): LiveData<LovePhrase>

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getByIdSync(id: String): LovePhrase?
}
package subtext.yuvallovenotes.crossapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*
import subtext.yuvallovenotes.loveletters.LoveItem

@Dao
interface LoveDao {
    @Query("SELECT * from love_phrase_table ORDER BY text ASC")
    fun getAlphabetizedLovItems(): LiveData<List<LoveItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quality: LoveItem)

    @Update
    suspend fun update(quality: LoveItem)

    @Delete
    suspend fun delete(quality: LoveItem)

    @Query("SELECT EXISTS (SELECT 1 FROM love_item_table WHERE objectId = :id)")
    fun exists(id: String): Boolean

    @Query("DELETE FROM love_phrase_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getQualityById(id: String): LiveData<LoveItem>

    @Query("SELECT * FROM love_phrase_table WHERE objectId=:id ")
    fun getQualityByIdSync(id: String): LoveItem?
}
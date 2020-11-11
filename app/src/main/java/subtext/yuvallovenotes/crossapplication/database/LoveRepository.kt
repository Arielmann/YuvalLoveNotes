package subtext.yuvallovenotes.crossapplication.database

import androidx.lifecycle.LiveData
import subtext.yuvallovenotes.loveletters.LoveItem

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LoveRepository() {

    private val loveDao: LoveDao = LoveLocalDatabase.getDatabase().loveDao()

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    fun getAllLocalDBQualities() : LiveData<List<LoveItem>> {
        return loveDao.getAlphabetizedLovItems()
    }

    suspend fun insert(quality: LoveItem) {
        loveDao.insert(quality)
    }

    suspend fun update(quality: LoveItem) {
        loveDao.update(quality)
    }

    suspend fun delete(quality: LoveItem) {
        loveDao.delete(quality)
    }

    fun getQualityById(id: String) : LiveData<LoveItem>{
        return loveDao.getQualityById(id)
    }

    fun getQualityByIdSync(id: String): LoveItem? {
        return loveDao.getQualityByIdSync(id)
    }
}
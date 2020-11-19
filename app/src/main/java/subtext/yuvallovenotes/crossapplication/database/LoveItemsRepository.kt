package subtext.yuvallovenotes.crossapplication.database

import android.content.Context
import androidx.lifecycle.LiveData
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveClosure
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveOpener
import subtext.yuvallovenotes.crossapplication.models.loveitems.LovePhrase

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LoveItemsRepository(context: Context) {

    private val loveDao: LoveDao = LoveLocalDatabase.getDatabase().loveDao()

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    fun getAllLocalDBLoveLetters(): LiveData<MutableList<LoveLetter>> {
        return loveDao.getAllLoveLetters()
    }

    fun getAllLocalDBLoveOpeners(): LiveData<List<LoveOpener>> {
        return loveDao.getAllLoveOpeners()
    }

    fun getAllLocalDBLovePhrases(): LiveData<List<LovePhrase>> {
        return loveDao.getAllLovePhrases()
    }

    fun getAllLocalDBLoveClosure(): LiveData<List<LoveClosure>> {
        return loveDao.getAllLoveClosures()
    }

    suspend fun insertLoveLetter(item: LoveLetter) {
        loveDao.insertLoveLetter(item)
    }

    suspend fun insertAllLoveLetters(items: List<LoveLetter>) {
        loveDao.insertAllLoveLetters(items)
    }


    fun getLoveLetterById(id: String): LiveData<LoveLetter> {
        return loveDao.getLoveLetterById(id)
    }

    fun getLoveLetterByTextSync(text: String): LoveLetter? {
        return loveDao.getLoveLetterByTextSync(text)
    }


    suspend fun insertLoveOpener(opener: LoveOpener) {
        loveDao.insertLoveOpener(opener)
    }

    suspend fun insertLovePhrase(phrase: LovePhrase) {
        loveDao.insertLovePhrase(phrase)
    }

    suspend fun insertAllLoveOpeners(openers: List<LoveOpener>) {
        loveDao.insertAllLoveOpeners(openers)
    }

    suspend fun insertAllLovePhrases(phrases: List<LovePhrase>) {
        loveDao.insertAllLovePhrases(phrases)
    }

    suspend fun insertAllLoveClosures(closures: List<LoveClosure>) {
        loveDao.insertAllLoveClosures(closures)
    }

    fun getLovePhraseById(phrase: LovePhrase): LiveData<LovePhrase> {
        return loveDao.getLovePhraseById(phrase.id)
    }

    fun getLovePhraseByIdSync(phrase: LovePhrase): LovePhrase? {
        return loveDao.getLovePhraseByIdSync(phrase.id)
    }
}
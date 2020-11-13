package subtext.yuvallovenotes.crossapplication.database

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import subtext.yuvallovenotes.crossapplication.backendless.LoveNetworkCalls
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LoveRepository(context: Context) {

    private val loveDao: LoveDao = LoveLocalDatabase.getDatabase().loveDao()
    var loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)
    
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    fun getAllLocalDBLoveOpeners() : LiveData<List<LoveOpener>> {
        return loveDao.getAllLoveOpeners()
    }

    fun getAllLocalDBLovePhrases() : LiveData<List<LovePhrase>> {
        return loveDao.getAllLovePhrases()
    }

    fun getAllLocalDBLoveClosure() : LiveData<List<LoveClosure>> {
        return loveDao.getAllLoveClosures()
    }

    suspend fun insertLovePhrase(phrase: LovePhrase) {
    /*    loveNetworkCalls.save(phrase, object : AsyncCallback<LovePhrase> {
            override fun handleResponse(response: LovePhrase?) {
                TODO("Not yet implemented")
            }

            override fun handleFault(fault: BackendlessFault?) {
                TODO("Not yet implemented")
            }
        })*/
        loveDao.insertLovePhrase(phrase)
    }

    suspend fun insertAllLovePhrases(phrases: List<LovePhrase>) {
        loveDao.insertAllLovePhrases(phrases)
    }

    suspend fun update(phrase: LovePhrase) {
        loveDao.update(phrase)
    }

    suspend fun delete(phrase: LovePhrase) {
        loveDao.delete(phrase)
    }

    fun getLovePhraseById(phrase: LovePhrase) : LiveData<LovePhrase>{
        return loveDao.getById(phrase.id)
    }

    fun getLovePhraseByIdSync(phrase: LovePhrase): LoveItem? {
        return loveDao.getByIdSync(phrase.id)
    }
}
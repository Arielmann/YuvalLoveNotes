package subtext.yuvallovenotes.crossapplication.database

import android.util.Log
import androidx.lifecycle.LiveData
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.localization.Language
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveClosure
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveOpener
import subtext.yuvallovenotes.crossapplication.models.loveitems.LovePhrase
import subtext.yuvallovenotes.crossapplication.models.users.LoveLettersUser
import subtext.yuvallovenotes.crossapplication.network.BackendlessNetworkServiceImpl
import subtext.yuvallovenotes.crossapplication.network.LoveLettersNetworkService
import subtext.yuvallovenotes.crossapplication.network.NetworkCallback
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils.isNetworkAvailable

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LoveItemsRepository {

    companion object {
        private val TAG: String = LoveItemsRepository::class.simpleName!!
    }

    private val loveDao: LoveDao = LoveLocalDatabase.getDatabase().loveDao()
    private val loveLettersNetworkService: LoveLettersNetworkService = get(BackendlessNetworkServiceImpl::class.java)

    private fun startOperationIfNetworkAvailable(operation: () -> Unit, networkCallback: NetworkCallback<*>?){
        val context = YuvalLoveNotesApp.context
        val noNetworkError = context.getString(R.string.error_no_network_connection)
        if (isNetworkAvailable(context)) {
            operation.invoke()
        } else {
            Log.w(TAG, "No network connection")
            networkCallback?.onFailure(noNetworkError)
        }
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    suspend fun insertAllLoveLetters(letters: List<LoveLetter>) {
        loveDao.insertAllLoveLetters(letters)
    }

    fun getAllLocalDBLoveLetters(): LiveData<MutableList<LoveLetter>?> {
        return loveDao.getAllLoveLetters()
    }

    fun getAllLocalDBLettersWrittenByUser(): LiveData<MutableList<LoveLetter>> {
        return loveDao.getAllLoveLettersWrittenByUser()
    }

    fun getLoveLetterById(id: String): LiveData<LoveLetter?> {
        return loveDao.getLoveLetterById(id)
    }

    fun getLoveLetterByTextSync(text: String): LoveLetter? {
        return loveDao.getLoveLetterByTextSync(text)
    }

    suspend fun insertLetter(letter: LoveLetter) {
        loveDao.insertLoveLetter(letter)
    }

    fun insertLetterSync(letter: LoveLetter) {
        loveDao.insertLoveLetterSync(letter)
    }

    suspend fun updateLetter(currentLetter: LoveLetter) {
        loveDao.updateLoveLetter(currentLetter)
    }

    fun updateLetterArchiveStatusSync(letter: LoveLetter) {
        loveDao.updateLetterArchiveStatusSync(letter.id, letter.isArchived)
    }

    suspend fun deleteLetter(letter: LoveLetter) {
        loveDao.deleteLoveLetter(letter.id)
    }

    fun deleteLetterSync(letter: LoveLetter) {
        loveDao.deleteLoveLetterSync(letter.id)
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

    fun uploadLetters(letters: List<LoveLetter>){
        loveLettersNetworkService.uploadLetters(letters)
    }

    fun fetchLettersFromServer(user: LoveLettersUser, language: Language, offset: Int = 0, callback: NetworkCallback<MutableList<LoveLetter>>) {
        startOperationIfNetworkAvailable({loveLettersNetworkService.fetchLetters(user, language, offset, callback)}, callback)
    }
}
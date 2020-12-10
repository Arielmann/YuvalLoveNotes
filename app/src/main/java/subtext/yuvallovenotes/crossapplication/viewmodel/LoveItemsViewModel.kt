package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.coroutines.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.network.LoveNetworkCalls
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import java.lang.ref.WeakReference

class LoveItemsViewModel(context: Context) : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveItemsRepository: LoveItemsRepository = LoveItemsRepository()
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()
    private val weakContext: WeakReference<Context> = WeakReference(context)
    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    //todo: cleanup
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)

    internal var loveLetters: LiveData<MutableList<LoveLetter>>

    init {
        loveLetters = loveItemsRepository.getAllLocalDBLoveLetters()
    }


    //todo: cleanup
    val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            loveItemsFromNetwork = response.toMutableList()

            val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
            insertAllPhrases(allPhrases)
            val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
            insertAllOpeners(openers)
            val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()
            insertAllClosures(closures)
        }

        override fun handleFault(fault: BackendlessFault?) {
            Log.d(TAG, "Backendless error: ${fault.toString()}")
            Toast.makeText(weakContext.get(), fault.toString(), Toast.LENGTH_LONG).show()
        }
    }

    internal fun insertAllOpeners(openers: List<LoveOpener>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLoveOpeners(openers)
        }
    }

    internal fun insertAllPhrases(phrases: List<LovePhrase>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLovePhrases(phrases)
        }
    }

    internal fun insertAllClosures(closures: List<LoveClosure>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLoveClosures(closures)
        }
    }

    internal fun getLetterById(id: String): LiveData<LoveLetter> {
        return loveItemsRepository.getLoveLetterById(id)
    }

    internal fun insertLetter(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertLoveLetter(letter)
        }
    }

    internal fun updateLetter(currentLetter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.updateLoveLetter(currentLetter)
        }
    }

    internal fun deleteLetterSync(letter: LoveLetter) {
        runBlocking {
            val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                loveItemsRepository.deleteLetterSync(letter)
                return@async
            }
            waitForDeletion.await()
        }
    }

    internal fun randomLetter(): LoveLetter {
        val optionalLetters = loveLetters.value
        val showOnlyLettersCreatedByUser = sharedPrefs.getBoolean(weakContext.get()!!.getString(R.string.pref_key_show_only_letters_created_by_user), false)
        var result = optionalLetters?.randomOrNull()
        if (showOnlyLettersCreatedByUser) {
            val lettersCreatedByUser = optionalLetters?.filter { !it.isDisabled && it.isCreatedByUser }
            result = lettersCreatedByUser?.randomOrNull()
        }

        if (result == null) {
            result = LoveLetter()
            result.isCreatedByUser = true

            runBlocking {
                val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                    loveItemsRepository.insertLoveLetterSync(result)
                }
                waitForDeletion.await()
            }
        }

        return result
    }

    /**
     * Deleting a letter if its empty.
     * Returns true if letter was deleted
     */
    internal fun deleteLetterIfEmpty(letter: LoveLetter?): Boolean {
        var isDeleted = false
        letter?.let {
            if (it.text.isBlank()) {
                Log.d(TAG, "deleting empty letter from data base")
                runBlocking {
                    val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                        loveItemsRepository.deleteLetterSync(letter)
                    }
                    waitForDeletion.await()
                    isDeleted = true
                }
            }
        }
        Log.d(TAG, "love letters list size: " + loveLetters.value?.size)
        return isDeleted
    }
}
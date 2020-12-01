package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.backendless.LoveNetworkCalls
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import subtext.yuvallovenotes.lovelettersgenerator.LetterGeneratorFragment
import java.lang.ref.WeakReference

class LoveItemsViewModel(context: Context) : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveItemsRepository: LoveItemsRepository = LoveItemsRepository()
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()
    private val weakContext: WeakReference<Context> = WeakReference(context)
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)


    //todo: cleanup
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)

    internal var loveLetters: LiveData<MutableList<LoveLetter>>
//    private var loveOpeners: LiveData<List<LoveOpener>>
//    private var lovePhrases: LiveData<List<LovePhrase>>
//    private var loveClosures: LiveData<List<LoveClosure>>
//    var areLoveItemsAvailable: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
//        areLoveItemsAvailable.value = false
        loveLetters = loveItemsRepository.getAllLocalDBLoveLetters()
//        loveOpeners = loveItemsRepository.getAllLocalDBLoveOpeners()
//        lovePhrases = loveItemsRepository.getAllLocalDBLovePhrases()
//        loveClosures = loveItemsRepository.getAllLocalDBLoveClosure()

        /*     areLoveItemsAvailable.addSource(loveOpeners) { openers ->
                 areLoveItemsAvailable.value = openers.isEmpty() == false &&
                         lovePhrases.value?.isNullOrEmpty() == false &&
                         loveClosures.value?.isNullOrEmpty() == false
             }

             areLoveItemsAvailable.addSource(lovePhrases) { phrases ->
                 areLoveItemsAvailable.value = phrases.isEmpty() == false &&
                         loveOpeners.value?.isNullOrEmpty() == false &&
                         loveClosures.value?.isNullOrEmpty() == false
             }

             areLoveItemsAvailable.addSource(loveClosures) { closures ->
                 areLoveItemsAvailable.value = closures.isEmpty() == false &&
                         loveOpeners.value?.isNullOrEmpty() == false &&
                         lovePhrases.value?.isNullOrEmpty() == false
             }*/
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

    fun insertAllOpeners(openers: List<LoveOpener>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLoveOpeners(openers)
        }
    }

    fun insertAllPhrases(phrases: List<LovePhrase>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLovePhrases(phrases)
        }
    }

    fun insertAllClosures(closures: List<LoveClosure>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertAllLoveClosures(closures)
        }
    }

    fun getLetterById(id: String): LiveData<LoveLetter> {
        return loveItemsRepository.getLoveLetterById(id)
    }

    fun insertLetter(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertLoveLetter(letter)
        }
    }

    fun updateLetter(currentLetter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.updateLoveLetter(currentLetter)
        }
    }

    fun deleteLetter(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.deleteLetter(letter)
        }
    }

    fun randomLetter(): LoveLetter {
        val optionalLetters = loveLetters.value
        val showOnlyLettersCreatedByUser = sharedPrefs.getBoolean(weakContext.get()!!.getString(R.string.pref_key_show_only_letters_created_by_user), false)
        var result = optionalLetters?.randomOrNull()
        if (showOnlyLettersCreatedByUser) {
            result = optionalLetters?.filter { !it.isDisabled && it.isCreatedByUser }?.randomOrNull()
        }

        if(result == null){
            result = LoveLetter()
        }

        return result
    }
}
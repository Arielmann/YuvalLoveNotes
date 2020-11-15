package subtext.yuvallovenotes.lovetabs.viewmodel

import android.content.Context
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.backendless.LoveNetworkCalls
import subtext.yuvallovenotes.crossapplication.database.LoveRepository
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import java.util.*

class LetterViewModel(context: Context) : ViewModel() {

    private val loveRepository: LoveRepository = LoveRepository(context)
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()
    private var loveItems: LiveData<List<LoveItem>>
    private var loveOpeners: LiveData<List<LoveOpener>>
    private var lovePhrases: LiveData<List<LovePhrase>>
    private var loveClosures: LiveData<List<LoveClosure>>
    var areAllLoveItemsAvailable: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        areAllLoveItemsAvailable.value = false
        loveItems = loveRepository.getAllLocalDBLoveItems()
        loveOpeners = loveRepository.getAllLocalDBLoveOpeners()
        lovePhrases = loveRepository.getAllLocalDBLovePhrases()
        loveClosures = loveRepository.getAllLocalDBLoveClosure()

        areAllLoveItemsAvailable.addSource(loveOpeners) { openers ->
            areAllLoveItemsAvailable.value = openers.isEmpty() == false &&
                    lovePhrases.value?.isNullOrEmpty() == false &&
                    loveClosures.value?.isNullOrEmpty() == false
        }

        areAllLoveItemsAvailable.addSource(lovePhrases) { phrases ->
            areAllLoveItemsAvailable.value = phrases.isEmpty() == false &&
                    loveOpeners.value?.isNullOrEmpty() == false &&
                    loveClosures.value?.isNullOrEmpty() == false
        }

        areAllLoveItemsAvailable.addSource(loveClosures) { closures ->
            areAllLoveItemsAvailable.value = closures.isEmpty() == false &&
                    loveOpeners.value?.isNullOrEmpty() == false &&
                    lovePhrases.value?.isNullOrEmpty() == false
        }
    }

    fun insertAllLoveOpeners(openers: List<LoveOpener>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertAllLoveOpeners(openers)
        }
    }

    fun insertAllLovePhrases(phrases: List<LovePhrase>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertAllLovePhrases(phrases)
        }
    }

    fun insertAllLoveClosures(closures: List<LoveClosure>) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertAllLoveClosures(closures)
        }
    }

    fun insertLoveItem(item: LoveItem) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertLoveItem(item)
        }
    }

    fun insertLoveOpener(opener: LoveOpener) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertLoveOpener(opener)
        }
    }

    fun insertRandomLoveLettersToLocalDatabase() {
       if(areAllLoveItemsAvailable.value == true){

           lovePhrases.value?.forEach { _ ->
               val item = LoveItem(UUID.randomUUID().toString(), newLetterText())
               if(loveItems.value?.find { it.text == item.text } == null) {
                   insertLoveItem(item)
               }
           }
       }
    }

    fun lovePhrasesAmountInLetter(allPhrases: List<LovePhrase>): Int {
        val context = YuvalLoveNotesApp.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val keyNumberOfLovePhrases = context.getString(R.string.pref_key_number_of_love_phrases)
        val defaultPhrasesAmountForLetter = context.resources.getInteger(R.integer.default_love_phrases_amount_for_single_letter)
        var lastIndex: Int = prefs.getString(keyNumberOfLovePhrases, defaultPhrasesAmountForLetter.toString())!!.toIntOrNull().takeIf { it != null && it > 0 }
                ?: defaultPhrasesAmountForLetter
        if (allPhrases.getOrNull(lastIndex) == null) {
            lastIndex = allPhrases.size
        }
        return lastIndex
    }

    fun newLetterText(openers: List<LoveOpener>, phrases: List<LovePhrase>, closures: List<LoveClosure>): String {
        var result = ""
        if (!openers.isNullOrEmpty()) {
            result = result.plus(openers.randomOrNull()?.text + "\n\n")
        }

        phrases.forEach { phrase ->
            result = result.plus(phrase.text + "\n\n")

        }

        if (!closures.isNullOrEmpty()) {
            result = result.plus(closures.random().text)
        }
        return result
    }

    fun newLetterText(): String {
        var result = ""
        areAllLoveItemsAvailable.value?.let {
            val allPhrases: List<LovePhrase> = lovePhrases.value!!.shuffled()
            val finalPhrasesPoolForSingleLetter: List<LovePhrase> = allPhrases.subList(0, lovePhrasesAmountInLetter(allPhrases))
            val openers: List<LoveOpener> = loveOpeners.value!!
            val closures: List<LoveClosure> = loveClosures.value!!
            if (!openers.isNullOrEmpty()) {
                result = result.plus(openers.randomOrNull()?.text + "\n\n")
            }

            finalPhrasesPoolForSingleLetter.forEach { phrase ->
                result = result.plus(phrase.text + "\n\n")

            }

            if (!closures.isNullOrEmpty()) {
                result = result.plus(closures.random().text)
            }
        }
        return result
    }

}
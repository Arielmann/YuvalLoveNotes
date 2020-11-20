package subtext.yuvallovenotes.lovelettersgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.backendless.LoveNetworkCalls
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import java.util.*

class LoveItemsViewModel(context: Context) : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveItemsRepository: LoveItemsRepository = LoveItemsRepository(context)
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()

    //todo: cleanup
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)

    internal var loveLetters: LiveData<MutableList<LoveLetter>>
    private var loveOpeners: LiveData<List<LoveOpener>>
    private var lovePhrases: LiveData<List<LovePhrase>>
    private var loveClosures: LiveData<List<LoveClosure>>
    var areLoveItemsAvailable: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        areLoveItemsAvailable.value = false
        loveLetters = loveItemsRepository.getAllLocalDBLoveLetters()
        loveOpeners = loveItemsRepository.getAllLocalDBLoveOpeners()
        lovePhrases = loveItemsRepository.getAllLocalDBLovePhrases()
        loveClosures = loveItemsRepository.getAllLocalDBLoveClosure()

        areLoveItemsAvailable.addSource(loveOpeners) { openers ->
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

    fun populateLettersList() {
        for (i in 0..100) {
            insertLetter(generateRandomLetter())
//            loveLetters.value?.add(generateRandomLetter())
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

    //todo: cleanup
    fun generateRandomLetterText(openers: List<LoveOpener>, phrases: List<LovePhrase>, closures: List<LoveClosure>): String {
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

    private fun generateRandomLetter(): LoveLetter {
        var text = ""
        var opener = LoveOpener()
        var finalPhrasesPoolForSingleLetter: List<LovePhrase> = mutableListOf()
        var closure = LoveClosure()

        areLoveItemsAvailable.value?.let {
            loveOpeners.value?.let {
                opener = it.randomOrNull() ?: opener
                text = text.plus(opener.text + "\n\n")
            }

            lovePhrases.value?.let { allPhrases ->
                finalPhrasesPoolForSingleLetter = allPhrases.subList(0, lovePhrasesAmountInLetter(allPhrases))
                finalPhrasesPoolForSingleLetter.forEach { phrase ->
                    text = text.plus(phrase.text + "\n\n")
                }
            }

            loveClosures.value?.let {
                closure = it.randomOrNull() ?: closure
                text = text.plus(closure.text + "\n\n")
            }
        }

        val id : String = run {
            val phrasesIds = run{
                var ids = ""
                finalPhrasesPoolForSingleLetter.forEach {
                    ids = ids.plus(it.id)
                }
            }
            opener.id.plus(phrasesIds).plus(closure.id)
        }

        val letter = LoveLetter(id, text)
        return letter
    }

    fun randomLetter(): LoveLetter? {
        val result = loveLetters.value?.randomOrNull()
        return result
    }

}
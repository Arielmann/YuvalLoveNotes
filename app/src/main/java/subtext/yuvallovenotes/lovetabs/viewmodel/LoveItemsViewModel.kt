package subtext.yuvallovenotes.lovetabs.viewmodel

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
import subtext.yuvallovenotes.crossapplication.database.LoveRepository
import subtext.yuvallovenotes.loveitems.*
import java.util.*

class LoveItemsViewModel(context: Context) : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveRepository: LoveRepository = LoveRepository(context)
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()

    internal var loveLetters: LiveData<MutableList<LoveLetter>>
    private var loveOpeners: LiveData<List<LoveOpener>>
    private var lovePhrases: LiveData<List<LovePhrase>>
    private var loveClosures: LiveData<List<LoveClosure>>
    var areLoveItemsAvailable: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        areLoveItemsAvailable.value = false
        loveLetters = loveRepository.getAllLocalDBLoveLetters()
        loveOpeners = loveRepository.getAllLocalDBLoveOpeners()
        lovePhrases = loveRepository.getAllLocalDBLovePhrases()
        loveClosures = loveRepository.getAllLocalDBLoveClosure()

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

    fun insertLoveItem(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertLoveLetter(letter)
        }
    }

    fun populateLoveLettersList() {
        for (i in 0..100) {
            loveLetters.value?.add(generateRandomLetter())
        }
    }

    fun insertLoveOpener(opener: LoveOpener) {
        viewModelScope.launch(Dispatchers.IO) {
            loveRepository.insertLoveOpener(opener)
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

    fun generateRandomLetter(openers: List<LoveOpener>, phrases: List<LovePhrase>, closures: List<LoveClosure>): String {
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
        areLoveItemsAvailable.value?.let {
            val allPhrases: List<LovePhrase> = lovePhrases.value!!.shuffled()
            val finalPhrasesPoolForSingleLetter: List<LovePhrase> = allPhrases.subList(0, lovePhrasesAmountInLetter(allPhrases))
            val openers: List<LoveOpener> = loveOpeners.value!!
            val closures: List<LoveClosure> = loveClosures.value!!
            if (!openers.isNullOrEmpty()) {
                text = text.plus(openers.randomOrNull()?.text + "\n\n")
            }

            finalPhrasesPoolForSingleLetter.forEach { phrase ->
                text = text.plus(phrase.text + "\n\n")

            }

            if (!closures.isNullOrEmpty()) {
                text = text.plus(closures.random().text)
            }
        }
        val letter = LoveLetter(UUID.randomUUID().toString(), text)
        return letter
    }

    fun randomLetter(): LoveLetter? {
        val result = loveLetters.value?.randomOrNull()
        return result
    }

}
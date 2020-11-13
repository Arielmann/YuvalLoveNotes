package subtext.yuvallovenotes.lovetabs.viewmodel

import android.content.Context
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.backendless.LoveNetworkCalls
import subtext.yuvallovenotes.crossapplication.database.LoveRepository
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase

class LoveViewModel(context: Context) : ViewModel() {

    private val loveRepository: LoveRepository = LoveRepository(context)
    val loveNetworkCalls: LoveNetworkCalls = LoveNetworkCalls(context)
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()
    private var loveOpenersLocal: LiveData<List<LoveOpener>>
    private var lovePhrasesLocal: LiveData<List<LovePhrase>>
    private var loveClosuresLocal: LiveData<List<LoveClosure>>
    var areAllLoveItemsAvailable: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        areAllLoveItemsAvailable.value = false
        loveOpenersLocal = loveRepository.getAllLocalDBLoveOpeners()
        lovePhrasesLocal = loveRepository.getAllLocalDBLovePhrases()
        loveClosuresLocal = loveRepository.getAllLocalDBLoveClosure()

        areAllLoveItemsAvailable.addSource(loveOpenersLocal) { openers ->
            areAllLoveItemsAvailable.value = openers.isEmpty() == false &&
                    lovePhrasesLocal.value?.isNullOrEmpty() == false &&
                    loveClosuresLocal.value?.isNullOrEmpty() == false
        }

        areAllLoveItemsAvailable.addSource(lovePhrasesLocal) { phrases ->
            areAllLoveItemsAvailable.value = phrases.isEmpty() == false &&
                    loveOpenersLocal.value?.isNullOrEmpty() == false &&
                    loveClosuresLocal.value?.isNullOrEmpty() == false
        }

        areAllLoveItemsAvailable.addSource(loveClosuresLocal) { closures ->
            areAllLoveItemsAvailable.value = closures.isEmpty() == false &&
                    loveOpenersLocal.value?.isNullOrEmpty() == false &&
                    lovePhrasesLocal.value?.isNullOrEmpty() == false
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

    fun newLetter(openers: List<LoveOpener>, phrases: List<LovePhrase>, closures: List<LoveClosure>): String {
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

    fun newLetter(): String {
        var result = ""
        areAllLoveItemsAvailable.value?.let {
            val allPhrases: List<LovePhrase> = lovePhrasesLocal.value!!.shuffled()
            val finalPhrasesPoolForSingleLetter: List<LovePhrase> = allPhrases.subList(0, lovePhrasesAmountInLetter(allPhrases))
            val openers: List<LoveOpener> = loveOpenersLocal.value!!
            val closures: List<LoveClosure> = loveClosuresLocal.value!!
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
package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.events.LoveLetterEvent
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import subtext.yuvallovenotes.lovelettersgenerator.whatsappsender.WhatsAppSender
import java.util.*

class LoveItemsViewModel : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveItemsRepository: LoveItemsRepository = get(LoveItemsRepository::class.java)
    private val sharedPrefs: SharedPreferences = get(SharedPreferences::class.java)
    private var currentLetterIndex = -1
    private var lettersHistoryList: MutableList<LoveLetter?> = mutableListOf()
    val emptyGeneratedLetterEvent: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val noNextLetters: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val noPreviousLetters: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val previousLettersFound: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val nextLettersFound: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val onLetterPickedForEditing: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val onLetterAddedToFavourites: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    val onLetterRemovedFromFavourites: MutableLiveData<LoveLetterEvent<Unit>> = MutableLiveData()
    var loveLetters: LiveData<MutableList<LoveLetter>?> = MutableLiveData()

    var currentLetter: LoveLetter? = null
        set(value) {
            d(TAG, "New current letter: ${value?.id}")
            if (!value?.id.isNullOrBlank()) {
                sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_current_letter_id), value!!.id).apply()
                notifyRelevantLetterFavouriteStateObservers(value)
                notifyRelevantDisplayLettersStateObservers()
            }
            field = value
        }

    init {
        loveLetters = loveItemsRepository.getAllLocalDBLoveLetters()
    }

    fun getFilteredLetters(): List<LoveLetter> {
        var optionalLetters = loveLetters.value?.filter { !it.isArchived && it.text.isNotBlank() }
        val showOnlyLettersCreatedByUser = sharedPrefs.getBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_show_only_letters_created_by_user), false)
        if (showOnlyLettersCreatedByUser) {
            optionalLetters = optionalLetters?.filter { it.isCreatedByUser }
        }
        return optionalLetters ?: mutableListOf()
    }

    internal fun getLetterById(id: String): LiveData<LoveLetter?> {
        return loveItemsRepository.getLoveLetterById(id)
    }

    private fun insertLetterToDB(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertLetter(letter)
        }
    }

    internal fun updateLetter(currentLetter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.updateLetter(currentLetter)
        }
    }

    internal fun switchCurrentLetterFavouriteState() {
        currentLetter?.let {
            it.isFavourite = !it.isFavourite
            notifyRelevantLetterFavouriteStateObservers(it)
            viewModelScope.launch(Dispatchers.IO) {
                loveItemsRepository.updateLetter(it)
            }
        } ?: w(TAG, "Warning: Tried to update a nul letter")
    }

    /**
     * Notifies the observers of changes in a [LoveLetter]'s favourite
     * state. The triggered observers will be determined by the letter's favourite boolean's state
     * so updates regarding this data could take place
     */
    private fun notifyRelevantLetterFavouriteStateObservers(letter: LoveLetter) {
        if (letter.isFavourite) {
            onLetterAddedToFavourites.postValue(LoveLetterEvent(Unit))
        } else {
            onLetterRemovedFromFavourites.postValue(LoveLetterEvent(Unit))
        }
    }

    internal fun updateLettersArchiveStatusSync(letters: List<LoveLetter>, isArchive: Boolean) {
        runBlocking {
            val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                letters.forEach { letter ->
                    letter.isArchived = isArchive
                    loveItemsRepository.updateLetterArchiveStatusSync(letter)
                }
                return@async
            }
            waitForDeletion.await()
        }
    }

    internal fun deleteLettersSync(letters: List<LoveLetter>) {
        runBlocking {
            d(TAG, "deleteLettersSync for letter ${letters.first()}")
            val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                letters.forEach { letter ->
                    loveItemsRepository.deleteLetterSync(letter)
                    cleanDisplayListFromCorruptedLetters { it.id == letter.id }
                }
                return@async
            }
            loveLetters.value?.removeAll(letters)
            waitForDeletion.await()
        }
    }

    fun randomLetter(): LoveLetter {
        val optionalLetters = getFilteredLetters()
        var result = optionalLetters.randomOrNull()

        if (currentLetter?.text == result?.text && optionalLetters.size > 1) { //If it's the same letter request new one
            return randomLetter()
        }

        if (result == null) {
            result = LoveLetter()
            result.isCreatedByUser = true

            runBlocking {
                val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                    loveItemsRepository.insertLetterSync(result)
                }
                waitForDeletion.await()
            }
        }

        return result
    }

    fun getCurrentLetterId(): String {
        val currentId = sharedPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_current_letter_id), "")!!
        return currentId
    }

    /**
     * Deleting a all letters that satisfies the given condition.
     * Used for preventing empty or deleted letters from remaining in the displayed letters list
     */
    internal fun cleanDisplayListFromCorruptedLetters(validationFunction: (letter: LoveLetter) -> Boolean): Boolean {
        var isDeleted = false
        with(lettersHistoryList.listIterator()) { //Prevents concurrent modification exception
            forEach { letter ->
                letter?.let {
                    if (validationFunction.invoke(it)) {
                        d(TAG, "Deleting letter from data base")
                        runBlocking {
                            val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                                loveItemsRepository.deleteLetterSync(letter)
                            }
                            isDeleted = true
                            waitForDeletion.await()
                        }
                        currentLetterIndex--
                        remove()
                    }
                }
            }
        }
        return isDeleted
    }

    fun clearLettersHistory() {
        i(TAG, "Clearing letters history")
        lettersHistoryList.clear()
        currentLetterIndex = -1
        noNextLetters.value = LoveLetterEvent(Unit)
        noPreviousLetters.value = LoveLetterEvent(Unit)
    }

    fun isLoginProcessCompleted(): Boolean {
        val sharedPrefs = get(SharedPreferences::class.java)
        val context = YuvalLoveNotesApp.context
        return sharedPrefs.getBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), false)
                && sharedPrefs.getBoolean(context.getString(R.string.pref_key_user_registered_in_server), false)
                && sharedPrefs.getBoolean(context.getString(R.string.pref_key_default_letters_downloaded), false)
    }

    fun loadInterstitialAd(context: Context, onSuccess: (InterstitialAd) -> Unit) {
        // Create the InterstitialAd and set it up.
        val unitId = BuildConfig.INTERSTITIAL_ADS_ID
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, unitId, adRequest, object : InterstitialAdLoadCallback() {

            override fun onAdLoaded(loadedAd: InterstitialAd) {
                super.onAdLoaded(loadedAd)
                onSuccess.invoke(loadedAd)
                d(TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val error = "domain: ${loadAdError.domain}, code: ${loadAdError.code}, " + "message: ${loadAdError.message}"
                e(TAG, "onAdFailedToLoad() with error $error")
            }

        })
        d(TAG, "Loading interstitial ad")
    }

    fun generateShareIntent(currentLetter: LoveLetter?): Intent {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            currentLetter?.let {
                putExtra(Intent.EXTRA_TEXT, it.text)
            }
            type = "text/plain"
        }
        return Intent.createChooser(shareIntent, null)
    }

    fun openWhatsapp(context: Context, text: String) {
        d(TAG, "Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        val phoneNumber: String = sharedPrefs.getString(context.getString(R.string.pref_key_lover_full_target_phone_number), "")
                ?: ""
        sendWhatsapp.send(context, phoneNumber, text)
    }

    fun newFullScreenContentCallback(onCompletion: () -> Unit): FullScreenContentCallback? {
        return object : FullScreenContentCallback() {

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                super.onAdFailedToShowFullScreenContent(adError)
                onCompletion.invoke()
                d(TAG, "onAdFailedToShowFullScreenContent")
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                d(TAG, "onAdOpened")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                onCompletion.invoke()
                d(TAG, "onAdDismissedFullScreenContent")
            }
        }
    }

    /**
     * Retrieving the next letter from the list of already displayed letters (like Ctrl+Y for letters)
     */
    fun getNextLetterFromDisplayedLettersList(): LoveLetter? {
//        d(TAG, "next letter. displayedLettersList size: ${displayedLettersList.size}")
//        d(TAG, "next letter. currentLetterIndex: $currentLetterIndex")
        var result: LoveLetter? = null
        if (lettersHistoryList.count() > 1) {
            if (lettersHistoryList.getOrNull(currentLetterIndex + 1) != null) {
                result = lettersHistoryList[++currentLetterIndex]
                if (lettersHistoryList.getOrNull(currentLetterIndex + 1) == null) {
                    noNextLetters.postValue(LoveLetterEvent(Unit))
                }

            } else {
                noNextLetters.postValue(LoveLetterEvent(Unit))
            }
        } else {
            noNextLetters.postValue(LoveLetterEvent(Unit))
        }
        return result
    }

    /**
     * Retrieving the previous letter from the list of already displayed letters (like Ctrl+Z for letters)
     */
    fun getPreviousLetterFromDisplayedLettersList(): LoveLetter? {
        d(TAG, "prev letter. displayedLettersList size: ${lettersHistoryList.size}")
        d(TAG, "prev letter. currentLetterIndex: $currentLetterIndex")
        var result: LoveLetter? = null
        if (lettersHistoryList.count() > 1) {
            if (lettersHistoryList.getOrNull(currentLetterIndex - 1) != null) {
                result = lettersHistoryList.getOrNull(--currentLetterIndex)
                if (lettersHistoryList.getOrNull(currentLetterIndex - 1) == null) {
                    noPreviousLetters.postValue(LoveLetterEvent(Unit))
                }
            } else {
                noPreviousLetters.postValue(LoveLetterEvent(Unit))
            }
        } else {
            noPreviousLetters.postValue(LoveLetterEvent(Unit))
        }
        return result
    }

    fun onLetterGenerated(letter: LoveLetter) {
        if (currentLetter?.id == letter.id) {
            notifyRelevantLetterFavouriteStateObservers(letter) //Necessary since the favourite letter's view might still need update
            d(TAG, "No need to update current letter data because it identical to target letter")
            return
        }

        noNextLetters.postValue(LoveLetterEvent(Unit))
//        d(TAG, "onLetterGenerated letter id: ${letter.id}")
//        d(TAG, "onLetterGenerated currentLetter id: ${currentLetter?.id}")
        currentLetter = letter
        if (currentLetterIndex >= 0 && currentLetterIndex + 1 != lettersHistoryList.size && lettersHistoryList.size > 1) {
            lettersHistoryList = lettersHistoryList.subList(0, currentLetterIndex + 1)
        }
        currentLetterIndex = lettersHistoryList.size
        lettersHistoryList.add(currentLetter)
        notifyRelevantDisplayLettersStateObservers()
//        d(TAG, "onLetterGenerated displayedLettersList size: ${displayedLettersList.size}")
//        d(TAG, "onLetterGenerated currentLetterIndex: $currentLetterIndex")
    }

    fun createNewLetter() {
        val newLetter = LoveLetter()
        newLetter.isCreatedByUser = true
        insertLetterToDB(newLetter)
        onLetterPickedForEditing(newLetter)
    }

    fun onLetterPickedForEditing(letter: LoveLetter) {
        d(TAG, "onLetterPickedForEditing")
        clearLettersHistory()
        onLetterGenerated(letter)
    }

    /**
     * Notifies the observers of the display letters list about changes in the list.
     * The observers that will be notified are determined by the list's state.
     */
    fun notifyRelevantDisplayLettersStateObservers() {
//        d(TAG, "requestDisplayedLettersState displayedLettersList size: ${displayedLettersList.size}")
//        d(TAG, "requestDisplayedLettersState currentLetterIndex: $currentLetterIndex")

        Handler(Looper.getMainLooper()).postDelayed(Runnable { //Runnable helps the logic to be putten in its right place in the event loop
            if (getFilteredLetters().size == 1) {
                d(TAG, "Optional letters pool contains only one letter. Clear history as it cannot exists")
                clearLettersHistory()
                return@Runnable
            }

            d(TAG, "currentLetterIndex: $currentLetterIndex")
            d(TAG, "lettersHistoryList size: " + lettersHistoryList.size)
            if (currentLetterIndex + 1 == lettersHistoryList.size) {
                d(TAG, "No next letter found")
                noNextLetters.value = LoveLetterEvent(Unit)
            } else if (lettersHistoryList.count() > 1) {
                d(TAG, "Next letters found")
                nextLettersFound.value = LoveLetterEvent(Unit)
            }

            if (currentLetterIndex + 1 <= 0) {
                d(TAG, "No previous letters found")
                noPreviousLetters.value = LoveLetterEvent(Unit)
            } else if(currentLetterIndex > 0) {
                d(TAG, "Previous letters found")
                previousLettersFound.value = LoveLetterEvent(Unit)
            }

        }, 0)
    }
}
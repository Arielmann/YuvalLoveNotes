package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log.d
import android.util.Log.e
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
    private var displayedLettersList: MutableList<LoveLetter?> = mutableListOf()
    private var currentLetterIndex = -1
    val emptyGeneratedLetterEvent: MutableLiveData<LoveLetterEvent> = MutableLiveData()
    val onEmptyLetterDeleted: MutableLiveData<LoveLetterEvent> = MutableLiveData()
    val noNextLetter: MutableLiveData<LoveLetterEvent> = MutableLiveData()
    val noPreviousLetter: MutableLiveData<LoveLetterEvent> = MutableLiveData()
    val onLetterPickedForEditing: MutableLiveData<LoveLetterEvent> = MutableLiveData()
    var loveLetters: LiveData<MutableList<LoveLetter>?> = MutableLiveData()

    var currentLetter: LoveLetter? = null
        set(value) {
            d(TAG, "New current letter: ${value?.id}")
            if (!value?.id.isNullOrBlank()) {
                sharedPrefs.edit().putString(YuvalLoveNotesApp.context.getString(R.string.pref_key_current_letter_id), value!!.id).commit()
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

    private fun insertLetter(letter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.insertLetter(letter)
        }
    }

    internal fun updateLetter(currentLetter: LoveLetter) {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.updateLetter(currentLetter)
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
            val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                letters.forEach { letter ->
                    loveItemsRepository.deleteLetterSync(letter)
                    displayedLettersList.remove(letter)
                }
                return@async
            }
            waitForDeletion.await()
        }
    }

    fun randomLetter(): LoveLetter {
        val optionalLetters = getFilteredLetters()
        var result = optionalLetters.randomOrNull()
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
     * Deleting a letter if its empty.
     * Returns true if letter was deleted
     */
    internal fun deleteIfEmptyLetter(letter: LoveLetter?): Boolean {
        var isDeleted = false
        letter?.let {
            if (it.text.isBlank()) {
                d(TAG, "deleting empty letter from data base")
                runBlocking {
                    val waitForDeletion = CoroutineScope(Dispatchers.IO).async {
                        loveItemsRepository.deleteLetterSync(letter)
                    }
                    waitForDeletion.await()
                    isDeleted = true
                    onEmptyLetterDeleted.postValue(LoveLetterEvent())
                    displayedLettersList.remove(letter)
                }
            }
        }
//        d(TAG, "love letters list size: " + getFilteredLetters().size)
        return isDeleted
    }

    fun isLoginProcessCompleted(): Boolean {
        val sharedPrefs = get(SharedPreferences::class.java)
        val context = YuvalLoveNotesApp.context
        return sharedPrefs.getBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), false) && sharedPrefs.getBoolean(context.getString(R.string.pref_key_user_registered_in_server), false)
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
        d(TAG, "next letter. displayedLettersList size: ${displayedLettersList.size}")
        d(TAG, "next letter. currentLetterIndex: $currentLetterIndex")
        var result: LoveLetter? = null
        if (displayedLettersList.count() > 1) {
            if (displayedLettersList.getOrNull(currentLetterIndex + 1) != null) {
                result = displayedLettersList[++currentLetterIndex]
                if (displayedLettersList.getOrNull(currentLetterIndex + 1) == null) {
                    noNextLetter.postValue(LoveLetterEvent())
                }

            } else {
                noNextLetter.postValue(LoveLetterEvent())
            }
        } else {
            noNextLetter.postValue(LoveLetterEvent())
        }
        return result
    }

    /**
     * Retrieving the previous letter from the list of already displayed letters (like Ctrl+Z for letters)
     */
    fun getPreviousLetterFromDisplayedLettersList(): LoveLetter? {
        d(TAG, "prev letter. displayedLettersList size: ${displayedLettersList.size}")
        d(TAG, "prev letter. currentLetterIndex: $currentLetterIndex")
        var result: LoveLetter? = null
        if (displayedLettersList.count() > 1) {
            if (displayedLettersList.getOrNull(currentLetterIndex - 1) != null) {
                result = displayedLettersList.getOrNull(--currentLetterIndex)
                if (displayedLettersList.getOrNull(currentLetterIndex - 1) == null) {
                    noPreviousLetter.postValue(LoveLetterEvent())
                }
            } else {
                noPreviousLetter.postValue(LoveLetterEvent())
            }
        } else {
            noPreviousLetter.postValue(LoveLetterEvent())
        }
        return result
    }

    fun onLetterGenerated(letter: LoveLetter) {
        noNextLetter.postValue(LoveLetterEvent())
        d(TAG, "onLetterGenerated letter id: ${letter.id}")
        d(TAG, "onLetterGenerated currentLetter id: ${currentLetter?.id}")
        if (currentLetter?.id == letter.id) {
            d(TAG, "No need to update current letter because it identical to target letter")
            return
        }
        currentLetter = letter
        if (currentLetterIndex >= 0 && currentLetterIndex + 1 != displayedLettersList.size && displayedLettersList.size > 1) {
            displayedLettersList = displayedLettersList.subList(0, currentLetterIndex + 1)
        }
        currentLetterIndex = displayedLettersList.size
        displayedLettersList.add(currentLetter)
        d(TAG, "onLetterGenerated displayedLettersList size: ${displayedLettersList.size}")
        d(TAG, "onLetterGenerated currentLetterIndex: $currentLetterIndex")
    }

    fun createNewLetter() {
        val newLetter = LoveLetter()
        newLetter.isCreatedByUser = true
        insertLetter(newLetter)
        onLetterPickedForEditing(newLetter)
    }

    fun onLetterPickedForEditing(letter: LoveLetter) {
        d(TAG, "onLetterPickedForEditing")
        displayedLettersList.clear()
        currentLetterIndex = -1
        onLetterGenerated(letter)
    }
}
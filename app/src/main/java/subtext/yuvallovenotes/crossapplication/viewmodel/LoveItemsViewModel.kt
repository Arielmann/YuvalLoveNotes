package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log.d
import android.util.Log.e
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import subtext.yuvallovenotes.crossapplication.models.localization.inferLanguageFromLocale
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import subtext.yuvallovenotes.crossapplication.network.NetworkCallback
import subtext.yuvallovenotes.lovelettersgenerator.whatsappsender.WhatsAppSender
import java.lang.ref.WeakReference

class LoveItemsViewModel(context: Context) : ViewModel() {

    companion object {
        private val TAG: String = LoveItemsViewModel::class.simpleName!!
    }

    private val loveItemsRepository: LoveItemsRepository = LoveItemsRepository()
    var loveItemsFromNetwork: MutableList<LoveItem> = mutableListOf()
    private val weakContext: WeakReference<Context> = WeakReference(context)
    private val sharedPrefs: SharedPreferences = get(SharedPreferences::class.java)
    internal var loveLetters: LiveData<MutableList<LoveLetter>> = MutableLiveData()

    init {
        loveLetters = loveItemsRepository.getAllLocalDBLoveLetters()
    }

    fun getFilteredLetters(): List<LoveLetter> {
        var optionalLetters = loveLetters.value?.filter { !it.isArchived && it.text.isNotBlank() }
        val showOnlyLettersCreatedByUser = sharedPrefs.getBoolean(weakContext.get()!!.getString(R.string.pref_key_show_only_letters_created_by_user), false)
        if (showOnlyLettersCreatedByUser) {
            optionalLetters = optionalLetters?.filter { it.isCreatedByUser }
        }
        return optionalLetters ?: mutableListOf()
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

    internal fun requestLoveLettersFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            loveItemsRepository.requestLoveLettersFromServer(inferLanguageFromLocale(), 0, object : NetworkCallback<MutableList<LoveLetter>> {
                override fun onSuccess(response: MutableList<LoveLetter>) {
                    viewModelScope.launch(Dispatchers.IO) {
                        loveItemsRepository.insertAllLoveLetters(response)
                    }
                    sharedPrefs.edit().putBoolean(YuvalLoveNotesApp.context.getString(R.string.pref_key_server_letters_downloaded_after_app_installed), true).apply()
                }

                override fun onFailure(message: String) {
                    e(TAG, "Error while downloading letters from server: $message")
                }

            })
        }
    }

    internal fun getLetterById(id: String): LiveData<LoveLetter> {
        return loveItemsRepository.getLoveLetterById(id)
    }

    internal fun insertLetter(letter: LoveLetter) {
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
                }
                return@async
            }
            waitForDeletion.await()
        }
    }

    internal fun randomLetter(): LoveLetter {
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

    /**
     * Deleting a letter if its empty.
     * Returns true if letter was deleted
     */
    internal fun deleteLetterIfEmpty(letter: LoveLetter?): Boolean {
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
}
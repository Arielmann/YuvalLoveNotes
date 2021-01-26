package subtext.yuvallovenotes.crossapplication.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log.d
import android.util.Log.e
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
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
    private lateinit var interstitialAd: InterstitialAd
    internal var loveLetters: LiveData<MutableList<LoveLetter>>

    init {
        d(TAG, "Getting all love letter from local database")
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
        val optionalLetters = loveLetters.value?.filter { !it.isArchived }
        val showOnlyLettersCreatedByUser = sharedPrefs.getBoolean(weakContext.get()!!.getString(R.string.pref_key_show_only_letters_created_by_user), false)
        var result = optionalLetters?.randomOrNull()
        if (showOnlyLettersCreatedByUser) {
            val lettersCreatedByUser = optionalLetters?.filter { !it.isArchived && it.isCreatedByUser }
            result = lettersCreatedByUser?.randomOrNull()
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

    fun getUnarchivedLetters(): List<LoveLetter>? {
        return loveLetters.value?.filter { !it.isArchived }
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
        d(TAG, "love letters list size: " + loveLetters.value?.size)
        return isDeleted
    }

    fun isLoginProcessCompleted(): Boolean {
        val sharedPrefs = get(SharedPreferences::class.java)
        val context = YuvalLoveNotesApp.context
        return sharedPrefs.getBoolean(context.getString(R.string.pref_key_device_registered_to_push_notifications), false) && sharedPrefs.getBoolean(context.getString(R.string.pref_key_user_registered_in_server), false)
    }

    /*Todo: Cleanup*/
    fun generateNativeAd(context: Context, onSuccess: (ad: UnifiedNativeAd) -> Unit) {
        val testAdId = "ca-app-pub-3940256099942544/2247696110"
        val adLoader = AdLoader.Builder(context, testAdId)
                .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                    e(TAG, "Ad loaded: $ad")
                    onSuccess.invoke(ad)
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        e(TAG, "Ad loading failure: $adError")
                    }
                }).withNativeAdOptions(NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadInterstitialAd(context: Context, onSuccess: (InterstitialAd) -> Unit) {
        // Create the InterstitialAd and set it up.
        val unitId = BuildConfig.INTERSTITIAL_ADS_ID
//      val unitId = context.getString(R.string.interstitial_ads_test_id)
        interstitialAd = InterstitialAd(context).apply {
            adUnitId = unitId
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    d(TAG, "onAdLoaded")
                    onSuccess.invoke(interstitialAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val error = "domain: ${loadAdError.domain}, code: ${loadAdError.code}, " + "message: ${loadAdError.message}"
                    e(TAG, "onAdFailedToLoad() with error $error")
                }

            })
        }
        d(TAG, "Loading interstitial ad")
        loadNewAd()
    }

    fun loadNewAd() {
        val adRequest = AdRequest.Builder().build()
        interstitialAd.loadAd(adRequest)
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
        val phoneNumber: String = sharedPrefs.getString(context.getString(R.string.pref_key_lover_full_target_phone_number), "") ?: ""
        sendWhatsapp.send(context, phoneNumber, text)
    }
}
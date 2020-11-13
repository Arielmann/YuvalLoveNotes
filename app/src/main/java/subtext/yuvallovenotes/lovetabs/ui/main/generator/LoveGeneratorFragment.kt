package subtext.yuvallovenotes.lovetabs.ui.main.generator

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_generator_tab.*
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp.Companion.APP_TAG
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import subtext.yuvallovenotes.lovetabs.viewmodel.LoveViewModel
import subtext.yuvallovenotes.whatsapp.WhatsAppSender

class LoveGeneratorFragment : Fragment() {

    companion object {

        private val TAG = LoveGeneratorFragment::class.simpleName

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): LoveGeneratorFragment {
            return LoveGeneratorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private var loveViewModel: LoveViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_generator_tab, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataUpdates()
        setOnClickListeners()
        loveLetterEditText.movementMethod = ScrollingMovementMethod()
    }

    override fun onStart() {
        super.onStart()
       /* if (loveViewModel.loveItemsFromNetwork.isEmpty()) {
            loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        }*/
    }

    private fun observeDataUpdates() {
        loveViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            // Update the cached copy of the lovePhrases in the adapter.
            if (areAvailable) {
                Log.d(TAG, "Love items available")
                loveLetterEditText.setText(loveViewModel.newLetter())
            }
        })
    }

    private val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {
            println("$APP_TAG server LoveItems response: $response")

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            loveViewModel.loveItemsFromNetwork = response.toMutableList()

            this@LoveGeneratorFragment.activity?.runOnUiThread {
                val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
                val phrasesForThisLetter: List<LovePhrase> = allPhrases.subList(0, loveViewModel.lovePhrasesAmountInLetter(allPhrases))
                val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
                val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()
                loveLetterEditText.setText(loveViewModel.newLetter(openers, phrasesForThisLetter, closures))
            }
        }

        override fun handleFault(fault: BackendlessFault?) {
            println("$APP_TAG Backendless error: ${fault.toString()}")
            Toast.makeText(context, fault.toString(), Toast.LENGTH_LONG).show()
        }

    }

    private val loveGeneratorListener: View.OnClickListener = View.OnClickListener {
        println("$TAG Generate button clicked. Generating love letter")
//        loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        loveLetterEditText.setText(loveViewModel.newLetter())
    }

    private val loveSendListener: View.OnClickListener = View.OnClickListener {
        println("$APP_TAG Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val phoneNumber: String = prefs.getString(getString(R.string.pref_key_target_phone_number), BuildConfig.MOBILE_NUMBER)!!.ifBlank {
            BuildConfig.MOBILE_NUMBER
        }
        sendWhatsapp.send(context, phoneNumber, loveLetterEditText.text.toString())
    }

    private fun setOnClickListeners() {
        loveLetterGeneratorBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        loveLetterGeneratorBtn.setOnClickListener(loveGeneratorListener)
        loveLetterSendBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        loveLetterSendBtn.setOnClickListener(loveSendListener)
    }
}


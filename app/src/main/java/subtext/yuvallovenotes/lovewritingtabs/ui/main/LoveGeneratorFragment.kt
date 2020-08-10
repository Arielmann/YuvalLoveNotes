package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_generator_tab.*
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp.Companion.LOG_TAG
import subtext.yuvallovenotes.loveletters.LoveClosure
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.loveletters.LovePhrase
import subtext.yuvallovenotes.whatsapp.WhatsAppSender


@ExperimentalStdlibApi
class LoveGeneratorFragment() : Fragment() {

    private lateinit var pageViewModelProvider: ViewModelProvider
    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModelProvider = ViewModelProvider(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_generator_tab, container, false)
        pageViewModel = pageViewModelProvider.get(PageViewModel::class.java).apply {
            setLoveNotesBackendless(context)
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        pageViewModel.loveNotesBackendless.findAllLoveData(findAllLoveDataBackendlessListener)
        setOnClickListeners();
        loveLetterTV.movementMethod = ScrollingMovementMethod()
    }

    private val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {
            println("$LOG_TAG server LoveItems response: $response")

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            pageViewModel.loveItems = response.toMutableList()

            this@LoveGeneratorFragment.activity?.runOnUiThread {
                var lastIndex = 3
                val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
                if (allPhrases.getOrNull(lastIndex) == null) {
                    lastIndex = allPhrases.size
                }
                val phrasesForThisLetter: List<LovePhrase> = allPhrases.subList(0, lastIndex)
                val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
                val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()

                var text = ""

                if(!openers.isNullOrEmpty()) {
                    text = text.plus(openers.randomOrNull()?.text + "\n\n")
                }
                phrasesForThisLetter.forEach { phrase ->
                    text = text.plus(phrase.text + "\n\n")

                }

                if(!closures.isNullOrEmpty()) {
                    text = text.plus(closures.random().text)
                }

                loveLetterTV.text = text
            }
        }

        override fun handleFault(fault: BackendlessFault?) {
            println("$LOG_TAG Backendless error: ${fault.toString()}")
            Toast.makeText(context, fault.toString(), Toast.LENGTH_LONG).show()
        }

    }
    private val loveGeneratorListener: View.OnClickListener = View.OnClickListener {
        println("$LOG_TAG Generate button clicked. Generating love letter")
        pageViewModel.loveNotesBackendless.findAllLoveData(findAllLoveDataBackendlessListener)
    }

    private val loveSendListener: View.OnClickListener = View.OnClickListener {
        println("$LOG_TAG Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        sendWhatsapp.send(context, BuildConfig.MOBILE_NUMBER, loveLetterTV.text.toString())
    }

    private fun setOnClickListeners() {
        loveLetterGeneratorBtn.setOnClickListener(loveGeneratorListener)
        loveLetterSendBtn.setOnClickListener(loveSendListener)
    }

    companion object {
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
}


package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_love_writer_tab.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveletters.LoveClosure
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.loveletters.LovePhrase

/**
 * A placeholder fragment containing a simple view.
 */
class LoveWriterFragment : Fragment() {

    private lateinit var pageViewModelProvider: ViewModelProvider
    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModelProvider = ViewModelProvider(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pageViewModel = pageViewModelProvider.get(PageViewModel::class.java).apply {
            setLoveNotesBackendless(context)
        }
        return inflater.inflate(R.layout.fragment_love_writer_tab, container, false)
    }

    override fun onStart() {
        super.onStart()
        setOnClickListeners();
    }

    private fun setOnClickListeners() {
        loveOpenerSenderBtn.setOnClickListener {
            val opener = LoveOpener();
            opener.text = loveOpenerEditText.text.toString()
            pageViewModel.loveNotesBackendless.saveLoveOpener(opener)
        }

        lovePhraseSendBtn.setOnClickListener {
            val phrase = LovePhrase();
            phrase.text = lovePhraseEditText.text.toString()
            pageViewModel.loveNotesBackendless.saveLovePhrase(phrase)
        }

        loveClosureSendBtn.setOnClickListener {
            val closure = LoveClosure();
            closure.text = loveClosureEditText.text.toString()
            pageViewModel.loveNotesBackendless.saveLoveClosure(closure)
        }
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
        fun newInstance(sectionNumber: Int): LoveWriterFragment {
            return LoveWriterFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
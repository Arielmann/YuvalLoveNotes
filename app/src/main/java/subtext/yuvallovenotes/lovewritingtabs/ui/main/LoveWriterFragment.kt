package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_love_writer_tab.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.backendless.LoveNotesBackendless
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
        pageViewModel = pageViewModelProvider.get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_writer_tab, container, false)
        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
        })
        return root
    }

    override fun onStart() {
        super.onStart()
        setOnClickListeners();
    }

    private fun setOnClickListeners() {
        loveOpenerSenderBtn.setOnClickListener {
            val opener = LoveOpener();
            opener.text = loveOpenerEditText.text.toString()
            context?.let { it1 -> LoveNotesBackendless.saveLoveOpener(it1, opener) }
        }

        lovePhraseSendBtn.setOnClickListener {
            val phrase = LovePhrase();
            phrase.text = lovePhraseEditText.text.toString()
            context?.let { it1 -> LoveNotesBackendless.saveLovePhrase(it1, phrase) }
        }

        loveClosureSendBtn.setOnClickListener {
            val closure = LoveClosure();
            closure.text = loveClosureEditText.text.toString()
            context?.let { it1 -> LoveNotesBackendless.saveLoveClosure(it1, closure) }
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
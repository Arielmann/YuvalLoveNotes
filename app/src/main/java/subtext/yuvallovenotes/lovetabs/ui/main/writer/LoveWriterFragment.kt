package subtext.yuvallovenotes.lovetabs.ui.main.writer

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_writer_tab.*
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import subtext.yuvallovenotes.lovetabs.viewmodel.LoveViewModel

class LoveWriterFragment : Fragment() {


    companion object {
        private val TAG: String = LoveWriterFragment::class.simpleName!!

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

    private var loveViewModel: LoveViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_love_writer_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeDataUpdates()
        setOnClickListeners()
        loveOpenerEditText.movementMethod = ScrollingMovementMethod()
        lovePhraseEditText.movementMethod = ScrollingMovementMethod()
        loveClosureEditText.movementMethod = ScrollingMovementMethod()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setOnClickListeners() {
        loveOpenerSenderBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        loveOpenerSenderBtn.setOnClickListener {
            loveOpenerSenderBtn.isClickable = false
            val opener = LoveOpener()
            opener.text = loveOpenerEditText.text.toString()
            loveViewModel.loveNetworkCalls.save(opener, object : AsyncCallback<LoveOpener> {
                override fun handleResponse(response: LoveOpener?) {
                    loveOpenerSenderBtn.isClickable = true
                    println("Backendless response ${response.toString()}")
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    loveOpenerEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    loveOpenerSenderBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }

        lovePhraseSendBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        lovePhraseSendBtn.setOnClickListener {
            lovePhraseSendBtn.isClickable = false
            val phrase = LovePhrase()
            phrase.text = lovePhraseEditText.text.toString()
            loveViewModel.loveNetworkCalls.save(phrase, object : AsyncCallback<LovePhrase> {
                override fun handleResponse(response: LovePhrase?) {
                    lovePhraseSendBtn.isClickable = true
                    println("Backendless response ${response.toString()}")
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    lovePhraseEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    lovePhraseSendBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }

        loveClosureSendBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        loveClosureSendBtn.setOnClickListener {
            loveClosureSendBtn.isClickable = false
            val closure = LoveClosure()
            closure.text = loveClosureEditText.text.toString()
            loveViewModel.loveNetworkCalls.save(closure, object : AsyncCallback<LoveClosure> {
                override fun handleResponse(response: LoveClosure?) {
                    loveClosureSendBtn.isClickable = true
                    println("Backendless response ${response.toString()}")
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    loveClosureEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    loveClosureSendBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }
    }

    private fun observeDataUpdates() {
        loveViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            // Update the cached copy of the lovePhrases in the adapter.
            if (areAvailable) {
                Log.d(TAG, "Love items available")

            }
        })
    }
}
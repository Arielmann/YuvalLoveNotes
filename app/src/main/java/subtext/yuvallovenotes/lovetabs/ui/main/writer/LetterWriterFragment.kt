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
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentLetterWriterBinding
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import subtext.yuvallovenotes.lovetabs.viewmodel.LoveItemsViewModel

class LetterWriterFragment : Fragment() {


    companion object {
        private val TAG: String = LetterWriterFragment::class.simpleName!!
    }

    private lateinit var binding: FragmentLetterWriterBinding
    private var loveItemsViewModel: LoveItemsViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterWriterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeDataUpdates()
        setOnClickListeners()
        binding.loveOpenerEditText.movementMethod = ScrollingMovementMethod()
        binding.lovePhraseEditText.movementMethod = ScrollingMovementMethod()
        binding.loveClosureEditText.movementMethod = ScrollingMovementMethod()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setOnClickListeners() {
       binding.loveOpenerSenderBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
       binding.loveOpenerSenderBtn.setOnClickListener {
           binding.loveOpenerSenderBtn.isClickable = false
            val opener = LoveOpener()
            opener.text = binding.loveOpenerEditText.text.toString()
            loveItemsViewModel.loveNetworkCalls.save(opener, object : AsyncCallback<LoveOpener> {
                override fun handleResponse(response: LoveOpener?) {
                    binding.loveOpenerSenderBtn.isClickable = true
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    binding.loveOpenerEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    binding.loveOpenerSenderBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }

        binding.lovePhraseSendBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        binding.lovePhraseSendBtn.setOnClickListener {
            binding.lovePhraseSendBtn.isClickable = false
            val phrase = LovePhrase()
            phrase.text = binding.lovePhraseEditText.text.toString()
            loveItemsViewModel.loveNetworkCalls.save(phrase, object : AsyncCallback<LovePhrase> {
                override fun handleResponse(response: LovePhrase?) {
                    binding.lovePhraseSendBtn.isClickable = true
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    binding.lovePhraseEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    binding.lovePhraseSendBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }

        binding.loveClosureSendBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        binding.loveClosureSendBtn.setOnClickListener {
            binding.loveClosureSendBtn.isClickable = false
            val closure = LoveClosure()
            closure.text = binding.loveClosureEditText.text.toString()
            loveItemsViewModel.loveNetworkCalls.save(closure, object : AsyncCallback<LoveClosure> {
                override fun handleResponse(response: LoveClosure?) {
                    binding.loveClosureSendBtn.isClickable = true
                    Toast.makeText(requireContext(), R.string.item_save_success_message_title, Toast.LENGTH_LONG).show()
                    binding.loveClosureEditText.text.clear()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    binding.loveClosureSendBtn.isClickable = true
                    println("Backendless error ${fault.toString()}")
                }
            })
        }
    }

    private fun observeDataUpdates() {
        loveItemsViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            // Update the cached copy of the lovePhrases in the adapter.
            if (areAvailable) {
                Log.d(TAG, "Love items available")
            }
        })
    }
}
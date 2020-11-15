package subtext.yuvallovenotes.lovetabs.ui.main.generator

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import subtext.yuvallovenotes.lovetabs.viewmodel.LetterViewModel
import subtext.yuvallovenotes.whatsapp.WhatsAppSender

class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
    }

    private lateinit var binding: FragmentLetterGeneratorBinding
    private var letterViewModel: LetterViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataUpdates()
        setOnClickListeners()
//        binding.loveLetterEditText.movementMethod = ScrollingMovementMethod()
    }

    override fun onStart() {
        super.onStart()
         if (letterViewModel.loveItemsFromNetwork.isEmpty()) {
             letterViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
         }
    }

    private fun observeDataUpdates() {
        letterViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            // Update the cached copy of the lovePhrases in the adapter.
            if (areAvailable) {
                d(TAG,"Love items available")
                binding.letterEditText.setText(letterViewModel.newLetterText())
            }
        })
    }

    private val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {
            println("$TAG server LoveItems response: $response")

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            letterViewModel.loveItemsFromNetwork = response.toMutableList()

            this@LetterGeneratorFragment.activity?.runOnUiThread {
                val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
                letterViewModel.insertAllLovePhrases(allPhrases)
                val phrasesForThisLetter: List<LovePhrase> = allPhrases.subList(0, letterViewModel.lovePhrasesAmountInLetter(allPhrases))
                val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
                letterViewModel.insertAllLoveOpeners(openers)
                val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()
                letterViewModel.insertAllLoveClosures(closures)
                binding.letterEditText.setText(letterViewModel.newLetterText(openers, phrasesForThisLetter, closures))
            }
        }

        override fun handleFault(fault: BackendlessFault?) {
            d(TAG,"Backendless error: ${fault.toString()}")
            Toast.makeText(requireContext(), fault.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private val letterGeneratorListener: View.OnClickListener = View.OnClickListener {
        d(TAG,"Generate button clicked. Generating love letter")
//        loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        binding.letterEditText.setText(letterViewModel.newLetterText())
    }

    private val letterSendListener: View.OnClickListener = View.OnClickListener {
        d(TAG,"Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val phoneNumber: String = prefs.getString(getString(R.string.pref_key_target_phone_number), BuildConfig.MOBILE_NUMBER)!!.ifBlank {
            BuildConfig.MOBILE_NUMBER
        }
        sendWhatsapp.send(requireContext(), phoneNumber, binding.letterEditText.text.toString())
    }

    private fun setOnClickListeners() {
        //Todo: newLetterBtn click bug: sometimes not returning to alpha 1 mode (standard mode before click)
        binding.newLetterBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.whatsappShareBtn.setOnTouchListener(LoveUtils.newOnTouchListenerWithViewClickUIBehaviour())
        binding.whatsappShareBtn.setOnClickListener(letterSendListener)
    }
}


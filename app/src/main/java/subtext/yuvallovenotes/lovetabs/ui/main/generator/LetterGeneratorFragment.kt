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
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.loveitems.LoveClosure
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.loveitems.LoveOpener
import subtext.yuvallovenotes.loveitems.LovePhrase
import subtext.yuvallovenotes.lovetabs.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.whatsapp.WhatsAppSender

class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
    }

    private lateinit var binding: FragmentLetterGeneratorBinding
    private var loveItemsViewModel: LoveItemsViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataUpdates()
        setOnClickListeners()
    }

    override fun onStart() {
        super.onStart()
        //Todo: remove, code should depend on local db
       /* if (loveItemsViewModel.loveItemsFromNetwork.isEmpty()) {
            loveItemsViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        }*/
    }

    private fun observeDataUpdates() {
        loveItemsViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            if (areAvailable) {
                loveItemsViewModel.loveItems.observeOnce(viewLifecycleOwner, { results ->
                    d(TAG, "Love items available: $results")
                    d(TAG, "Love items size: ${results?.size}")
                    loveItemsViewModel.populateLoveLettersList()
                    binding.letterEditText.setText(loveItemsViewModel.randomLetter()?.text)
                })
            }
        })
    }

    private val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            loveItemsViewModel.loveItemsFromNetwork = response.toMutableList()

            this@LetterGeneratorFragment.activity?.runOnUiThread {
                val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
                loveItemsViewModel.insertAllLovePhrases(allPhrases)
                val phrasesForThisLetter: List<LovePhrase> = allPhrases.subList(0, loveItemsViewModel.lovePhrasesAmountInLetter(allPhrases))
                val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
                loveItemsViewModel.insertAllLoveOpeners(openers)
                val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()
                loveItemsViewModel.insertAllLoveClosures(closures)
                binding.letterEditText.setText(loveItemsViewModel.generateRandomLetter(openers, phrasesForThisLetter, closures))
            }
        }

        override fun handleFault(fault: BackendlessFault?) {
            d(TAG, "Backendless error: ${fault.toString()}")
            Toast.makeText(requireContext(), fault.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private val letterGeneratorListener: View.OnClickListener = View.OnClickListener {
        //Todo: cleanup
//        loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        binding.letterEditText.setText(loveItemsViewModel.randomLetter()?.text)
    }

    private val letterSendListener: View.OnClickListener = View.OnClickListener {
        d(TAG, "Opening Whatsapp")
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


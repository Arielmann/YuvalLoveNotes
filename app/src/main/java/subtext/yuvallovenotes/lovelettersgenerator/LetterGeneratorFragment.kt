package subtext.yuvallovenotes.lovelettersgenerator

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log.d
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.*
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.whatsapp.WhatsAppSender


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
    }

    private var currentLetter: LoveLetter? = null
    private lateinit var binding: FragmentLetterGeneratorBinding
    private var loveItemsViewModel: LoveItemsViewModel = get()

    private val onLetterTextChanged: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(newText: android.text.Editable?) {
            this@LetterGeneratorFragment.currentLetter?.let {
                d(TAG, "updating love letter")
                it.text = newText.toString()
                loveItemsViewModel.updateLetter(it)
            } ?: d(TAG, "no current letter to update")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayLetterData()
        setOnClickListeners()
    }

    //todo: cleanup
    override fun onStart() {
        super.onStart()
        //Todo: cleanup, code should depend on local db
        /* if (loveItemsViewModel.loveItemsFromNetwork.isEmpty()) {
             loveItemsViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
         }*/
    }

    private fun displayLetterData() {
        val args: LetterGeneratorFragmentArgs by navArgs()
        loveItemsViewModel.getLetterById(args.StringLetterId) //Checking if specific letter should be displayed
                .observeOnce(viewLifecycleOwner, { letter ->
                    letter?.let {
                        d(TAG, "found letter by id: $args.StringLetterId")
                        createRandomLettersData()
                        this.currentLetter = letter
                        binding.letterEditText.setText(letter.text)
                        binding.letterEditText.addTextChangedListener(onLetterTextChanged)
                    } ?: createRandomLettersData {
                        d(TAG, "No specific letter, generate random one and set its text")
                        this.currentLetter = loveItemsViewModel.randomLetter()
                        d(TAG, "generated letter text: ${currentLetter?.text}")
                        binding.letterEditText.setText(this.currentLetter?.text)
                        binding.letterEditText.addTextChangedListener(onLetterTextChanged)
                    }
                })
    }

    private fun createRandomLettersData(onCompletion: () -> Unit = {}) {

        loveItemsViewModel.loveLetters.observeOnce(viewLifecycleOwner, { letters ->
            d(TAG, "Love items size: ${letters?.size}")
            onCompletion.invoke()
        })
/*        val observer: Observer<Boolean> = object : Observer<Boolean> {
            override fun onChanged(areAvailable: Boolean) {
                d(TAG, "Are love items available: $areAvailable")
                if (areAvailable) {
                    loveItemsViewModel.areLoveItemsAvailable.removeObserver(this)
                    loveItemsViewModel.populateLettersList()
                }
            }

        }

        loveItemsViewModel.areLoveItemsAvailable.observe(viewLifecycleOwner, observer)*/
    }

    //todo: cleanup
    private val findAllLoveDataBackendlessListener = object : AsyncCallback<List<LoveItem>> {
        override fun handleResponse(response: List<LoveItem>?) {

            if (response.isNullOrEmpty()) {
                handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                return
            }
            loveItemsViewModel.loveItemsFromNetwork = response.toMutableList()

            this@LetterGeneratorFragment.activity?.runOnUiThread {
                val allPhrases: List<LovePhrase> = response.filterIsInstance<LovePhrase>().shuffled()
                loveItemsViewModel.insertAllPhrases(allPhrases)
                val openers: List<LoveOpener> = response.filterIsInstance<LoveOpener>()
                loveItemsViewModel.insertAllOpeners(openers)
                val closures: List<LoveClosure> = response.filterIsInstance<LoveClosure>()
                loveItemsViewModel.insertAllClosures(closures)
//                val tempLetter = loveItemsViewModel.generateRandomLetterText(openers, phrasesForThisLetter, closures)
//                binding.letterEditText.setText(tempLetter)
            }
        }

        override fun handleFault(fault: BackendlessFault?) {
            d(TAG, "Backendless error: ${fault.toString()}")
            Toast.makeText(requireContext(), fault.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private val goToLettersListListener: View.OnClickListener = View.OnClickListener {
        findNavController().navigate(R.id.navigate_to_letter_generator)
    }

    private val letterGeneratorListener: View.OnClickListener = View.OnClickListener {
        //Todo: cleanup
//        loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        binding.letterEditText.removeTextChangedListener(onLetterTextChanged)
        binding.letterEditText.setText(loveItemsViewModel.randomLetter()?.text)
        binding.letterEditText.addTextChangedListener(onLetterTextChanged)
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
        binding.goToLettersListBtn.setOnClickListener { findNavController().navigate(R.id.navigate_to_letter_list) }
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.whatsappShareBtn.setOnClickListener(letterSendListener)
    }
}


package subtext.yuvallovenotes.lovelettersgenerator

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.AlignmentSpan
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.sun.mail.imap.protocol.FetchResponse.getItem
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.whatsapp.WhatsAppSender


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
        private val GENERATOR_SPINNER_TAG = "$TAG GENERATOR_SPINNER_TAG"
    }

    private var spinnerItemSelectionAllowed: Boolean = false
    private var currentLetter: LoveLetter? = null
    private lateinit var binding: FragmentLetterGeneratorBinding
    private var loveItemsViewModel: LoveItemsViewModel = get()

    private val onLetterTextChanged: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(newText: android.text.Editable?) {
            this@LetterGeneratorFragment.currentLetter?.let {
                it.text = newText.toString()
                if (!newText.toString().isBlank()) {
                    loveItemsViewModel.updateLetter(it)
                    d(TAG, "updating love letter")
                }
            } ?: d(TAG, "no current letter to update")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        displayLetterData()
        setButtonsOnClickListeners()
    }

    private fun setupToolbar() {
        binding.letterGeneratorToolBar.inflateMenu(R.menu.letter_generator_menu);

        binding.letterGeneratorToolBar.setOnMenuItemClickListener { item -> // Handle item selection
            when (item?.itemId) {

                R.id.menuActionSettings -> {
                    navigateToSettingsActivity()
                    true
                }

                R.id.menuActionShare -> {
                    showSharingPopup()
                    true
                }

                R.id.menuActionGoToLettersList -> {
                    onNavigationToLettersListRequsted()
                    true
                }

                else -> false
            }
        }
    }


    //todo: cleanup
    override fun onStart() {
        super.onStart()
        d(TAG, "onStart. ignore first spinner auto click")
//        binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
        spinnerItemSelectionAllowed = false
        //Todo: cleanup, code should depend on local db
        /* if (loveItemsViewModel.loveItemsFromNetwork.isEmpty()) {
             loveItemsViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener.findAllLoveDataBackendlessListener)
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
    }

    private val letterGeneratorListener: View.OnClickListener = View.OnClickListener {
        //Todo: cleanup
//        loveViewModel.loveNetworkCalls.findAllLoveData(findAllLoveDataBackendlessListener)
        binding.letterEditText.removeTextChangedListener(onLetterTextChanged)
        binding.letterEditText.setText(loveItemsViewModel.randomLetter()?.text)
        binding.letterEditText.addTextChangedListener(onLetterTextChanged)
    }

    private val letterListNavigationRequiredClickListener: View.OnClickListener = View.OnClickListener {
        onNavigationToLettersListRequsted()
    }

    private fun onNavigationToLettersListRequsted() {
        currentLetter?.let {
            if (it.text.isBlank() && it.isCreatedByUser) {
                d(TAG, "deleting empty letter from data base if exists")
                Toast.makeText(requireContext(), getString(R.string.title_empty_letter_deleted), LENGTH_LONG).show()
                loveItemsViewModel.deleteLetter(it)
            }
        }
        findNavController().navigate(R.id.navigate_to_letter_list)
    }

    private val letterSendListener: View.OnClickListener = View.OnClickListener {
        d(TAG, "Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        //Todo: set mobile number to yuval's only under my id (also set my custom letters)
        val phoneNumber: String = prefs.getString(getString(R.string.pref_key_full_target_phone_number), "") ?: ""
        sendWhatsapp.send(requireContext(), phoneNumber, binding.letterEditText.text.toString())
    }

    private fun setButtonsOnClickListeners() {
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.whatsappShareBtn.setOnClickListener(letterSendListener)
    }


    private val openSpinnerClickListener: View.OnClickListener = View.OnClickListener {
        spinnerItemSelectionAllowed = true //User clicked - allow spinner clicks
    }

   /* private fun setMoreOptionsSpinner() {
        d(GENERATOR_SPINNER_TAG, "setMoreOptionsSpinner called")
        val itemsArrayResID = R.array.more_options_array
        if (binding.letterGeneratorMoreOptionsSpinner.adapter == null) {
            d(GENERATOR_SPINNER_TAG, "setting more options spinner adapter")
            ArrayAdapter.createFromResource(requireContext(), itemsArrayResID, android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item_layout)
                binding.letterGeneratorMoreOptionsSpinner.adapter = adapter
            }

            binding.letterGeneratorMoreOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    d(GENERATOR_SPINNER_TAG, "onItemSelected at position " + binding.letterGeneratorMoreOptionsSpinner.selectedItemPosition)
                    if (parent.getChildAt(0) is TextView) {
                        (parent.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                    }
                    val items = resources.getStringArray(itemsArrayResID).map { n -> n.toLowerCase(Locale.getDefault()) }
                    if (spinnerItemSelectionAllowed) {
                        when (parent.getItemAtPosition(pos).toString().toLowerCase(Locale.getDefault())) {
                            items[0] -> {
                                showSharingPopup()
                            }

                            items[1] -> {
                                navigateToSettingsActivity()
                            }
                        }
                    } else {
                        d(GENERATOR_SPINNER_TAG, "System tried to force spinner item click. ignoring...")
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    d(GENERATOR_SPINNER_TAG, "More options spinner: nothing selected")
                    binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
                }
            }
        }
    }*/

    private fun navigateToSettingsActivity() {
        d(TAG, "Navigating to settings")
        spinnerItemSelectionAllowed = false
        findNavController().navigate(R.id.navigate_to_settings)
    }

    private fun showSharingPopup() {
        d(TAG, "Sharing letter in general sharing options")
        spinnerItemSelectionAllowed = false
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Send your letter")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}


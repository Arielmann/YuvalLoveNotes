package subtext.yuvallovenotes.lovelettersgenerator

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log.d
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.more_options_dialog.view.*
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.whatsapp.WhatsAppSender
import java.util.*


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
        private val GENERATOR_SPINNER_TAG = "$TAG GENERATOR_SPINNER_TAG"
    }

    private var systemForcesSpinnerAutoClick: Boolean = true
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
        displayLetterData()
//        setMoreOptionsSpinner()
        setButtonsOnClickListeners()
    }

    //todo: cleanup
    override fun onStart() {
        super.onStart()
        d(TAG, "onStart. ignore first spinner auto click")
        binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
        systemForcesSpinnerAutoClick = true
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
        currentLetter?.let {
            if (it.text.isBlank() && it.isCreatedByUser) {
                d(TAG, "deleting empty letter from data base if exists")
                Toast.makeText(requireContext(), getString(R.string.title_item_deleted), LENGTH_LONG).show()
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
        val phoneNumber: String = prefs.getString(getString(R.string.pref_key_target_phone_number), BuildConfig.MOBILE_NUMBER)!!.ifBlank {
            BuildConfig.MOBILE_NUMBER
        }
        sendWhatsapp.send(requireContext(), phoneNumber, binding.letterEditText.text.toString())
    }

    private fun setButtonsOnClickListeners() {
        binding.goToLettersListBtn.setOnClickListener(letterListNavigationRequiredClickListener)
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.whatsappShareBtn.setOnClickListener(letterSendListener)
        binding.letterSpinnerOpenButton.setOnClickListener(openSpinnerClickListener)
    }


    private val openSpinnerClickListener: View.OnClickListener = View.OnClickListener {
//        systemForcesSpinnerAutoClick = true
        showMessageBox("")
//        setMoreOptionsSpinner()
//        binding.letterGeneratorMoreOptionsSpinner.performClick()
//        binding.letterGeneratorMoreOptionsSpinner.visibility = View.VISIBLE
    }

    private fun setIdleSpinnerAdapter() {
        d(GENERATOR_SPINNER_TAG, "setting idle adapter")
//        var items = arrayOf("", "Share", "Settings")
        val itemsArrayResID = R.array.empty_spinner_array
//        binding.letterGeneratorMoreOptionsSpinner.setSelection(0, false)
//        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items)
//        adapter.setDropDownViewResource(R.layout.spinner_item_layout)
//        binding.letterGeneratorMoreOptionsSpinner.adapter = adapter
        ArrayAdapter.createFromResource(requireContext(), itemsArrayResID, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item_layout)
            binding.letterGeneratorMoreOptionsSpinner.adapter = adapter
            binding.letterGeneratorMoreOptionsSpinner.setSelection(2)
        }
    }

    fun showMessageBox(text: String){

        //Inflate the dialog as custom view
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.more_options_dialog, null)

        //AlertDialogBuilder
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)

        //setting text values
        messageBoxView.shareBtn.text = "Share"
        messageBoxView.message_box_content.text = "Settings"

        //show dialog
        val messageBoxInstance = messageBoxBuilder.create()
        messageBoxInstance.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp: WindowManager.LayoutParams = messageBoxInstance.window!!.attributes
        wmlp.dimAmount = 0.0f //No dim
        wmlp.gravity = Gravity.TOP or Gravity.LEFT
//        wmlp.x = 100 //x position

//        wmlp.y = 100 //y position
        messageBoxInstance.show()

        //set Listener
        messageBoxView.setOnClickListener(){
            //close dialog
            messageBoxInstance.dismiss()
        }
    }

    private fun setMoreOptionsSpinner() {
        d(GENERATOR_SPINNER_TAG, "setMoreOptionsSpinner")
//        var items = arrayOf("", "Share", "Settings")
        val itemsArrayResID = R.array.more_options_array
//        binding.letterGeneratorMoreOptionsSpinner.setSelection(0, false)
//        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items)
//        adapter.setDropDownViewResource(R.layout.spinner_item_layout)
//        binding.letterGeneratorMoreOptionsSpinner.adapter = adapter
        if (binding.letterGeneratorMoreOptionsSpinner.adapter == null) {
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
//                    if (systemForcesSpinnerAutoClick) {
//                        d(GENERATOR_SPINNER_TAG, "System forcing spinner first item click for first time. ignoring...")
//                        systemForcesSpinnerAutoClick = false
//                    } else {
                        when ( parent.getItemAtPosition(pos).toString().toLowerCase(Locale.getDefault())) {
                            items[0] -> {
                                d(GENERATOR_SPINNER_TAG, "Sharing letter in general sharing options")
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Send your letter")
                                    type = "text/plain"
                                    binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                startActivity(shareIntent)
                                setIdleSpinnerAdapter()
                            }

                            items[1] -> {
                                d(GENERATOR_SPINNER_TAG, "Settings clicked")
//                            findNavController().navigate(R.id.navigate_to_settings)
                                binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
                                setIdleSpinnerAdapter()
                            }
                        }
//                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    d(TAG, "More options spinner: nothing selected")
                    binding.letterGeneratorMoreOptionsSpinner.visibility = View.INVISIBLE
                }
            }
        }
    }

}


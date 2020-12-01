package subtext.yuvallovenotes.lovelettersgenerator

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding
import subtext.yuvallovenotes.login.EnterUserNameFragmentDirections
import subtext.yuvallovenotes.sendlettersreminder.LoveLetterAlarm
import subtext.yuvallovenotes.whatsapp.WhatsAppSender


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
    }

    private var currentLetter: LoveLetter? = null
    private lateinit var binding: FragmentLetterGeneratorBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var loveItemsViewModel: LoveItemsViewModel = get()

    private val onLetterTextChanged: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(newText: android.text.Editable?) {
            this@LetterGeneratorFragment.currentLetter?.let {
                it.text = newText.toString()
                if (newText.toString().isNotBlank()) {
                    loveItemsViewModel.updateLetter(it)
                    d(TAG, "updating love letter")
                }
            } ?: d(TAG, "no current letter to update")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (!sharedPrefs.getBoolean(getString(R.string.pref_key_is_login_process_completed), false)) {
            findNavController().navigate(LetterGeneratorFragmentDirections.navigateToEnterUserName())
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
        LoveLetterAlarm.SEND_LETTER_REMINDER.setAlarmAndCancelAllPreviousWithSameData(requireContext(), LoveLetterAlarm.SEND_LETTER_REMINDER.getDefaultActivationCalendar())
    }

    private fun setupToolbar() {
        binding.letterGeneratorToolBar.inflateMenu(R.menu.letter_generator_menu);

        binding.letterGeneratorToolBar.setOnMenuItemClickListener { item -> // Handle item selection
            when (item?.itemId) {

                R.id.menuActionSettings -> {
                    d(TAG, "Navigating to settings")
                    findNavController().navigate(R.id.navigate_to_settings)
                    true
                }

                R.id.menuActionShare -> {
                    showSharingPopup()
                    true
                }

                R.id.menuActionDelete -> {
                    showReallyDeleteDialog()
                    true
                }

                R.id.menuActionGoToLettersList -> {
                    onNavigationToLettersListRequested()
                    true
                }

                else -> false
            }
        }
    }

    //todo: cleanup
    override fun onStart() {
        super.onStart()
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
        this.currentLetter = loveItemsViewModel.randomLetter()
        binding.letterEditText.removeTextChangedListener(onLetterTextChanged)
        binding.letterEditText.setText(currentLetter?.text)
        binding.letterEditText.addTextChangedListener(onLetterTextChanged)
    }

    private fun onNavigationToLettersListRequested() {
        currentLetter?.let {
            if (it.text.isBlank()) {
                d(TAG, "deleting empty letter from data base")
                Toast.makeText(requireContext(), getString(R.string.title_empty_letter_deleted), LENGTH_LONG).show()
                loveItemsViewModel.deleteLetter(it)
            }
        }
        findNavController().navigate(R.id.navigate_to_letter_list)
    }

    private fun showReallyDeleteDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    currentLetter?.let {
                        d(TAG, "deleting letter")
                        Toast.makeText(requireContext(), getString(R.string.title_letter_deleted), LENGTH_LONG).show()
                        loveItemsViewModel.deleteLetter(it)
                        letterGeneratorListener.onClick(view)
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    d(TAG, "forfeited letter deletion request")
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title_are_you_sure))
                .setMessage(getString(R.string.title_letter_will_be_deleted_forever))
                .setPositiveButton(getString(R.string.title_ok), dialogClickListener)
                .setNegativeButton(getString(R.string.title_cancel), dialogClickListener).show()
    }

    private val letterSendListener: View.OnClickListener = View.OnClickListener {
        d(TAG, "Opening Whatsapp")
        val sendWhatsapp = WhatsAppSender()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        //Todo: set mobile number to yuval's only under my id (also set my custom letters)
        val phoneNumber: String = prefs.getString(getString(R.string.pref_key_full_target_phone_number), "")
                ?: ""
        sendWhatsapp.send(requireContext(), phoneNumber, binding.letterEditText.text.toString())
    }

    private fun setButtonsOnClickListeners() {
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.whatsappShareBtn.setOnClickListener(letterSendListener)
    }

    private fun showSharingPopup() {
        d(TAG, "Sharing letter in general sharing options")
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            currentLetter?.let {
                putExtra(Intent.EXTRA_TEXT, it.text)
            }
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}


package subtext.yuvallovenotes.lovelettersgenerator

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.interstitial.InterstitialAd
import io.sentry.Sentry
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
        private const val NEXT_LETTER_MENU_ITEM_POSITION: Int = 1
        private const val PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION: Int = 2
    }

    private lateinit var binding: FragmentLetterGeneratorBinding

    private var interstitialAd: InterstitialAd? = null
    private var loveItemsViewModel: LoveItemsViewModel = get()

    private val onLetterTextChanged: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(newText: android.text.Editable?) {
            loveItemsViewModel.currentLetter?.let {
                it.text = newText.toString()
                loveItemsViewModel.updateLetter(it)
                d(TAG, "Updating love letter ${it.id}")
            } ?: d(TAG, "No current letter to update")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        checkIfRegistrationNeeded()
        loveItemsViewModel.loadInterstitialAd(requireContext()) { ad ->
            this.interstitialAd = ad
        }
        return binding.root
    }

    private fun checkIfRegistrationNeeded() {
        if (!loveItemsViewModel.isLoginProcessCompleted()) { //todo: this should be !loveItemsViewModel.isLoginProcessCompleted()
            try {
                findNavController().navigate(LetterGeneratorFragmentDirections.navigateToEnterUserName())
            } catch (e: IllegalArgumentException) {
                w(TAG, "Unnecessary attempt to navigate to first login screen")
                e.printStackTrace()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeLettersListChanges()
        observeScreenEvents()
        displayLetterDataForFirstTime()
        binding.lettersGeneratorEditText.addTextChangedListener(onLetterTextChanged)
        setButtonsOnClickListeners()
    }

    private fun observeScreenEvents() {
        loveItemsViewModel.emptyGeneratedLetterEvent.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), getString(R.string.title_letter_list_is_empty), LENGTH_LONG).show()
        }

        loveItemsViewModel.noPreviousLetter.observe(viewLifecycleOwner) {
            if (binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION) != null) {
                binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_previous_faded_white_24)
            }
        }

        loveItemsViewModel.noNextLetter.observe(viewLifecycleOwner) {
            if (binding.letterGeneratorToolBar.menu.getItem(NEXT_LETTER_MENU_ITEM_POSITION) != null) {
                binding.letterGeneratorToolBar.menu.getItem(NEXT_LETTER_MENU_ITEM_POSITION).setIcon(R.drawable.ic_next_faded_white_24)
            }
        }
    }

    private fun setButtonsOnClickListeners() {
        binding.newLetterBtn.setOnClickListener(letterGeneratorListener)
        binding.navigateToLettersListBtn.setOnClickListener(navigateToLettersListListener)
        binding.whatsappShareBtn.setOnClickListener(whatsappSendListener)
    }

    private fun observeLettersListChanges() {
        loveItemsViewModel.loveLetters.observe(viewLifecycleOwner) {
            d(TAG, "Love letters size: " + loveItemsViewModel.getFilteredLetters().size)
        }
    }

    private fun setupToolbar() {
        binding.letterGeneratorToolBar.inflateMenu(R.menu.letter_generator_menu)

        binding.letterGeneratorToolBar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {

                R.id.menuActionSettings -> {
                    d(TAG, "Navigating to settings")
                    findNavController().navigate(R.id.navigate_to_settings)
                    true
                }

                R.id.menuActionShare -> {
                    showSharePopup()
                    true
                }

                R.id.menuActionDelete -> {
                    showReallyDeleteDialog()
                    true
                }

                R.id.menuActionNextLetter -> {
                    val nextLetter = loveItemsViewModel.getNextLetterFromDisplayedLettersList()
                    if (nextLetter != null) {
                        if (binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION) != null) {
                            binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_previous_white_24)
                        }
                        loveItemsViewModel.currentLetter = nextLetter
                        binding.lettersGeneratorEditText.setText(loveItemsViewModel.currentLetter?.text)
                    }
                    true
                }

                R.id.menuActionPreviousLetter -> {
                    val prevLetter = loveItemsViewModel.getPreviousLetterFromDisplayedLettersList()
                    if (prevLetter != null) {
                        if (binding.letterGeneratorToolBar.menu.getItem(NEXT_LETTER_MENU_ITEM_POSITION) != null) {
                            binding.letterGeneratorToolBar.menu.getItem(NEXT_LETTER_MENU_ITEM_POSITION).setIcon(R.drawable.ic_next_white_24)
                        }
                        loveItemsViewModel.currentLetter = prevLetter
                        binding.lettersGeneratorEditText.setText(loveItemsViewModel.currentLetter?.text)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun displayLetterDataForFirstTime() {
        val letterId = loveItemsViewModel.getCurrentLetterId()
        d(TAG, "letterId: $letterId")
        val observer: Observer<LoveLetter> = Observer { letter ->
            letter?.let {
                loveItemsViewModel.onLetterGenerated(letter)
                binding.lettersGeneratorEditText.setText(letter.text)
            } ?: createRandomLettersData {
                d(TAG, "No specific letter, generate random one and set its text")
                val newLetter = loveItemsViewModel.randomLetter()
                loveItemsViewModel.onLetterGenerated(newLetter)
                binding.lettersGeneratorEditText.setText(loveItemsViewModel.currentLetter?.text)
            }
        }
        loveItemsViewModel.getLetterById(letterId).observeOnce(viewLifecycleOwner, observer) //Checking if specific letter should be displayed
    }

    private fun createRandomLettersData(onCompletion: () -> Unit = {}) {
        loveItemsViewModel.loveLetters.observeOnce(viewLifecycleOwner, { letters ->
            d(TAG, "Love items size: ${letters?.size}")
            onCompletion.invoke()
        })
    }

    private val letterGeneratorListener: View.OnClickListener = View.OnClickListener {
        displayNewLetter()
    }

    private fun displayNewLetter() {
        loveItemsViewModel.deleteLetterIfEmpty(loveItemsViewModel.currentLetter)
        val newLetter = loveItemsViewModel.randomLetter()
        if (loveItemsViewModel.currentLetter?.text == newLetter.text && loveItemsViewModel.getFilteredLetters().size!! > 1) {
            displayNewLetter()
            return
        }
        if (binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION) != null) {
            binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_previous_white_24)
        }
        loveItemsViewModel.onLetterGenerated(newLetter)
        binding.lettersGeneratorEditText.removeTextChangedListener(onLetterTextChanged)
        binding.lettersGeneratorEditText.setText(loveItemsViewModel.currentLetter?.text)
        binding.lettersGeneratorEditText.addTextChangedListener(onLetterTextChanged)
        binding.lettersGeneratorEditText.requestFocus()
    }

    private fun onNavigationToLettersListRequested() {
        if (loveItemsViewModel.deleteLetterIfEmpty(loveItemsViewModel.currentLetter)) {
            Toast.makeText(requireContext(), getString(R.string.title_empty_letter_deleted), LENGTH_LONG).show()
        }
        val action = LetterGeneratorFragmentDirections.navigateToLetterList(loveItemsViewModel.currentLetter?.id
                ?: "")
        findNavController().navigate(action)
    }

    private fun showReallyDeleteDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    loveItemsViewModel.currentLetter?.let {
                        d(TAG, "Deleting letter")
//                        loveItemsViewModel.updateLettersArchiveStatusSync(listOf(it), true)
                        loveItemsViewModel.deleteLettersSync(listOf(it))
                        d(TAG, "Deleting completed")
                        letterGeneratorListener.onClick(view)
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    d(TAG, "Forfeited letter deletion request")
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title_are_you_sure))
                .setMessage(getString(R.string.title_letter_will_be_deleted_forever))
                .setPositiveButton(getString(R.string.title_ok), dialogClickListener)
                .setNegativeButton(getString(R.string.title_cancel), dialogClickListener).show()
    }

    private fun showAd(onCompletion: () -> Unit) {
        val fullScreenContentCallback = loveItemsViewModel.newFullScreenContentCallback(onCompletion)
        interstitialAd?.let {
            it.fullScreenContentCallback = fullScreenContentCallback
            it.show(requireActivity())
        } ?: onCompletion.invoke()
    }


    private val whatsappSendListener: View.OnClickListener = View.OnClickListener {
        showAd {
            Sentry.captureMessage("User sent message with whatsapp")
            loveItemsViewModel.openWhatsapp(requireContext(), binding.lettersGeneratorEditText.text.toString())
            loveItemsViewModel.loadInterstitialAd(requireContext()) { ad ->
                this.interstitialAd = ad
            }
        }
    }

    private val navigateToLettersListListener: View.OnClickListener = View.OnClickListener {
        onNavigationToLettersListRequested()
    }

    private fun showSharePopup() {
        d(TAG, "Sharing letter in general sharing options")
//        val shareIntent = Intent.createChooser(sendIntent, null)
        val shareIntent = loveItemsViewModel.generateShareIntent(loveItemsViewModel.currentLetter)
        if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
            showAd {
                if (LoveUtils.getDeviceId() != BuildConfig.ARIEL_DEVICE_ID) {
                } //TODO: use this if statement
                Sentry.captureMessage("User is sharing letter in general sharing options")
                startActivity(shareIntent)
                loveItemsViewModel.loadInterstitialAd(requireContext()) { ad ->
                    this.interstitialAd = ad
                }
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_no_external_app_found_for_sharing_content), LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        loveItemsViewModel.deleteLetterIfEmpty(loveItemsViewModel.currentLetter)
    }
}




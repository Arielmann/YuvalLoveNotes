package subtext.yuvallovenotes.lovelettersgenerator

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.util.Log.d
import android.util.Log.w
import android.view.*
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.interstitial.InterstitialAd
import io.sentry.Sentry
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.observeAndIgnoreInitialNotification
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding


class LetterGeneratorFragment : Fragment() {

    companion object {
        private val TAG = LetterGeneratorFragment::class.simpleName
        private const val MAKE_LETTER_FAVOURITE_ITEM_POSITION: Int = 3
    }

    private lateinit var binding: FragmentLetterGeneratorBinding
    private var interstitialAd: InterstitialAd? = null
    private lateinit var loveItemsViewModel: LoveItemsViewModel
    private var previousHistoryLetterMenuItemPosition: Int = 1
    private var nextHistoryLetterMenuItemPosition: Int = 2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLetterGeneratorBinding.inflate(inflater, container, false)
        loveItemsViewModel = ViewModelProvider(requireActivity()).get(LoveItemsViewModel::class.java)
        checkIfRegistrationNeeded()
        loveItemsViewModel.loadInterstitialAd(requireContext()) { ad ->
            this.interstitialAd = ad
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeLettersListChanges()
        observeViewmodelEvents()
        displayLetterDataForFirstTime()
        setButtonsOnClickListeners()
        configViewsAccordingToLayoutDirection()
    }

    override fun onStart() {
        super.onStart()
        binding.lettersGeneratorEditText.addTextChangedListener(onLetterTextChanged)
        loveItemsViewModel.notifyRelevantDisplayLettersStateObservers()
    }

    private var onNextLetterRequired: () -> Unit = {
        val nextLetter = loveItemsViewModel.getNextLetterFromDisplayedLettersList()
        if (nextLetter != null) {
            validatePreviousLetterMenuItemExistence {
                loveItemsViewModel.notifyRelevantDisplayLettersStateObservers()
//                            binding.letterGeneratorToolBar.menu.getItem(PREVIOUS_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_arrow_back_24)
            }
            loveItemsViewModel.currentLetter = nextLetter
            loveItemsViewModel.onLetterGenerated(nextLetter)
            updateLetterEditText()
        }
    }

    private var onPreviousLetterRequired: () -> Unit = {
        val prevLetter = loveItemsViewModel.getPreviousLetterFromDisplayedLettersList()
        if (prevLetter != null) {
            validateNextLetterMenuItemExistence {
                loveItemsViewModel.notifyRelevantDisplayLettersStateObservers()
//                            binding.letterGeneratorToolBar.menu.getItem(NEXT_LETTER_MENU_ITEM_POSITION).setIcon(R.drawable.ic_arrow_forward_24)
            }
            loveItemsViewModel.currentLetter = prevLetter
            updateLetterEditText()
        }
    }

    private fun configViewsAccordingToLayoutDirection() {
        if (resources.getBoolean(R.bool.is_right_to_left)) {
            previousHistoryLetterMenuItemPosition = 2
            nextHistoryLetterMenuItemPosition = 1
        } else {
            onNextLetterRequired = onPreviousLetterRequired.also { onPreviousLetterRequired = onNextLetterRequired } //Swapping logic
        }
    }

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

    private fun observeViewmodelEvents() {
        loveItemsViewModel.emptyGeneratedLetterEvent.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), getString(R.string.title_letter_list_is_empty), LENGTH_LONG).show()
        }


        loveItemsViewModel.previousLettersFound.observeAndIgnoreInitialNotification(viewLifecycleOwner) {
            validatePreviousLetterMenuItemExistence {
                d(TAG, "Setting image: ic_arrow_back_24")
                binding.letterGeneratorToolbar.menu.getItem(previousHistoryLetterMenuItemPosition).setIcon(R.drawable.ic_arrow_back_24)
            }
        }

        loveItemsViewModel.nextLettersFound.observeAndIgnoreInitialNotification(viewLifecycleOwner) {
            validateNextLetterMenuItemExistence {
                d(TAG, "Setting image: ic_arrow_forward_24")
                binding.letterGeneratorToolbar.menu.getItem(nextHistoryLetterMenuItemPosition).setIcon(R.drawable.ic_arrow_forward_24)
            }
        }

        loveItemsViewModel.noPreviousLetters.observeAndIgnoreInitialNotification(viewLifecycleOwner) {
            validatePreviousLetterMenuItemExistence {
                d(TAG, "Setting image: ic_arrow_back_faded_24")
                binding.letterGeneratorToolbar.menu.getItem(previousHistoryLetterMenuItemPosition).setIcon(R.drawable.ic_arrow_back_faded_24)
            }
        }

        loveItemsViewModel.noNextLetters.observeAndIgnoreInitialNotification(viewLifecycleOwner) {
            validateNextLetterMenuItemExistence {
                d(TAG, "Setting image: ic_arrow_forward_faded_24")
                binding.letterGeneratorToolbar.menu.getItem(nextHistoryLetterMenuItemPosition).setIcon(R.drawable.ic_arrow_forward_faded_24)
            }
        }

        loveItemsViewModel.onLetterPickedForEditing.observe(viewLifecycleOwner) {
            updateLetterEditText()
        }

        loveItemsViewModel.onLetterAddedToFavourites.observe(viewLifecycleOwner) {
            binding.letterGeneratorToolbar.menu.getItem(MAKE_LETTER_FAVOURITE_ITEM_POSITION).setIcon(R.drawable.ic_baseline_favourite_white)
        }

        loveItemsViewModel.onLetterRemovedFromFavourites.observe(viewLifecycleOwner) {
            binding.letterGeneratorToolbar.menu.getItem(MAKE_LETTER_FAVOURITE_ITEM_POSITION).setIcon(R.drawable.ic_baseline_favourite_border_white)
        }
    }

    private fun validatePreviousLetterMenuItemExistence(onValid: () -> Unit) {
        if (binding.letterGeneratorToolbar.menu.getItem(previousHistoryLetterMenuItemPosition) != null) {
            onValid.invoke()
        } else {
            Log.e(TAG, "Could not update previous letter menu item because it's null")
        }
    }

    private fun validateNextLetterMenuItemExistence(onValid: () -> Unit) {
        if (binding.letterGeneratorToolbar.menu.getItem(nextHistoryLetterMenuItemPosition) != null) {
            onValid.invoke()
        } else {
            Log.e(TAG, "Could not update previous letter menu item because it's null")
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
        binding.letterGeneratorToolbar.inflateMenu(R.menu.letter_generator_menu)

        binding.letterGeneratorToolbar.setOnMenuItemClickListener { item ->
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

                R.id.menuActionMakeFavourite -> {
                    loveItemsViewModel.switchCurrentLetterFavouriteState()
                    true
                }

                R.id.menuActionNextLetter -> {
                    onNextLetterRequired.invoke()
                    true
                }

                R.id.menuActionPreviousLetter -> {
                    onPreviousLetterRequired.invoke()
                    true
                }

                else -> false
            }
        }
    }

    private fun updateLetterEditText() {
        binding.lettersGeneratorEditText.removeTextChangedListener(onLetterTextChanged)
        binding.lettersGeneratorEditText.setText(loveItemsViewModel.currentLetter?.text)
        binding.lettersGeneratorEditText.addTextChangedListener(onLetterTextChanged)
    }

    private fun displayLetterDataForFirstTime() {
        val letterId = loveItemsViewModel.getCurrentLetterId()
        d(TAG, "letterId: $letterId")
        val observer: Observer<LoveLetter> = Observer { letter ->
            letter?.let {
                loveItemsViewModel.onLetterGenerated(letter)
                updateLetterEditText()
            } ?: createRandomLettersData {
                d(TAG, "No specific letter, generate random one and set its text")
                val newLetter = loveItemsViewModel.randomLetter()
                loveItemsViewModel.onLetterGenerated(newLetter)
                updateLetterEditText()
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
        displayNewRandomLetter()
    }

    private fun displayNewRandomLetter() {
        /*  if (loveItemsViewModel.deleteEmptyLetters()) {
              Toast.makeText(requireContext(), getString(R.string.title_empty_letter_deleted), LENGTH_LONG).show()
          }*/
        val newLetter = loveItemsViewModel.randomLetter()
        loveItemsViewModel.onLetterGenerated(newLetter)
        updateLetterEditText()
        binding.lettersGeneratorEditText.requestFocus()
    }

    private fun onNavigationToLettersListRequested() {
        if (loveItemsViewModel.cleanDisplayListFromCorruptedLetters { it.text.isBlank() }) {
            Toast.makeText(requireContext(), getString(R.string.title_empty_letter_deleted), LENGTH_LONG).show()
        }
        val action = LetterGeneratorFragmentDirections.navigateToLetterList(loveItemsViewModel.currentLetter?.id ?: "")
        try {
            findNavController().navigate(action)
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun showReallyDeleteDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    loveItemsViewModel.currentLetter?.let {
                        d(TAG, "Deleting letter")
//                        loveItemsViewModel.updateLettersArchiveStatusSync(listOf(it), true)
                        loveItemsViewModel.deleteLettersSync(listOf(it))
                        loveItemsViewModel.clearLettersHistory()
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
        binding.lettersGeneratorEditText.removeTextChangedListener(onLetterTextChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        d(TAG, "onDestroy")
        loveItemsViewModel.cleanDisplayListFromCorruptedLetters { it.text.isBlank() }
    }
}




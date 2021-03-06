package subtext.yuvallovenotes.lovelettersoverview

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.listsadapter.CustomLinearLayout
import subtext.yuvallovenotes.crossapplication.listsadapter.ItemSelectionCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.crossapplication.utils.observeOnce
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterListBinding


class LetterListFragment : Fragment() {

    companion object {
        private val TAG = LetterListFragment::class.simpleName!!
        private const val SELECT_ALL_LETTERS_MENU_ITEM_POSITION: Int = 0
        private const val DELETE_SELECTED_LETTERS_MENU_ITEM_POSITION: Int = 1
    }

    private lateinit var binding: FragmentLetterListBinding
    private lateinit var loveItemsViewModel: LoveItemsViewModel
    private lateinit var lettersListAdapter: LetterListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        loveItemsViewModel = ViewModelProvider(requireActivity()).get(LoveItemsViewModel::class.java)
        binding = FragmentLetterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loveItemsViewModel.loveLetters.observeOnce(viewLifecycleOwner, loveLettersListObserver)
        setupLetterList()
        setOnClickListeners()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
        setupFragmentLettersListToolbar()
        loveItemsViewModel.cleanDisplayListFromCorruptedLetters { it.text.isBlank() }
    }

    private val loveLettersListObserver: Observer<MutableList<LoveLetter>?> = Observer<MutableList<LoveLetter>?> {
        d(TAG, "Letters list updated")
        lettersListAdapter.submitList(mutableListOf())
        lettersListAdapter.submitList(loveItemsViewModel.getFilteredLetters().sortedBy { !it.isCreatedByUser })
    }


    /**
     * Setting up the onBackPressed functionality for this fragment
     */
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (lettersListAdapter.isSelectionModeActive) {
                exitSelectionMode()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupFragmentLettersListToolbar() {
        binding.letterListToolBar.menu.clear()
//        binding.letterListToolBar.inflateMenu(R.menu.letter_list_menu)
        binding.letterListToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24)
        binding.letterListToolBar.setNavigationOnClickListener {
            d(TAG, "Navigating to previous screen")
            findNavController().popBackStack()
        }
    }

    private fun setOnClickListeners() {
        binding.createLetterBtn.setOnClickListener {
            loveItemsViewModel.createNewLetter()
            findNavController().popBackStack()
        }
    }

    private val favouriteLetterSelectionListener = object : ItemSelectionCallback<LoveLetter> {
        override fun onItemSelected(item: LoveLetter) {
            loveItemsViewModel.loveLetters.removeObserver(loveLettersListObserver)
            d(TAG, "Removing observer")
            loveItemsViewModel.updateLetter(item)
        }

        override fun itemWillBeRemovedFromSelectionList(item: LoveLetter) {
            loveItemsViewModel.loveLetters.removeObserver(loveLettersListObserver)
            d(TAG, "Removing observer")
            loveItemsViewModel.updateLetter(item)
        }

    }

    private val letterSelectionListener = object : ItemSelectionCallback<LoveLetter> {
        override fun onItemSelected(item: LoveLetter) {
            d(TAG, "onItemSelected")
            if (lettersListAdapter.selectedLetters.size == 1 && !lettersListAdapter.isSelectionModeActive) {
                d(TAG, "Entering selection mode")
                binding.letterListToolBar.inflateMenu(R.menu.letter_list_item_selected_menu)
                binding.letterListToolBar.setNavigationIcon(R.drawable.ic_baseline_close_white_24)
                binding.letterListToolBar.setNavigationOnClickListener { exitSelectionMode() }
                binding.letterListToolBar.setOnMenuItemClickListener(selectedLettersMenuClickListener)
            }

            if (binding.letterListToolBar.menu.getItem(DELETE_SELECTED_LETTERS_MENU_ITEM_POSITION) != null) { //If menu is inflated
                binding.letterListToolBar.menu.getItem(DELETE_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_delete_white_24dp)
            }

            if (lettersListAdapter.areAllItemsSelected()) {
                binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_unselect_all_24)
            }


        }

        override fun itemWillBeRemovedFromSelectionList(item: LoveLetter) {
            d(TAG, "Love letter will be removed from selection list")
            if (lettersListAdapter.areAllItemsSelected()) {
                binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_select_all_24)
            }

            if (lettersListAdapter.selectedLetters.size == 1) {
                d(TAG, "Removing the last letter from the selection list and exiting selection mode")
                exitSelectionMode()
            }
        }
    }

    private fun setupLetterList() {

        val onLetterOpenRequest: (letter: LoveLetter) -> Unit = { letter ->
            loveItemsViewModel.onLetterPickedForEditing(letter)
            LetterListFragmentDirections.navigateToLetterGenerator(letter.id)
            findNavController().popBackStack()
        }

        lettersListAdapter = LetterListAdapter(requireContext(), onLetterOpenRequest)
        lettersListAdapter.lettersSelectionListener = letterSelectionListener
        lettersListAdapter.favouriteLetterSelectionListener = favouriteLetterSelectionListener

        binding.lettersRV.apply {
            lettersListAdapter.setHasStableIds(true)
            binding.lettersRV.adapter = lettersListAdapter
            layoutManager = CustomLinearLayout(requireContext())
        }
    }


    private val selectedLettersMenuClickListener: Toolbar.OnMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->

        when (item?.itemId) {
            R.id.menuActionChooseAllLetters -> {
                if (lettersListAdapter.areAllItemsSelected()) {
                    lettersListAdapter.deselectAllLetters()
                    binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_select_all_24)
                    binding.letterListToolBar.menu.getItem(DELETE_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_delete_faded_white_24dp)
                } else {
                    binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_unselect_all_24)
                    lettersListAdapter.selectAllLetters()
                    binding.letterListToolBar.menu.getItem(DELETE_SELECTED_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_delete_white_24dp)
                }
                true
            }

            R.id.menuActionDelete -> {
                if (lettersListAdapter.selectedLetters.size > 0) {
                    showReallyDeleteDialog(lettersListAdapter.selectedLetters)
                }
                true
            }
            else -> false
        }
    }

    private fun showReallyDeleteDialog(letters: MutableSet<LoveLetter>) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    d(TAG, "Deleting letters")
                    loveItemsViewModel.deleteLettersSync(letters.toList())
                    loveItemsViewModel.cleanDisplayListFromCorruptedLetters { it.text.isBlank() }
                    loveItemsViewModel.clearLettersHistory()
                    d(TAG, "Deletion completed")
                    lettersListAdapter.exitSelectionMode()
                    loveItemsViewModel.loveLetters.observe(viewLifecycleOwner, loveLettersListObserver)
                    LoveUtils.setupFragmentDefaultToolbar(this, binding.letterListToolBar)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    d(TAG, "Forfeited letter deletion request")
                }
            }
        }

        var msg = getString(R.string.title_letter_will_be_deleted_forever)
        if (letters.size > 1) {
            msg = getString(R.string.title_letters_will_be_deleted_forever)
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title_are_you_sure))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.title_ok), dialogClickListener)
                .setNegativeButton(getString(R.string.title_cancel), dialogClickListener).show()
    }

    private fun exitSelectionMode() {
        lettersListAdapter.exitSelectionMode()
        LoveUtils.setupFragmentDefaultToolbar(this, binding.letterListToolBar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
    }

}


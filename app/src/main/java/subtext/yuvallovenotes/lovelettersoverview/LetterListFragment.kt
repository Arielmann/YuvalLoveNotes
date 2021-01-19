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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.listsadapter.ItemSelectionCallback
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterListBinding


class LetterListFragment : Fragment(), ItemSelectionCallback {

    companion object {
        private val TAG = LetterListFragment::class.simpleName!!
        private const val SELECT_ALL_LETTERS_MENU_ITEM_POSITION: Int = 0
    }

    private lateinit var binding: FragmentLetterListBinding
    private val loveItemsViewModel: LoveItemsViewModel = get()
    private lateinit var lettersListAdapter: LetterListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLetterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupLetterList()
        setOnClickListeners()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
        LoveUtils.setupFragmentDefaultToolbar(this, binding.letterListToolBar)
    }

    private fun setupViewModel() {
        observeDataUpdates()
    }

    private fun observeDataUpdates() {
        loveItemsViewModel.loveLetters.observe(viewLifecycleOwner) { letters ->
            // Update the cached copy of the letters in the adapter.
            letters?.let {
//                Log.d(TAG, "Updating letters list UI. letters: {$letters}")
                lettersListAdapter.submitList(letters.filter { !it.isArchived }.sortedBy { !it.isCreatedByUser })
            }
        }
    }

    private fun setOnClickListeners() {
        binding.createLetterBtn.setOnClickListener {
            val newLetter = LoveLetter()
            newLetter.isCreatedByUser = true
            loveItemsViewModel.insertLetter(newLetter)
            val action = LetterListFragmentDirections.navigateToLetterGenerator(newLetter.id)
            findNavController().navigate(action)
        }
    }

    private fun setupLetterList() {

        val onLetterOpenRequest: (letter: LoveLetter) -> Unit = { letter ->
            val action = LetterListFragmentDirections.navigateToLetterGenerator(letter.id)
            findNavController().navigate(action)
        }

        lettersListAdapter = LetterListAdapter(requireContext(), onLetterOpenRequest, this)

        binding.lettersRV.apply {
            lettersListAdapter.setHasStableIds(true)
            binding.lettersRV.adapter = lettersListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    private val selectedLettersMenuClickListener: Toolbar.OnMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item?.itemId) {
            R.id.menuActionChooseAllLetters -> {
                if (lettersListAdapter.areAllItemsSelected()) {
                    lettersListAdapter.deselectAllLetters()
                    binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_select_all_24)
                } else {
                    binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_unselect_all_24)
                    lettersListAdapter.selectAllLetters()
                }
                true
            }

            R.id.menuActionDelete -> {
                showReallyDeleteDialog(lettersListAdapter.selectedLetters)
                true
            }
            else -> false
        }
    }

    /**
     * Setting up the onBackPressed functionality for this fragment
     */
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (lettersListAdapter.selectedLetters.isNotEmpty()) {
                exitSelectionMode()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun showReallyDeleteDialog(letters: MutableSet<LoveLetter>) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    d(TAG, "Deleting letters")
                    loveItemsViewModel.updateLettersArchiveStatusSync(letters.toList(), true)
//                    loveItemsViewModel.deleteLettersSync(letters.toList())
                    d(TAG, "Deleting completed")
                    lettersListAdapter.exitSelectionMode()
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

    override fun onItemSelected() {
        if (lettersListAdapter.selectedLetters.size == 1 && !lettersListAdapter.isSelectionModeActive) {
            d(TAG, "Entering selection mode")
            binding.letterListToolBar.inflateMenu(R.menu.letter_list_item_selected_menu)
            binding.letterListToolBar.setNavigationIcon(R.drawable.ic_baseline_close_white_24)
            binding.letterListToolBar.setNavigationOnClickListener { exitSelectionMode() }
            binding.letterListToolBar.setOnMenuItemClickListener(selectedLettersMenuClickListener)
        }

        if (lettersListAdapter.areAllItemsSelected()) {
            binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_unselect_all_24)
        }

    }

    override fun itemWillBeRemoved() {
        d(TAG, "Love letter will be removed")
        if (lettersListAdapter.areAllItemsSelected()) {
            binding.letterListToolBar.menu.getItem(SELECT_ALL_LETTERS_MENU_ITEM_POSITION).setIcon(R.drawable.ic_baseline_select_all_24)
        }
    }

    private fun exitSelectionMode() {
        lettersListAdapter.exitSelectionMode()
        LoveUtils.setupFragmentDefaultToolbar(this, binding.letterListToolBar)
    }

}


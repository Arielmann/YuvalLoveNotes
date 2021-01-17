package subtext.yuvallovenotes.lovelettersoverview

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.databinding.FragmentLetterListBinding


class LetterListFragment : Fragment() {

    companion object {
        private val TAG = LetterListFragment::class.simpleName!!
    }

    private var selectionActionMode: ActionMode? = null
    private lateinit var binding: FragmentLetterListBinding
    private val loveItemsViewModel: LoveItemsViewModel = get()
    private lateinit var lettersListAdapter: LetterListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                Log.d(TAG, "Updating letters list UI. letters: {$letters}")
                lettersListAdapter.submitList(letters.filter { !it.isDisabled }.sortedBy { !it.isCreatedByUser })
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

        lettersListAdapter = LetterListAdapter(requireContext())



        lettersListAdapter.onItemClickListener = { letter ->
            val action = LetterListFragmentDirections.navigateToLetterGenerator(letter.id)
            findNavController().navigate(action)
        }

        binding.lettersRV.apply {
            lettersListAdapter.setHasStableIds(true)
            binding.lettersRV.adapter = lettersListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        val lettersSelectionTracker = SelectionTracker.Builder(
                TAG,
                binding.lettersRV,
                StableIdKeyProvider(binding.lettersRV),
                LetterListAdapter.LetterListAdapterItemDetailsLookup(binding.lettersRV),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        lettersListAdapter.lettersSelectionTracker = lettersSelectionTracker
        setLettersSelectionObservers()
    }

    private fun setLettersSelectionObservers() {
        lettersListAdapter.setSelectionsObserver()
        lettersListAdapter.lettersSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (lettersListAdapter.lettersSelectionTracker.hasSelection() && selectionActionMode == null) {
                    selectionActionMode = (requireActivity() as AppCompatActivity?)!!.startSupportActionMode(selectionActionModeCallback)
                } else if (!lettersListAdapter.lettersSelectionTracker.hasSelection() && selectionActionMode != null) {
                    selectionActionMode!!.finish()
                }
            }


        })
    }

    private var selectionActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.letter_list_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {

                R.id.menuActionChooseAllLetters -> {
                    lettersListAdapter.chooseAllLetters()
                    Toast.makeText(requireContext(), "Option 1 selected", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.menuActionDelete -> {
                    Toast.makeText(requireContext(), "Option 2 selected", Toast.LENGTH_SHORT).show()
                    showReallyDeleteDialog(lettersListAdapter.selectedLetters, mode)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            selectionActionMode = null
        }
    }

    /**
     * Setting up the onBackPressed functionality for this fragment
     */
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (selectionActionMode != null) {
                selectionActionMode!!.finish()
                return
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun showReallyDeleteDialog(mode1: MutableMap<Long, LoveLetter>, mode: ActionMode) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    currentLetter?.let {
                        Log.d(TAG, "Deleting letter")
                        loveItemsViewModel.deleteLettersAsync(it)
                        Log.d(TAG, "Deleting completed")
                        mode.finish()
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    Log.d(TAG, "Forfeited letter deletion request")
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title_are_you_sure))
                .setMessage(getString(R.string.title_letter_will_be_deleted_forever))
                .setPositiveButton(getString(R.string.title_ok), dialogClickListener)
                .setNegativeButton(getString(R.string.title_cancel), dialogClickListener).show()
    }

}


package subtext.yuvallovenotes.lovewritingtabs.ui.main.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_editor_tab.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.lovewritingtabs.ui.main.PageViewModel

/**
 * A placeholder fragment containing a simple view.
 */
class LoveEditorFragment : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var pageViewModelProvider: ViewModelProvider
    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModelProvider = ViewModelProvider(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_editor_tab, container, false)
        pageViewModel = pageViewModelProvider.get(PageViewModel::class.java).apply {
            setLoveNotesBackendless(context)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        // set a LinearLayoutManager to handle Android
        // RecyclerView behavior
        pageViewModel.loveNotesBackendless.findAllLoveData(object : AsyncCallback<List<LoveItem>> {
            override fun handleFault(fault: BackendlessFault?) {
                TODO("Not yet implemented")
            }

            override fun handleResponse(response: List<LoveItem>?) {
                activity?.runOnUiThread(Runnable {
                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)
                        if (response != null) {
                            adapter = ListAdapter(response.toMutableList())
                        }
                    }
                })

            }
        })
        // set the custom adapter to the RecyclerView
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): LoveEditorFragment {
            return LoveEditorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
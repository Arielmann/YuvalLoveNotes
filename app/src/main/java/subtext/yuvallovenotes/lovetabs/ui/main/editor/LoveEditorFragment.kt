package subtext.yuvallovenotes.lovetabs.ui.main.editor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_editor_tab.*
import org.koin.android.ext.android.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveitems.LoveItem
import subtext.yuvallovenotes.lovetabs.viewmodel.LoveViewModel

class LoveEditorFragment : Fragment() {

    companion object {
        private val TAG: String = LoveEditorFragment::class.simpleName!!
    }

    private var loveViewModel: LoveViewModel = get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_editor_tab, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDataUpdates()

        loveViewModel.loveNetworkCalls.findAllLoveData(object : AsyncCallback<List<LoveItem>> {

            override fun handleResponse(response: List<LoveItem>?) {

                if (response.isNullOrEmpty()) {
                    handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                    return
                }

                loveViewModel.loveItemsFromNetwork = response.toMutableList()

                list_recycler_view.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = ListAdapter(loveViewModel.loveItemsFromNetwork)
                }
            }

            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(requireContext(), fault.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun observeDataUpdates() {
        loveViewModel.areAllLoveItemsAvailable.observe(viewLifecycleOwner, { areAvailable ->
            // Update the cached copy of the lovePhrases in the adapter.
            if (areAvailable) {
                Log.d(TAG, "Love items available")

            }
        })
    }
}
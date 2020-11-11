package subtext.yuvallovenotes.lovewritingtabs.ui.main.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_editor_tab.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.loveletters.LoveItemType
import subtext.yuvallovenotes.lovewritingtabs.ui.main.LoveViewModel
import org.koin.android.ext.android.get

class LoveEditorFragment : Fragment() {

    private var loveViewModel: LoveViewModel = get()
    private lateinit var loveItemsOffestsMap: Map<LoveItemType, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loveItemsOffestsMap = hashMapOf(LoveItemType.OPENER to 40, LoveItemType.PHRASE to 50, LoveItemType.CLOSURE to 60)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_love_editor_tab, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loveViewModel.loveNotesBackendless.findAllLoveData(object : AsyncCallback<List<LoveItem>> {

            override fun handleResponse(response: List<LoveItem>?) {

                if (response.isNullOrEmpty()) {
                    handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                    return
                }

                loveViewModel.loveItems = response.toMutableList()

                list_recycler_view.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = ListAdapter(loveViewModel.loveItems)
                }
            }

            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(requireContext(), fault.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }
}
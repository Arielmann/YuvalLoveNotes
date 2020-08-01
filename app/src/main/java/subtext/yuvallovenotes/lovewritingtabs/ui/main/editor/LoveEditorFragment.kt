package subtext.yuvallovenotes.lovewritingtabs.ui.main.editor

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.android.synthetic.main.fragment_love_editor_tab.*
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.loveletters.LoveClosure
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.loveletters.LovePhrase
import subtext.yuvallovenotes.lovewritingtabs.ui.main.PageViewModel

class LoveEditorFragment : Fragment() {

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
            pageViewModel.loveNotesBackendless.findAllLoveData(object : AsyncCallback<List<LoveItem>> {

                override fun handleResponse(response: List<LoveItem>?) {

                    if (response.isNullOrEmpty()) {
                        handleFault(BackendlessFault("Bad response. Result returned from server is $response"))
                        return
                    }

                    pageViewModel.loveItems = response.toMutableList()

                    list_recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)
                        adapter = ListAdapter(pageViewModel.loveItems)
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Toast.makeText(context, fault.toString(), Toast.LENGTH_LONG).show()
                }

            })
    }

    private fun showDialog(title: String) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.edit_item_dialog)
        val body = dialog.findViewById(R.id.editorDialogTV) as TextView
        body.text = title
        val yesBtn = dialog.findViewById(R.id.editorDialogDoneBtn) as Button
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
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
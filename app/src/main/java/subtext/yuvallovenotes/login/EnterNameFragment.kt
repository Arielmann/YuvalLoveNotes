package subtext.yuvallovenotes.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import subtext.yuvallovenotes.databinding.FragmentEnterNameBinding
import subtext.yuvallovenotes.databinding.FragmentLetterGeneratorBinding

class EnterNameFragment : Fragment() {

    lateinit var binding : FragmentEnterNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEnterNameBinding.inflate(inflater, container, false)
        return binding.root
    }

}
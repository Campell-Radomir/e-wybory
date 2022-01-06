package pl.edu.pjwstk.ewybory.ui.edo

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.edu.pjwstk.ewybory.R
import pl.edu.pjwstk.ewybory.databinding.EdoCanFragmentBinding

class EdoCanFragment : Fragment() {

    companion object {
        fun newInstance() = EdoCanFragment()
    }

    private lateinit var viewModel: EdoCanViewModel
    private lateinit var binding: EdoCanFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity()).get(EdoCanViewModel::class.java)
        binding = EdoCanFragmentBinding.inflate(inflater,container, false)
        addListener()
        return binding.root
    }

    private fun addListener() {
        binding.canEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                canEditHandler(s)
            }
        })
    }

    private fun canEditHandler(input: CharSequence) {
        viewModel.setCan(input.toString())
    }

}
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
import pl.edu.pjwstk.ewybory.databinding.EdoDocumentFragmentBinding

class EdoDocumentFragment : Fragment() {

    companion object {
        fun newInstance() = EdoDocumentFragment()
    }

    private lateinit var viewModel: EdoDocumentViewModel
    private lateinit var binding: EdoDocumentFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(EdoDocumentViewModel::class.java)
        binding = EdoDocumentFragmentBinding.inflate(inflater, container, false)
        addEditTextListeners()
        return binding.root
    }

    private fun expirationDateEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank() && text.matches(Regex("^\\d{6}$"))) {
            viewModel.setExpirationDate(text.toString())
        }
    }

    private fun dateOfBirthEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank() && text.matches(Regex("^\\d{6}$"))) {
            viewModel.setBirthDate(text.toString())
        }
    }

    private fun documentNumberEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            viewModel.setDocumentNumber(text.toString())
        }
    }


    private fun addEditTextListeners() {
        binding.documentNumberEditText.addTextChangedListener(object : TextWatcher {
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
                documentNumberEditHandler(s)
            }
        })

        binding.dateOfBirthEditText.addTextChangedListener(object : TextWatcher {
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
                dateOfBirthEditHandler(s)
            }
        })

        binding.expirationDateEditText.addTextChangedListener(object : TextWatcher {
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
                expirationDateEditHandler(s)
            }
        })
    }

}
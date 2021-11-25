package pl.edu.pjwstk.ewybory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pl.edu.pjwstk.ewybory.databinding.ActivityEdoResultBinding
import java.text.SimpleDateFormat
import java.util.*

class EdoResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdoResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showResultData()
    }

    private fun showResultData() {
        binding.firstNameText.text = getStringOrDefault(getString(R.string.intent_first_name));
        binding.lastNameText.text = getStringOrDefault(getString(R.string.intent_last_name))
        binding.birthDateText.text = formatDate(getStringOrDefault(getString(R.string.intent_birth_date)))
        binding.nationalityText.text = getStringOrDefault(getString(R.string.intent_nationality))
        binding.personalNumberText.text = getStringOrDefault(getString(R.string.intent_personal_number))

    }

    private fun formatDate(date: String): CharSequence? {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(date))
    }

    private fun getStringOrDefault(extraName: String): String {
        val text = intent.getStringExtra(extraName)
        return if (text.isNullOrBlank()) getString(R.string.result_empty_data_text) else text
    }

}
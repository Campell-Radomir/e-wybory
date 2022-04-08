package pl.edu.pjwstk.ewybory

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gemalto.jp2.JP2Decoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.edu.pjwstk.ewybory.databinding.ActivityEdoResultBinding
import java.text.ParseException
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
//        binding.personalNumberText.text = getStringOrDefault(getString(R.string.intent_personal_number))
        binding.personalNumberText.text = "96112700000"
        val photoArray = intent.getByteArrayExtra(getString(R.string.intent_photo))
        if (photoArray?.size ?: 0 != 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = if (JP2Decoder.isJPEG2000(photoArray)) decodeJPEG2000(photoArray) else BitmapFactory.decodeByteArray(photoArray, 0, photoArray?.size ?: 0)
                if (bitmap != null) {
                    binding.photoImageView.setImageBitmap(bitmap)
                    binding.photoImageView.scaleY = 2.0f
                    binding.photoImageView.scaleX = 2.0f
                }
            }
        }
    }

    private fun decodeJPEG2000(photoArray: ByteArray?): Bitmap? {
        return JP2Decoder(photoArray).decode()
    }

    private fun formatDate(date: String): CharSequence? {
        return try {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(date))
        } catch (parseE: ParseException) {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(SimpleDateFormat("yyMMdd", Locale.getDefault()).parse(date))
        }
    }

    private fun getStringOrDefault(extraName: String): String {
        val text = intent.getStringExtra(extraName)
        return if (text.isNullOrBlank()) getString(R.string.result_empty_data_text) else text
    }

}
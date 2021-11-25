package pl.edu.pjwstk.ewybory

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sf.scuba.smartcards.CardService
import org.jmrtd.*
import org.jmrtd.PassportService.NORMAL_MAX_TRANCEIVE_LENGTH
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.icao.DG11File
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.DG2File
import org.spongycastle.jce.provider.BouncyCastleProvider
import pl.edu.pjwstk.ewybory.databinding.ActivityEdoAppAuthBinding
import java.security.Security


class EdoAppAuthActivity : AppCompatActivity() {

    private val NFC_TIMEOUT = 10 * 1000

    private val TAG: String = EdoAppAuthActivity::class.java.simpleName
    private lateinit var nfcAdapter: NfcAdapter
    private var documentNumber = ""
    private var birthDate = ""
    private var expirationDate = ""
    private var can = ""
    private lateinit var binding: ActivityEdoAppAuthBinding;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoAppAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        addEditTextListeners()
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "New intent incoming...")
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.action)) {
            handleNfcIntent(intent)
        }
    }

    private fun handleNfcIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag?.techList?.contains("android.nfc.tech.IsoDep")!!) {
            if (!documentNumber.isNullOrBlank()
                    && !expirationDate.isNullOrBlank()
                    && !birthDate.isNullOrBlank()
            ) {
                val bacKey: BACKeySpec = BACKey(documentNumber, birthDate, expirationDate)
                Log.i(TAG, "Creating BACKey $bacKey")
                enableLoadingView()
                readDocument(IsoDep.get(tag), bacKey)
            } else if(!can.isNullOrBlank()) {
                val canKey = PACEKeySpec.createCANKey(can)
                Log.i(TAG, "Creating PACE Can key $canKey")
                enableLoadingView()
                readDocument(IsoDep.get(tag), canKey)
            } else {
                Toast.makeText(this, "Brak danych niezbędnych do odczytu E-dowodu", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun readDocument(isoDep: IsoDep, key: AccessKeySpec) {
        CoroutineScope(Dispatchers.IO).launch {
            isoDep.timeout = NFC_TIMEOUT
            val cardService = CardService.getInstance(isoDep)
            val passportService = PassportService(cardService, NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_BUFFER_SIZE, true, false)
            passportService.open()
            var paceSucceeded = false;
            try {
               val cardAccessFile = CardAccessFile(passportService.getInputStream(PassportService.EF_CARD_ACCESS))
                for (securityInfo in cardAccessFile.securityInfos) {
                    if (securityInfo is PACEInfo) {
                        authorizeWithPACE(passportService, key, securityInfo)
                        paceSucceeded = true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, e)
                updateLoadingWithText(R.string.edo_nfc_error_pace)
                return@launch
            }

            passportService.sendSelectApplet(paceSucceeded)
//            if (!paceSucceeded) {
//                authorizeWithBAC(passportService, bacKey)
//            }
            Log.i(TAG, "Accessing DG1 data")
            updateLoadingWithText(R.string.edo_nfc_dg1_started)
            val dg1 = getDataGroup1FromDocument(passportService)
            Log.i(TAG, "Accessing DG11 data")
            val dg11 = getDataGroup11FromDocument(passportService)
            showResult(dg1, dg11)

        }
    }

    private fun getDataGroup1FromDocument(passportService: PassportService): DG1File {
        val dataGroup = passportService.getInputStream(PassportService.EF_DG1)
        return DG1File(dataGroup)
    }

    private fun getDataGroup2FromDocument(passportService: PassportService): DG2File {
        val dataGroup = passportService.getInputStream(PassportService.EF_DG2)
        return DG2File(dataGroup)
    }

    private fun getDataGroup11FromDocument(passportService: PassportService): DG11File {
        val dataGroup = passportService.getInputStream(PassportService.EF_DG11)
        return DG11File(dataGroup)
    }

    private fun authorizeWithPACE(passportService: PassportService, bacKey: AccessKeySpec, securityInfo: PACEInfo) {
        updateLoadingWithText(R.string.edo_nfc_pace_started)
        //PACEKeySpec.createCANKey(can)
        val paceResult = passportService.doPACE(
                bacKey,
                securityInfo.objectIdentifier,
                PACEInfo.toParameterSpec(securityInfo.parameterId),
                null
        )
        Log.i(TAG, "PACE successful $paceResult")
        updateLoadingWithText(R.string.edo_nfc_dg1_started)
    }

//    private fun authorizeWithBAC(passportService: PassportService, bacKey: BACKeySpec) {
//        try {
//            passportService.doBAC(bacKey)
//        } catch (e: Exception) {
//            Log.w(TAG, e)
//            disableLoadingView()
//            CoroutineScope(Dispatchers.Main).launch {
//                Toast.makeText(this@EdoAppAuthActivity, R.string.edo_nfc_error_bac, Toast.LENGTH_LONG).show()
//                NavUtils.navigateUpFromSameTask(this@EdoAppAuthActivity)
//
//            }
//        }
//    }

    private fun showResult(dg1: DG1File, dg11: DG11File) {
        val mrzInfo = dg1.mrzInfo
        disableLoadingView()
        Log.i(TAG, "MRZInfo: $mrzInfo")
        Log.i(TAG, "Primary " + mrzInfo.primaryIdentifier)
        Log.i(TAG, "Secondary " + mrzInfo.secondaryIdentifier)
        Log.i(TAG, "BirthDate " + mrzInfo.dateOfBirth)
        Log.i(TAG, "Nationality " + mrzInfo.nationality)
        Log.i(TAG, "ISS " + mrzInfo.issuingState)
        //todo dodać zdjęcie

        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@EdoAppAuthActivity, getString(R.string.edo_download_complete_toast), Toast.LENGTH_SHORT).show()
            val resultIntent = Intent(baseContext, EdoResultActivity::class.java)
            resultIntent.putExtra(getString(R.string.intent_first_name), mrzInfo.secondaryIdentifier.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_last_name), mrzInfo.primaryIdentifier.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_birth_date), dg11.fullDateOfBirth.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_nationality), mrzInfo.nationality.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_gender), mrzInfo.gender.name)
            resultIntent.putExtra(getString(R.string.intent_personal_number), dg11.personalNumber.replace("<", ""))
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            baseContext.startActivity(resultIntent)
        }
    }


    private fun enableLoadingView() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.loadingText.text = getString(R.string.edo_nfc_started)
            binding.loadingText.visibility = View.VISIBLE
            ObjectAnimator.ofInt(binding.loadingImage, "progress", 10,25,50,75,100)
                .setDuration(NFC_TIMEOUT.toLong())
                .start()
            binding.loadingImage.visibility = View.VISIBLE
        }
    }

    private fun updateLoadingWithText(resId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.loadingText.text = getString(resId)
        }
    }

    private fun disableLoadingView() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.loadingText.text = ""
            binding.loadingText.visibility = View.INVISIBLE
            binding.loadingImage.clearAnimation()
            binding.loadingImage.visibility = View.INVISIBLE
        }
    }

    private fun expirationDateEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank() && text.matches(Regex("^\\d{6}$"))) {
            expirationDate = text.toString()
        }
    }

    private fun dateOfBirthEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank() && text.matches(Regex("^\\d{6}$"))) {
            birthDate = text.toString()
        }
    }

    private fun documentNumberEditHandler(text: CharSequence?) {
        if (!text.isNullOrBlank()) {
            documentNumber = text.toString()
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
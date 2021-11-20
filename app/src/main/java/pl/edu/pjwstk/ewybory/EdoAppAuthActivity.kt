package pl.edu.pjwstk.ewybory

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sf.scuba.smartcards.CardService
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec
import org.jmrtd.PACEKeySpec
import org.jmrtd.PassportService
import org.jmrtd.PassportService.NORMAL_MAX_TRANCEIVE_LENGTH
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.CardSecurityFile
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.MRZInfo
import org.spongycastle.jce.provider.BouncyCastleProvider
import pl.edu.pjwstk.ewybory.databinding.ActivityEdoAppAuthBinding
import java.security.Security


class EdoAppAuthActivity : AppCompatActivity() {
    private val TAG: String = EdoAppAuthActivity::class.java.simpleName
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var passportNumber: String
    private lateinit var birthDate: String
    private lateinit var expirationDate: String
    private lateinit var binding: ActivityEdoAppAuthBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoAppAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        passportNumber = "DCX899637";
//        date format MRZ date YYMMDD
        birthDate = "961127"
        expirationDate = "310803"
//        birthDate = "271196"
//        expirationDate = "030831"
        Security.insertProviderAt(BouncyCastleProvider(), 1)

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
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag?.techList?.contains("android.nfc.tech.IsoDep")!!) {
                if (passportNumber != null && passportNumber.isNotEmpty()
                    && expirationDate != null && expirationDate.isNotEmpty()
                    && birthDate != null && birthDate.isNotEmpty()
                ) {
                    val bacKey: BACKeySpec = BACKey(passportNumber, birthDate, expirationDate)
                    Log.i(TAG, "Creating BACKey $bacKey")
                    binding.loadingImage.visibility = View.VISIBLE
                    readDocument(IsoDep.get(tag), bacKey)
                }
            }
        }
    }

    private fun readDocument(isoDep: IsoDep, bacKey: BACKeySpec) {
        CoroutineScope(Dispatchers.IO).launch {


            isoDep.timeout = 10*1000
            val cardService = CardService.getInstance(isoDep)
//            cardService.open()

            val passportService = PassportService(cardService, NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_BUFFER_SIZE, true, false)
            passportService.open()
            var paceSucceeded = false;
            try {
               val cardAccessFile = CardAccessFile(passportService.getInputStream(PassportService.EF_CARD_ACCESS))
                for (securityInfo in cardAccessFile.securityInfos) {
                    if (securityInfo is PACEInfo) {
                        val paceResult = passportService.doPACE(
                            bacKey,
                            securityInfo.objectIdentifier,
                            PACEInfo.toParameterSpec(securityInfo.parameterId),
                            null
                        )
                        paceSucceeded = true
                        Log.i(TAG, "PACE successful $paceResult")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, e)
            }

            passportService.sendSelectApplet(paceSucceeded)
            if (!paceSucceeded) {
                try {
                    passportService.getInputStream(PassportService.EF_COM).read()
                } catch (e: Exception) {
                    passportService.doBAC(bacKey)
                }
            }
            Log.i(TAG, "Accessing DG1 data")
            val dg1In = passportService.getInputStream(PassportService.EF_DG1)
            val mrzInfo = DG1File(dg1In).mrzInfo
            logMrzinfo(mrzInfo)

        }
    }

    private fun logMrzinfo(mrzInfo: MRZInfo) {
        Log.i(TAG, "Primary " + mrzInfo.primaryIdentifier)
        Log.i(TAG, "Secondary " + mrzInfo.secondaryIdentifier)
        Log.i(TAG, "BirthDate " + mrzInfo.dateOfBirth)
        Log.i(TAG, "Nationality " + mrzInfo.nationality)
        Log.i(TAG, "ISS " + mrzInfo.issuingState)
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@EdoAppAuthActivity, "Primary " + mrzInfo.primaryIdentifier, Toast.LENGTH_LONG).show()
            Toast.makeText(this@EdoAppAuthActivity, "Secondary " + mrzInfo.secondaryIdentifier, Toast.LENGTH_LONG).show()
            Toast.makeText(this@EdoAppAuthActivity, "BirthDate " + mrzInfo.dateOfBirth, Toast.LENGTH_LONG).show()
            Toast.makeText(this@EdoAppAuthActivity, "Nationality " + mrzInfo.nationality, Toast.LENGTH_LONG).show()
            Toast.makeText(this@EdoAppAuthActivity, "ISS " + mrzInfo.issuingState, Toast.LENGTH_LONG).show()
        }
        binding.loadingImage.visibility = View.INVISIBLE
    }

}
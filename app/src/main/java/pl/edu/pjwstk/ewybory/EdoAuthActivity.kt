package pl.edu.pjwstk.ewybory

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sf.scuba.smartcards.CardService
import org.jmrtd.*
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.icao.DG11File
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.DG2File
import org.spongycastle.jce.provider.BouncyCastleProvider
import pl.edu.pjwstk.ewybory.databinding.ActivityEdoAuthBinding
import pl.edu.pjwstk.ewybory.ui.edo.EdoCanViewModel
import pl.edu.pjwstk.ewybory.ui.edo.EdoDocumentViewModel
import pl.edu.pjwstk.ewybory.ui.edo.SectionsPagerAdapter
import java.security.Security

class EdoAuthActivity : AppCompatActivity() {

    private val NFC_TIMEOUT = 10 * 1000

    private val TAG: String = EdoAuthActivity::class.java.simpleName
    private lateinit var nfcAdapter: NfcAdapter
    private var documentKey = DocumentKey()
    private var canKey = CANKey();
    private lateinit var binding: ActivityEdoAuthBinding
    private lateinit var canViewModel: EdoCanViewModel
    private lateinit var documentViewModel: EdoDocumentViewModel
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdoAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        canViewModel = ViewModelProvider(this).get(EdoCanViewModel::class.java)
        documentViewModel = ViewModelProvider(this).get(EdoDocumentViewModel::class.java)
        Log.i(TAG, "Current item ${viewPager.currentItem}")
        addViewModelsListeners()
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
            if (isDocumentFragmentVisible() && documentKey.isPrepared()) {
                val bacKey: BACKeySpec = documentKey.createBACKey()
                Log.i(TAG, "Creating BACKey $bacKey")
                enableLoadingView()
                readDocument(IsoDep.get(tag), bacKey)
            } else if(isCanFragmentVisible() && canKey.isPrepared()) {
                val canKey = canKey.createPACEKey()
                Log.i(TAG, "Creating PACE Can key $canKey")
                enableLoadingView()
                readDocument(IsoDep.get(tag), canKey)
            } else {
                Toast.makeText(this, getString(R.string.error_not_enough_data_for_authentication), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readDocument(isoDep: IsoDep, key: AccessKeySpec) {
        CoroutineScope(Dispatchers.IO).launch {
            isoDep.timeout = NFC_TIMEOUT
            val cardService = CardService.getInstance(isoDep)
            val passportService = PassportService(cardService, PassportService.NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_BUFFER_SIZE, true, false)
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
            Log.i(TAG, "Accessing DG1 data")
            updateLoadingWithText(R.string.edo_nfc_dg1_started)
            val dg1 = getDataGroup1FromDocument(passportService)
            Log.i(TAG, "Accessing DG2 data")
            val dg2 = getDataGroup2FromDocument(passportService)
            Log.i(TAG, "Accessing DG11 data")
            val dg11 = getDataGroup11FromDocument(passportService)
            showResult(dg1, dg2, dg11)
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
        val paceResult = passportService.doPACE(
            bacKey,
            securityInfo.objectIdentifier,
            PACEInfo.toParameterSpec(securityInfo.parameterId),
            null
        )
        Log.i(TAG, "PACE successful $paceResult")
        updateLoadingWithText(R.string.edo_nfc_dg1_started)
    }

    private fun showResult(dg1: DG1File,dg2: DG2File, dg11: DG11File) {
        val mrzInfo = dg1.mrzInfo
        disableLoadingView()
        Log.i(TAG, "MRZInfo: $mrzInfo")

        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@EdoAuthActivity, getString(R.string.edo_download_complete_toast), Toast.LENGTH_SHORT).show()
            val resultIntent = Intent(baseContext, EdoResultActivity::class.java)
            resultIntent.putExtra(getString(R.string.intent_first_name), mrzInfo.secondaryIdentifier.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_last_name), mrzInfo.primaryIdentifier.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_birth_date), dg11.fullDateOfBirth.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_nationality), mrzInfo.nationality.replace("<", ""))
            resultIntent.putExtra(getString(R.string.intent_gender), mrzInfo.gender.name)
            resultIntent.putExtra(getString(R.string.intent_personal_number), dg11.personalNumber.replace("<", ""))
            if (dg2?.faceInfos?.get(0)?.faceImageInfos?.size ?: 0 != 0) {
                resultIntent.putExtra(getString(R.string.intent_photo),dg2.faceInfos?.get(0)?.faceImageInfos?.get(0)?.imageInputStream?.readBytes())
            }
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            baseContext.startActivity(resultIntent)
        }
    }


    private fun isCanFragmentVisible(): Boolean {
        return viewPager.currentItem == 0
    }

    private fun isDocumentFragmentVisible(): Boolean {
        return viewPager.currentItem == 1
    }

    private fun addViewModelsListeners() {
        canViewModel.storedCan.observe(this, Observer { this.canKey.setCAN(it) })
        documentViewModel.storedDocumentNumber.observe(this, Observer { this.documentKey.setDocumentNumber(it) })
        documentViewModel.storedBirthDate.observe(this, Observer { this.documentKey.setBirthDate(it) })
        documentViewModel.storedExpirationDate.observe(this, Observer { this.documentKey.setExpirationDate(it) })

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
}
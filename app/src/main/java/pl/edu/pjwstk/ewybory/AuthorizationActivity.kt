package pl.edu.pjwstk.ewybory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.spongycastle.jce.provider.BouncyCastleProvider
import pl.edu.pjwstk.ewybory.databinding.ActivityAuthorizationBinding
import java.security.Security

class AuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun edoAppButtonClickHandler(view: View) {
        val edoAppAuthorizationIntent = Intent(this, EdoAppAuthActivity::class.java)
        edoAppAuthorizationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(edoAppAuthorizationIntent)
    }
}
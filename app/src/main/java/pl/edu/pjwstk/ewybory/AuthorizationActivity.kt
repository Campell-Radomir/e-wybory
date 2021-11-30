package pl.edu.pjwstk.ewybory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pl.edu.pjwstk.ewybory.databinding.ActivityAuthorizationBinding

class AuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun edoAppButtonClickHandler(view: View) {
        val edoAppAuthorizationIntent = Intent(this, EdoAuthActivity::class.java)
        edoAppAuthorizationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(edoAppAuthorizationIntent)
    }

    fun passportButtonClickHandler(view: View) {
        val edoAppAuthorizationIntent = Intent(this, PassportAuthActivity::class.java)
        edoAppAuthorizationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(edoAppAuthorizationIntent)
    }
}
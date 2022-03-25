package pl.edu.pjwstk.ewybory

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_light_theme -> changeTheme(AppCompatDelegate.MODE_NIGHT_NO)
            R.id.menu_dark_theme -> changeTheme(AppCompatDelegate.MODE_NIGHT_YES)
            R.id.menu_system_theme -> changeTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeTheme(themeMode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        delegate.applyDayNight()
        return true
    }
}
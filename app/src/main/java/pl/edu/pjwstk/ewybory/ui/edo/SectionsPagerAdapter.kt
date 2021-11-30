package pl.edu.pjwstk.ewybory.ui.edo

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.edu.pjwstk.ewybory.R

private val TAB_TITLES = arrayOf(
        R.string.edo_tab_can,
        R.string.edo_tab_data
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> EdoCanFragment.newInstance()
            1 -> EdoDocumentFragment.newInstance()
            else -> throw RuntimeException("Unknown Position")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}
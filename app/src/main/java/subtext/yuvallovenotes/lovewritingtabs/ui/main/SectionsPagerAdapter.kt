package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import subtext.yuvallovenotes.R

private val TABS = listOf(
        Pair(R.string.tab_title_love_generator_frag, LoveGeneratorFragment.newInstance(1)),
        Pair(R.string.tab_title_love_writer_frag, LoveWriterFragment.newInstance(2)),
        Pair(R.string.tab_title_love_generator_frag, LoveEditorFragment.newInstance(3)))

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return TABS[position].second
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TABS[position].first)
    }

    override fun getCount(): Int {
        return TABS.size
    }
}
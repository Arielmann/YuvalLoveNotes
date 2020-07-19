package subtext.yuvallovenotes.lovewritingtabs.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import subtext.yuvallovenotes.R
import java.lang.IllegalStateException

private val TAB_TITLES = arrayOf(
        R.string.tab_title_love_generator_frag,
        R.string.tab_title_love_writer_frag,
        R.string.tab_title_love_editor_frag
)


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val loveGeneratorFragment = LoveGeneratorFragment.newInstance(1)
    private val loveWriterFragment = LoveWriterFragment.newInstance(2)
    private val loveEditorFragment = LoveEditorFragment.newInstance(3)

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        when (TAB_TITLES[position]) {
            R.string.tab_title_love_generator_frag -> if(!loveGeneratorFragment.isAdded) return loveGeneratorFragment
            R.string.tab_title_love_writer_frag -> if(!loveWriterFragment.isAdded) return loveWriterFragment
            R.string.tab_title_love_editor_frag ->  if(!loveEditorFragment.isAdded) return loveEditorFragment
        }
        throw IllegalStateException("Fragment Not Defined")

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}
package subtext.yuvallovenotes.registration.ui/* This file is auto-generated from OnboardingDemoFragment.java.  DO NOT MODIFY. */ /*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.leanback.app.OnboardingSupportFragment
import androidx.navigation.fragment.findNavController
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import java.util.*

class OnboardingDemoSupportFragment : OnboardingSupportFragment() {

    companion object {
        private val TAG = OnboardingDemoSupportFragment::class.java.simpleName
        private const val ANIMATION_DURATION: Long = 500
        private val CONTENT_IMAGES = intArrayOf(
                R.drawable.new_letter_icon,
                R.drawable.love_letter_image,
                R.drawable.lovers_logo
        )
    }


    private var titles: MutableList<String> = LoveUtils.getAllItemsFromArrayFile(R.array.onboarding_page_titles)
    private var descriptions: MutableList<String> = LoveUtils.getAllItemsFromArrayFile(R.array.onboarding_page_descriptions)
    private lateinit var mBackgroundView: View
    private lateinit var mContentView: ImageView
    private val mImage1: ImageView? = null
    private val mImage2: ImageView? = null
    private var mContentAnimator: Animator? = null


    override fun getPageCount(): Int {
        return titles.size
    }

    override fun getPageTitle(i: Int): CharSequence {
        return titles[i]
    }

    override fun getPageDescription(i: Int): CharSequence {
        return descriptions[i]
    }

    override fun onProvideTheme(): Int {
        return R.style.Theme_AppCompat_Leanback_Onboarding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        logoResourceId = R.drawable.splash_screen
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateBackgroundView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        mBackgroundView = layoutInflater.inflate(R.layout.onboarding_image, viewGroup, false)
        return mBackgroundView
    }

    override fun onCreateContentView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View {
        mContentView = layoutInflater.inflate(R.layout.onboarding_image, viewGroup, false) as ImageView
//        mContentView = rootView.findViewById(R.id.onboardingIV)
        val layoutParams = mContentView.layoutParams as MarginLayoutParams
        layoutParams.topMargin = 30
        layoutParams.bottomMargin = 60
        return mContentView
    }

    override fun onCreateForegroundView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View? {
        return null
    }

    override fun onCreateEnterAnimation(): Animator {
        val animators = ArrayList<Animator?>()
        animators.add(createFadeInAnimator(mBackgroundView))
        mContentView.setImageResource(CONTENT_IMAGES[currentPageIndex])
        mContentAnimator = createFadeInAnimator(mContentView)
        animators.add(mContentAnimator)
        val set = AnimatorSet()
        set.playTogether(animators)
        return set
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        if (mContentAnimator != null) {
            mContentAnimator!!.end()
        }
        val animators = ArrayList<Animator>()
        val fadeOut = createFadeOutAnimator(mContentView)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mContentView.setImageResource(CONTENT_IMAGES[currentPageIndex])
            }
        })
        animators.add(fadeOut)
        animators.add(createFadeInAnimator(mContentView))
        val set = AnimatorSet()
        set.playSequentially(animators)
        set.start()
        mContentAnimator = set
    }

    private fun createFadeInAnimator(view: View?): Animator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(ANIMATION_DURATION)
    }

    private fun createFadeOutAnimator(view: View?): Animator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(ANIMATION_DURATION)
    }

    override fun onFinishFragment() {
        super.onFinishFragment()
        findNavController().navigate(OnboardingDemoSupportFragmentDirections.navigateToEnterUserDetails())
    }
}
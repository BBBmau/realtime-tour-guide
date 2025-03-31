package com.mau.exploreai

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// TutorialPagerAdapter.kt
class TutorialPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val pages = listOf(
        TutorialPage("Welcome!", "This is a brief introduction to our app."),
        TutorialPage("Features", "Discover all the amazing features."),
        TutorialPage("Get Started", "Let's set up your profile!")
    )

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return TutorialFragment.newInstance(pages[position])
    }
}

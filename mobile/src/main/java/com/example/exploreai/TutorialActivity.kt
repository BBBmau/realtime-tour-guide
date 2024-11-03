package com.example.exploreai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityTutorialBinding
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator

// TutorialActivity.kt
class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    private lateinit var pagerAdapter: TutorialPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        pagerAdapter = TutorialPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Add dots indicator
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { tab, _ ->
            tab.setIcon(R.drawable.dot_indicator)
        }.attach()
    }

    private fun setupButtons() {
        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < pagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, UserInfoActivity::class.java))
                finish()
            }
        }

        binding.skipButton.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
            finish()
        }
    }
}
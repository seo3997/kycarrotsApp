package com.whomade.kycarrots

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.whomade.kycarrots.ui.ad.adreview.ProductReviewFragment
import com.whomade.kycarrots.ui.ad.adqna.ProductQnaFragment

class AdDetailPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductDescriptionFragment()
            1 -> ProductReviewFragment()
            else -> ProductQnaFragment()
        }
    }
}

package com.kfo00.currencyexchange

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kfo00.currencyexchange.fragments.ExchangeFragment
import com.kfo00.currencyexchange.fragments.TransactionsFragment
import com.kfo00.currencyexchange.fragments.StatisticsFragment
import com.kfo00.currencyexchange.fragments.UserTransactionsFragment

class TabsPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ExchangeFragment()
            }
            1 -> {
                StatisticsFragment()
            }
            2 -> {
                UserTransactionsFragment()
            }
            3 -> {
                TransactionsFragment()
            }
            else -> ExchangeFragment()
        }
    }
    override fun getItemCount(): Int {
        return 4
    }
    }
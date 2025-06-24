package com.hasnain.usermoduleupdated.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
class DepthScaleTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        when {
            position < -1 -> {
                // Page is way off-screen to the left
                page.alpha = 0f
            }
            position <= 1 -> {
                val scaleFactor = 0.85f + (1 - abs(position)) * 0.15f
                val alphaFactor = 0.5f + (1 - abs(position)) * 0.5f

                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
                page.alpha = alphaFactor

                // Smooth horizontal slide
                page.translationX = page.width * -position * 0.2f
            }
            else -> {
                // Page is way off-screen to the right
                page.alpha = 0f
            }
        }
    }
}

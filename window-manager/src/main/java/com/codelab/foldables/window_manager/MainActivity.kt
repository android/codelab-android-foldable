/*
 *
 *  * Copyright (C) 2021 Google Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.codelab.foldables.window_manager

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import com.codelab.foldables.window_manager.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var windowInfoRepository: WindowInfoRepository
    private val scope = MainScope()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowInfoRepository = windowInfoRepository()

        obtainWindowMetrics()
        onWindowLayoutInfoChange(windowInfoRepository)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun obtainWindowMetrics() {
        val wmc = WindowMetricsCalculator.getOrCreate()
        binding.windowMetrics.text =
            "CurrentWindowMetrics: ${wmc.computeCurrentWindowMetrics(this).bounds.flattenToString()}\n" +
                "MaximumWindowMetrics: ${wmc.computeMaximumWindowMetrics(this).bounds.flattenToString()}"
    }

    private fun onWindowLayoutInfoChange(windowInfoRepository: WindowInfoRepository) {
        scope.launch {
            windowInfoRepository.windowLayoutInfo.collect { value ->
                updateUI(value)
            }
        }
    }

    private fun updateUI(newLayoutInfo: WindowLayoutInfo) {
        binding.layoutChange.text = newLayoutInfo.toString()
        if (newLayoutInfo.displayFeatures.isNotEmpty()) {
            binding.configurationChanged.text = "Spanned across displays"
            alignViewToDeviceFeatureBoundaries(newLayoutInfo)
        } else {
            binding.configurationChanged.text = "One logic/physical display - unspanned"
        }
    }

    private fun alignViewToDeviceFeatureBoundaries(newLayoutInfo: WindowLayoutInfo) {
        val constraintLayout = binding.constraintLayout
        val set = ConstraintSet()
        set.clone(constraintLayout)

        //We get the folding feature bounds.
        val foldingFeature = newLayoutInfo.displayFeatures[0] as FoldingFeature
        val rect = foldingFeature.bounds

        //Sets the view to match the height and width of the folding feature
        set.constrainHeight(
            R.id.folding_feature,
            rect.bottom - rect.top
        )
        set.constrainWidth(R.id.folding_feature, rect.right - rect.left)

        set.connect(
            R.id.folding_feature, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START, 0
        )
        set.connect(
            R.id.folding_feature, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0
        )

        if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
            set.setMargin(R.id.folding_feature, ConstraintSet.START, rect.left)
            set.connect(
                R.id.layout_change, ConstraintSet.END,
                R.id.folding_feature, ConstraintSet.START, 0
            )
        } else {
            //FoldingFeature is Horizontal
            val statusBarHeight = calculateStatusBarHeight()
            val toolBarHeight = calculateToolbarHeight()
            set.setMargin(
                R.id.folding_feature, ConstraintSet.TOP,
                rect.top - statusBarHeight - toolBarHeight
            )
            set.connect(
                R.id.layout_change, ConstraintSet.TOP,
                R.id.folding_feature, ConstraintSet.BOTTOM, 0
            )
        }

        //Set the view to visible and apply constraints
        set.setVisibility(R.id.folding_feature, View.VISIBLE)
        set.applyTo(constraintLayout)
    }

    private fun calculateToolbarHeight(): Int {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            0
        }
    }

    private fun calculateStatusBarHeight(): Int {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }
}

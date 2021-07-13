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
import androidx.window.FoldingFeature
import androidx.window.WindowInfoRepo
import androidx.window.WindowLayoutInfo
import androidx.window.rxjava2.currentWindowMetricsFlowable
import androidx.window.windowInfoRepository
import com.codelab.foldables.window_manager.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var windowInfoRepo: WindowInfoRepo
    private val scope = MainScope()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowInfoRepo = windowInfoRepository()

        obtainWindowMetrics(windowInfoRepo)
        onWindowLayoutInfo(windowInfoRepo, ::updateUI)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun obtainWindowMetrics(windowInfoRepo: WindowInfoRepo) {
        // Using Flowables as a way to get the window metrics
        val metricsFlowable = windowInfoRepo.currentWindowMetricsFlowable()
        metricsFlowable.subscribe {
            binding.windowMetrics.text =
                "CurrentWindowMetrics: ${it.bounds.flattenToString()}\n" +
                    "MaximumWindowMetrics: ${windowInfoRepo.maximumWindowMetrics.bounds.flattenToString()}"
        }
    }

    private fun onWindowLayoutInfo(
        windowInfoRepo: WindowInfoRepo,
        updateUI: (windowLayoutInfo: WindowLayoutInfo) -> Unit
    ) {
        // Using coroutines as a way to get the window layout info
        scope.launch {
            windowInfoRepo.windowLayoutInfo
                .onEach { value -> updateUI(value) }
                .collect()
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
        val foldingFeature = newLayoutInfo.displayFeatures.get(0) as FoldingFeature
        val rect = foldingFeature.bounds

        //Sets the view to match the height and width of the device feature
        set.constrainHeight(
            R.id.device_feature,
            rect.bottom - rect.top
        )
        set.constrainWidth(R.id.device_feature, rect.right - rect.left)

        set.connect(
            R.id.device_feature, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START, 0
        )
        set.connect(
            R.id.device_feature, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0
        )

        if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
            set.setMargin(R.id.device_feature, ConstraintSet.START, rect.left)
            set.connect(
                R.id.layout_change, ConstraintSet.END,
                R.id.device_feature, ConstraintSet.START, 0
            )
        } else {
            val statusBarHeight = calculateStatusBarHeight()
            val toolBarHeight = calculateToolbarHeight()
            set.setMargin(
                R.id.device_feature, ConstraintSet.TOP,
                rect.top - statusBarHeight - toolBarHeight
            )
            set.connect(
                R.id.layout_change, ConstraintSet.TOP,
                R.id.device_feature, ConstraintSet.BOTTOM, 0
            )
        }

        //Set the view to visible and apply constraints
        set.setVisibility(R.id.device_feature, View.VISIBLE)
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

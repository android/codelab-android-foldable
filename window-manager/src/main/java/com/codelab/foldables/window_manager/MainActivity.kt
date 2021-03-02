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
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.util.Consumer
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.codelab.foldables.window_manager.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var wm: WindowManager
    private val layoutStateChangeCallback = LayoutStateChangeCallback()
    private lateinit var binding: ActivityMainBinding

    private fun runOnUiThreadExecutor(): Executor {
        val handler = Handler(Looper.getMainLooper())
        return Executor() {
            handler.post(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wm = WindowManager(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        wm.registerLayoutChangeCallback(
            runOnUiThreadExecutor(),
            layoutStateChangeCallback
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        wm.unregisterLayoutChangeCallback(layoutStateChangeCallback)
    }

    private fun printLayoutStateChange(newLayoutInfo: WindowLayoutInfo) {
        binding.windowMetrics.text =
            "CurrentWindowMetrics: ${wm.currentWindowMetrics.bounds.flattenToString()}\n" +
                "MaximumWindowMetrics: ${wm.maximumWindowMetrics.bounds.flattenToString()}"

        binding.layoutChange.text = newLayoutInfo.toString()
        if (newLayoutInfo.displayFeatures.size > 0) {
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

        //We get the display feature bounds.
        val rect = newLayoutInfo.displayFeatures[0].bounds

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

        if (rect.top == 0) {
            // Device feature is placed vertically
            set.setMargin(R.id.device_feature, ConstraintSet.START, rect.left)
            set.connect(
                R.id.layout_change, ConstraintSet.END,
                R.id.device_feature, ConstraintSet.START, 0
            )
        } else {
            //Device feature is placed horizontally
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

    inner class LayoutStateChangeCallback : Consumer<WindowLayoutInfo> {
        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            printLayoutStateChange(newLayoutInfo)
        }
    }
}

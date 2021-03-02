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

package com.codelab.foldables.drop

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.codelab.foldables.drop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnDragListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.dropEditText.setOnDragListener(this)
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                    setDragStartedBackground()
                    true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                setDragEnteredBackground()
                true
            }
            DragEvent.ACTION_DROP -> {
                handleDrop(event)
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                clearBackgroundColor()
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                setDragStartedBackground()
                true
            }
            else -> false
        }
    }

    private fun handleDrop(event: DragEvent) {
        val dropPermissions = requestDragAndDropPermissions(event)
        val item = event.clipData.getItemAt(0)
        val dragData = item.text.toString()
        binding.dropEditText.setText(dragData)
        dropPermissions?.release()
    }

    private fun clearBackgroundColor() {
        with (binding.dropEditText) {
            background.clearColorFilter()
            hint = getString(R.string.default_hint)
            elevation = 0f
            invalidate()
        }
    }

    private fun setDragStartedBackground() {
        val colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        with (binding.dropEditText) {
            hint = getString(R.string.move_text_to_drop)
            background.colorFilter = colorFilter
            elevation = 4f
            invalidate()
        }
    }

    private fun setDragEnteredBackground() {
        val colorFilter = PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
        with (binding.dropEditText) {
            hint = getString(R.string.drop_text_now)
            background.colorFilter = colorFilter
            elevation = 4f
            invalidate()
        }
    }
}

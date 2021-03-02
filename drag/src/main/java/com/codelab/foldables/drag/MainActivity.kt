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

package com.codelab.foldables.drag

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codelab.foldables.drag.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnLongClickListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dragTextView.tag = "text_view"
        binding.dragTextView.setOnLongClickListener(this)
    }

    override fun onLongClick(view: View): Boolean {
        return if (view is TextView) {
            val text = ClipData.Item(view.text)
            val mimeType = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val dataToShare = ClipData(view.tag.toString(), mimeType, text)
            val dragShadowBuilder = View.DragShadowBuilder(view)

            //These flags are needed to share between apps
            val flags =
                View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ

            view.startDragAndDrop(dataToShare, dragShadowBuilder, view, flags)
            true
        } else {
            false
        }
    }
}

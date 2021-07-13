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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.testing.WindowLayoutInfoPublisherRule
import androidx.window.windowInfoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class WMTest {
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private val publisherRule = WindowLayoutInfoPublisherRule()

    @get:Rule
    val testRule: TestRule

    init {
        testRule = RuleChain.outerRule(publisherRule).around(activityRule)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun test_that_WindowInfoRepo_has_the_correct_info() {
        runBlockingTest {
            val hinge = FoldingFeature(
                bounds = Rect(1350, 0, 1434, 1800),
                type = FoldingFeature.Type.HINGE,
                state = FoldingFeature.State.FLAT
            )
            val expected = WindowLayoutInfo.Builder().setDisplayFeatures(listOf(hinge)).build()
            activityRule.scenario.onActivity { activity ->
                val values = mutableListOf<WindowLayoutInfo>()
                val value = async {
                    activity.windowInfoRepository().windowLayoutInfo.take(1).toCollection(values)
                }
                publisherRule.overrideWindowLayoutInfo(expected)
                runBlockingTest {
                    assertEquals(
                        listOf(expected),
                        value.await().toList()
                    )
                }
            }
        }
    }
}
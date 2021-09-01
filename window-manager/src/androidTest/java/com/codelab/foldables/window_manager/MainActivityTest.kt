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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoRepository.Companion.windowInfoRepository
import androidx.window.layout.WindowLayoutInfo
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private val publisherRule = WindowLayoutInfoPublisherRule()

    private val testScope = TestCoroutineScope()

    @get:Rule
    val testRule: TestRule

    init {
        testRule = RuleChain.outerRule(publisherRule).around(activityRule)
    }

    @Test
    fun testText_is_left_of_Vertical_FoldingFeature() = runBlockingTest {
        activityRule.scenario.onActivity { activity ->
            val hinge = FoldingFeature(
                activity = activity,
                state = FoldingFeature.State.FLAT,
                orientation = FoldingFeature.Orientation.VERTICAL,
                size = 2
            )
            val expected =
                WindowLayoutInfo.Builder().setDisplayFeatures(listOf(hinge)).build()

            val value = testScope.async {
                activity.windowInfoRepository().windowLayoutInfo.first()
            }
            publisherRule.overrideWindowLayoutInfo(expected)
            runBlockingTest {
                assertEquals(
                    expected,
                    value.await()
                )
            }
        }
        onView(withId(R.id.layout_change)).check(
            PositionAssertions.isCompletelyLeftOf(withId(R.id.folding_feature))
        )
    }

    @Test
    fun testText_is_below_of_Horizontal_FoldingFeature() = runBlockingTest {
        activityRule.scenario.onActivity { activity ->
            val hinge = FoldingFeature(
                activity = activity,
                state = FoldingFeature.State.FLAT,
                orientation = FoldingFeature.Orientation.HORIZONTAL,
                size = 2
            )
            val expected =
                WindowLayoutInfo.Builder().setDisplayFeatures(listOf(hinge)).build()

            val value = testScope.async {
                activity.windowInfoRepository().windowLayoutInfo.first()
            }
            publisherRule.overrideWindowLayoutInfo(expected)
            runBlockingTest {
                assertEquals(
                    expected,
                    value.await()
                )
            }
        }
        onView(withId(R.id.layout_change)).check(
            PositionAssertions.isCompletelyBelow(withId(R.id.folding_feature))
        )
    }
}
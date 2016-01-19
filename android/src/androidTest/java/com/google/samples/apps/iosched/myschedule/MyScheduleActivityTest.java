/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samples.apps.iosched.myschedule;

import android.content.res.TypedArray;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.model.ScheduleItem;
import com.google.samples.apps.iosched.settings.SettingsUtils;
import com.google.samples.apps.iosched.testutils.MatchersHelper;
import com.google.samples.apps.iosched.util.TimeUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * UI tests for {@link MyScheduleActivity}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyScheduleActivityTest {

    /**
     * A JUnit {@link Rule @Rule} to launch your activity under test. This is a replacement
     * for {@link ActivityInstrumentationTestCase2}.
     * <p/>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link Before @Before} method.
     * <p/>
     * {@link ActivityTestRule} will create and launch of the activity for you and also expose
     * the activity under test. To get a reference to the activity you can use
     * the {@link ActivityTestRule#getActivity()} method.
     */
    @Rule
    public ActivityTestRule<MyScheduleActivity> mActivityRule = new ActivityTestRule<>(
            MyScheduleActivity.class);

    @Before
    public void setStartTimeAndDisableReminders() {
        // Set up time to 3 hours before conference
        TimeUtils.setCurrentTimeRelativeToStartOfConference(
                InstrumentationRegistry.getTargetContext(), TimeUtils.HOUR * 3);

        // Don't show notifications for sessions as they get in the way of the UI
        SettingsUtils.setShowSessionReminders(InstrumentationRegistry.getTargetContext(), false);
    }

    /**
     * This will fail if there is no free slot with available sessions.
     */
    @FlakyTest
    @Test
    public void addEventToMySchedule() {
        // Given a free slot with available sessions
        findFreeSlotWithAvailableSessions();

        // When adding the first available session
        String title = addFirstAvailableSession();

        // Then the session is shown
        onView(withText(title)).check(matches(isDisplayed()));

        // Clean up
        removeSessionFromSchedule(title);
    }

    /**
     * This will fail if there is no free slot with available sessions.
     */
    @FlakyTest
    @Test
    public void removeEventToMySchedule() {
        // Given a session in the schedule
        findFreeSlotWithAvailableSessions();
        String title = addFirstAvailableSession();
        onView(withText(title)).check(matches(isDisplayed()));

        // When the session is removed
        removeSessionFromSchedule(title);

        // Then the session is not shown anymore
        onView(withText(title)).check(matches(not(isDisplayed())));
    }

    /**
     * This finds the first free slow with available session and click on "Browse sessions" so
     * the list of available sessions is now displayed
     */
    private void findFreeSlotWithAvailableSessions() {
        TypedArray ids = InstrumentationRegistry.getTargetContext().getResources()
                .obtainTypedArray(R.array.myschedule_listview_ids);
        int listViewId = ids.getResourceId(1, 0);
        onData(allOf(is(instanceOf(ScheduleItem.class)), new FirstFreeSlotWithAvailableSessionsMatcher()))
                .inAdapterView(withId(listViewId)).onChildView(withId(R.id.browse_sessions))
                .perform(click());
    }

    /**
     * This assumes that the screen shows a list of available sessions. It adds the first one to the
     * schedule then goes back to the schedule screen.
     *
     * @return the title of the session that was added
     *
     */
    private String addFirstAvailableSession() {
        onData(is(instanceOf(Integer.class))).inAdapterView(withId(R.id.collection_view))
                .atPosition(0).perform(click());

        // Session details screen
        String title = MatchersHelper.getText(withId(R.id.session_title));
        onView(withId(R.id.add_schedule_button)).perform(click());

        onView(withContentDescription(InstrumentationRegistry.getTargetContext()
                .getString(R.string.close_and_go_back))).perform(click());
        onView(withContentDescription(InstrumentationRegistry.getTargetContext()
                .getString(R.string.close_and_go_back))).perform(click());

        return title;
    }

    private void removeSessionFromSchedule(String sessionTitle) {
        onView(withText(sessionTitle)).perform(click());
        onView(withId(R.id.add_schedule_button)).perform(click());
        onView(withContentDescription(InstrumentationRegistry.getTargetContext()
                .getString(R.string.close_and_go_back))).perform(click());
    }

}
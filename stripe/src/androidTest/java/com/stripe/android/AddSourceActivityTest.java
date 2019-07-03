package com.stripe.android;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.stripe.android.view.AddSourceActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AddSourceActivityTest {

    @Rule
    public ActivityTestRule<AddSourceActivity> mActivityRule =
            new ActivityTestRule(AddSourceActivity.class);

    @Test
    public void titleRenders() {
        onView(withText(R.string.title_add_a_card)).check(matches(isDisplayed()));
    }

}

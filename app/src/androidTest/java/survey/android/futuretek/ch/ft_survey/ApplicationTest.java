package survey.android.futuretek.ch.ft_survey;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Run test in the right order when running everything at once
public class ApplicationTest {

    /** Perform action of waiting for a specific view id. */
    public static ViewAction waitId(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                final Matcher<View> viewMatcher = withId(viewId);

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                }
                while (System.currentTimeMillis() < endTime);
            }
        };
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void t1EnsureDialogText() {
        // When running all test we need to wait more the first time we are opening the app
        // because the text animation is slower
        onView(isRoot()).perform(waitId(R.id.textView1, TimeUnit.SECONDS.toMillis(10)));
        // Check the text before input is the right one
        onView(withId(R.id.textView1)).check(matches(withText("Name: ")));
    }

    @Test
    public void t2EnsureDialogShowWithNoName() {
        onView(isRoot()).perform(waitId(R.id.userInput, TimeUnit.SECONDS.toMillis(4)));
        // Vaidate the dialog without entering a Name
        onView(withId(R.id.okBtn)).perform(click());

        onView(isRoot()).perform(waitId(R.id.userInput, TimeUnit.SECONDS.toMillis(1)));

        // Check the display of the dialog again
        onView(withId(R.id.textView1)).check(matches(withText("Name: ")));
    }

    @Test
    public void t3EnsureNoNextWithNoName() {
        onView(isRoot()).perform(waitId(R.id.userInput, TimeUnit.SECONDS.toMillis(4)));
        // Vaidate the dialog without entering a Name
        onView(withId(R.id.okBtn)).perform(click());

        // Check that we cannot click on the next button
        onView(withId(R.id.nextBtn)).check(matches(not(isEnabled())));
    }

    @Test
    public void t4EnsureDialogNotShowWithName() {
        onView(isRoot()).perform(waitId(R.id.userInput, TimeUnit.SECONDS.toMillis(4)));
        // Enter "test" as a name
        onView(withId(R.id.userInput)).perform(typeText("test"));
        // Click on the OK button in the dialog
        onView(withId(R.id.okBtn)).perform(click());

        onView(isRoot()).perform(waitId(R.id.textView1, TimeUnit.SECONDS.toMillis(2)));

        // Check that we can click on the next button
        onView(withId(R.id.nextBtn)).check(matches(isEnabled()));
    }
}


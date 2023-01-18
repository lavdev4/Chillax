package com.lavdevapp.chillax

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.test.core.app.ActivityScenario.*
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingResource.ResourceCallback
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask
import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityInstrumentedTest {
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val delayMinutes = 3

    @get:Rule
    val activityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(TimeDelayIdlingResource)
    }

    @Test
    fun testForLongTimeWorkInBackground() {
        enableMainSwitch()
        enableSomeTracks()
        uiDevice.pressHome()
        uiDevice.sleep()
        TimeDelayIdlingResource.setDelay(delayMinutes)
        checkIfServiceRunning(PlayersService::class)
        uiDevice.wakeUp()
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(TimeDelayIdlingResource)
    }

    private fun enableMainSwitch() {
        onView(allOf(withId(R.id.main_switch), isNotChecked()))
            .perform(click())
    }

    private fun enableSomeTracks() {
        val enablePlayerSwitchAction = createRecyclerChildEnableAction(R.id.player_switch)
        onView(withId(R.id.players_recycler_view))
            .perform(
                actionOnItemAtPosition<TracksListAdapter.TracksListViewHolder>(
                    1,
                    enablePlayerSwitchAction
                ),
                actionOnItemAtPosition<TracksListAdapter.TracksListViewHolder>(
                    2,
                    enablePlayerSwitchAction
                ),
                actionOnItemAtPosition<TracksListAdapter.TracksListViewHolder>(
                    5,
                    enablePlayerSwitchAction
                )
            )
    }

    @Suppress("DEPRECATION")
    private fun checkIfServiceRunning(searchedService: KClass<out Service>) {
        val runningServices = (InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Integer.MAX_VALUE)
        onIdle {
            check(runningServices.any { it.service.className == searchedService.qualifiedName })
        }
    }

    private fun createRecyclerChildEnableAction(childId: Int): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String {
                return "click on recycler view child"
            }

            override fun getConstraints(): Matcher<View> {
                return any(View::class.java)
            }

            override fun perform(uiController: UiController?, view: View?) {
                val button = view?.findViewById<CompoundButton>(childId)
                button?.let { if (!it.isChecked) it.performClick() }
            }
        }
    }

    private object TimeDelayIdlingResource : IdlingResource {
        @Volatile
        private var callback: ResourceCallback? = null
        private val isIdleNow = AtomicBoolean(true)

        override fun getName(): String {
            return this.javaClass.name
        }

        override fun isIdleNow(): Boolean {
            return isIdleNow.get()
        }

        override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
            this.callback = callback
        }

        fun setDelay(minutes: Int) {
            setPoliciesDelay(minutes)
            isIdleNow.set(false)
            val timerTask = timerTask {
                isIdleNow.set(true)
                callback?.onTransitionToIdle()
                setPoliciesDelayDefault()
            }
            val delayMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())
            Timer().schedule(timerTask, delayMillis)
        }

        private fun setPoliciesDelay(minutes: Int) {
            IdlingPolicies.setMasterPolicyTimeout((minutes + 1).toLong(), TimeUnit.MINUTES)
            IdlingPolicies.setIdlingResourceTimeout((minutes + 1).toLong(), TimeUnit.MINUTES)
        }

        private fun setPoliciesDelayDefault() {
            IdlingPolicies.setIdlingResourceTimeout(26, TimeUnit.SECONDS)
            IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS)
        }
    }
}
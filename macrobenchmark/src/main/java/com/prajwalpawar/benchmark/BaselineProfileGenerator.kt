package com.prajwalpawar.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.prajwalpawar.fiscus",
        // Check if the app is ready for benchmarking
        includeInStartupProfile = true
    ) {
        // This block defines the actions that represent "hot" code paths
        // We start by launching the application
        pressHome()
        startActivityAndWait()

        // Interact with the dashboard
        // Wait for some content to be visible (e.g. Recent Transactions)
        device.waitForIdle()
        
        // We can add interactions like scrolling or clicking tabs
        // to help the compiler optimize navigation as well.
        device.findObject(By.text("Analysis"))?.click()
        device.waitForIdle()
        
        device.findObject(By.text("History"))?.click()
        device.waitForIdle()
        
        device.findObject(By.text("Settings"))?.click()
        device.waitForIdle()
    }
}

package com.illustravibes.arus

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArusE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppShellRendersAndNavigates() {
        // Wait for Home Screen elements with ignoreCase = true
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithText("TOTAL SALDO", ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Verify Home screen elements render cleanly
        composeTestRule.onNodeWithText("TOTAL SALDO", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Aksi Cepat", ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Daftar Dompet", ignoreCase = true).assertIsDisplayed()

        // 2. Navigate to Laporan tab
        composeTestRule.onNodeWithText("Laporan").performClick()
        composeTestRule.onNodeWithText("Laporan Keuangan", ignoreCase = true).assertIsDisplayed()

        // 3. Navigate to Pengaturan tab
        composeTestRule.onNodeWithText("Pengaturan").performClick()
        composeTestRule.onNodeWithText("Kelola Data", ignoreCase = true).assertIsDisplayed()

        // 4. Navigate back to Beranda tab
        composeTestRule.onNodeWithText("Beranda").performClick()
        composeTestRule.onNodeWithText("TOTAL SALDO", ignoreCase = true).assertIsDisplayed()
    }
}

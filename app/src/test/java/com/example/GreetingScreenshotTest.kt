package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.DailyRoutineState
import com.example.data.GitaVerse
import com.example.data.MotivationalQuote
import com.example.ui.RoutineTabContent
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockQuote = MotivationalQuote(
        1, 
        "The secret of getting ahead is getting started.", 
        "Mark Twain"
    )
    val mockVerse = GitaVerse(
        1, 
        2, 
        47, 
        "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥", 
        "karmaṇy-evādhikāras te mā phaleṣu kadācana", 
        "You have a right to perform your duties but are not entitled to results.", 
        "Focus on execution, not the rewards."
    )
    val mockState = DailyRoutineState(
        date = "2026-05-26", 
        brush = true, 
        bath = false
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          RoutineTabContent(
            quote = mockQuote,
            verse = mockVerse,
            state = mockState,
            onShuffleQuote = {},
            onShuffleVerse = {},
            onToggleBrush = {},
            onToggleBath = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.*
import com.example.data.llm.native.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ==================== DYNAMIC MULTI-THEME DATA CONFIG ====================
enum class AppThemePreset(
    val themeName: String,
    val bg: Color,
    val surface: Color,
    val surfaceMedium: Color,
    val textPrim: Color,
    val textMuted: Color,
    val accentSaffron: Color,
    val accentGold: Color,
    val border: Color,
    val isLight: Boolean
) {
    SAFFRON_SLATE(
        themeName = "Saffron Slate (Premium Dark)",
        bg = Color(0xFF101010),
        surface = Color(0xFF18171B),
        surfaceMedium = Color(0xFF242327),
        textPrim = Color(0xFFE5DFE5),
        textMuted = Color(0xFFA59EA7),
        accentSaffron = Color(0xFFFF5722),
        accentGold = Color(0xFFFFD54F),
        border = Color(0xFF332F38),
        isLight = false
    ),
    MINIMAL_MONK(
        themeName = "Minimal Monk (Zen Cream)",
        bg = Color(0xFFFCF9F2),
        surface = Color(0xFFF3ECE0),
        surfaceMedium = Color(0xFFE9DEC8),
        textPrim = Color(0xFF2D2C28),
        textMuted = Color(0xFF6E6A60),
        accentSaffron = Color(0xFFCF5C36),
        accentGold = Color(0xFFAB7C11),
        border = Color(0xFFDDD2BC),
        isLight = true
    ),
    TELANGANA_WARMTH(
        themeName = "Telangana Warmth (Clay Gold)",
        bg = Color(0xFF1E1310),
        surface = Color(0xFF2B1C17),
        surfaceMedium = Color(0xFF3B2923),
        textPrim = Color(0xFFF7EFE5),
        textMuted = Color(0xFFCBB6AF),
        accentSaffron = Color(0xFFE0533C),
        accentGold = Color(0xFFF9A825),
        border = Color(0xFF4C342C),
        isLight = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDashboardScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Live collecting all persistent Room state variables
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val checkListState by viewModel.routineState.collectAsStateWithLifecycle()
    val activeHabits by viewModel.activeHabits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.habitLogs.collectAsStateWithLifecycle()
    val mealLogs by viewModel.mealLogs.collectAsStateWithLifecycle()
    val voiceNotes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val contactReminders by viewModel.contactReminders.collectAsStateWithLifecycle()
    val dailyContent by viewModel.dailyContent.collectAsStateWithLifecycle()

    // Manage a local UI setting for theme choice, default to Saffron Slate
    var currentThemeState by remember { mutableStateOf(AppThemePreset.SAFFRON_SLATE) }
    val currentTheme = currentThemeState

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PRATIDINAM AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = currentTheme.textPrim
                        )
                        Text(
                            text = viewModel.getFormattedDisplayDate(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = currentTheme.accentSaffron,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Spiritual Heart",
                        tint = currentTheme.accentSaffron,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(24.dp)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val items = AppThemePreset.values()
                        val nextIdx = (items.indexOf(currentTheme) + 1) % items.size
                        currentThemeState = items[nextIdx]
                        Toast.makeText(context, "Theme: ${items[nextIdx].themeName}", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Change Theme",
                            tint = currentTheme.textMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = currentTheme.bg,
                    titleContentColor = currentTheme.textPrim
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = currentTheme.bg,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .border(BorderStroke(0.5.dp, currentTheme.border))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(80.dp)
            ) {
                // Navigation items that map to 5 full design pages
                val tabLabels = listOf("Dashboard", "Habits", "Logs & Media", "WhatsApp", "Coach")
                val tabIcons = listOf(
                    Icons.Default.Home,
                    Icons.Default.CheckCircle,
                    Icons.Default.Star, // Guaranteed present replacement for Camera log
                    Icons.Default.Share,
                    Icons.Default.Face
                )
                
                for (idx in 0..4) {
                    val isSelected = selectedTab == idx
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = idx },
                        icon = {
                            Icon(
                                imageVector = tabIcons[idx],
                                contentDescription = tabLabels[idx],
                                tint = if (isSelected) currentTheme.accentSaffron else currentTheme.textMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tabLabels[idx],
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) currentTheme.accentSaffron else currentTheme.textMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = currentTheme.surface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.bg)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "MainTabsNavigation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> DashboardTab(viewModel, currentTheme, userProfile, checkListState, dailyContent)
                    1 -> HabitsTab(viewModel, currentTheme, activeHabits, habitLogs)
                    2 -> LogsAndMediaTab(viewModel, currentTheme, mealLogs, voiceNotes)
                    3 -> WhatsAppTab(viewModel, currentTheme, contactReminders)
                    4 -> CoachSettingsTab(viewModel, currentTheme, userProfile)
                }
            }
        }
    }
}

// ==================== TAB 0: TODAY DASHBOARD ====================
@Composable
fun DashboardTab(
    viewModel: RoutineViewModel,
    theme: AppThemePreset,
    profile: UserProfile,
    trackerState: DailyRoutineState,
    content: DailyContent
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    
    // Quick log goal entries variables
    var topGoalsInput by remember { mutableStateOf("") }
    var currentMood by remember { mutableStateOf("Calm") }
    var currentEnergyLevel by remember { mutableIntStateOf(7) }

    // State expansion keys for daily spark list items
    var quoteExpanded by remember { mutableStateOf(false) }
    var gitaExpanded by remember { mutableStateOf(false) }
    var tipExpanded by remember { mutableStateOf(false) }
    var financeExpanded by remember { mutableStateOf(false) }
    var learnExpanded by remember { mutableStateOf(false) }

    // Live AI planners states
    val isPlanGenerating by viewModel.isPlanGenerating.collectAsStateWithLifecycle()
    val aiPlanOutput by viewModel.aiPlanOutput.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GREETING HEADER
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Namaskar, ${profile.name}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = theme.textPrim
            )
            Text(
                text = "Today is your day of positive discipline & spiritual silence.",
                fontSize = 13.sp,
                color = theme.textMuted
            )
        }

        // VITALS SUMMARY CARDS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Water count tracker Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                border = BorderStroke(1.dp, theme.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("WATER logged", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${trackerState.waterIntakeMl} / ${profile.waterGoalMl} ml",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.accentSaffron
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.addWaterIntake(250) },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron)
                        ) {
                            Text("+250ml", fontSize = 10.sp, color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.addWaterIntake(500) },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron)
                        ) {
                            Text("+500ml", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            // Morning basic hygiene completion streak card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                border = BorderStroke(1.dp, theme.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DAILY hygiene", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Brush", fontSize = 12.sp, color = theme.textPrim)
                        Icon(
                            imageVector = if (trackerState.brush) Icons.Default.CheckCircle else Icons.Default.Info, // Safe present icon replacement
                            contentDescription = null,
                            tint = if (trackerState.brush) theme.accentGold else theme.textMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.toggleBrush(!trackerState.brush) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bath", fontSize = 12.sp, color = theme.textPrim)
                        Icon(
                            imageVector = if (trackerState.bath) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (trackerState.bath) theme.accentGold else theme.textMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.toggleBath(!trackerState.bath) }
                        )
                    }
                }
            }
        }

        // --- THE DAILY SPARK EXPANDABLES COLLECTION ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "TODAY'S SEEDED SPARKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = theme.accentSaffron,
                letterSpacing = 1.sp
            )

            // Spark 1: QUOTE CARD (Spark)
            ExpandableInteractiveCard(
                title = "DAILY SPARK QUOTE",
                subtitle = content.quote.substringBefore(" — "),
                caption = content.quote.substringAfter(" — ", "Philosophy"),
                expanded = quoteExpanded,
                onHeaderClick = { quoteExpanded = !quoteExpanded },
                headerIcon = Icons.Default.Star,
                theme = theme
            ) {
                Text(
                    text = "This quote reminds us to ground our actions into systematic habits rather than relying on inconsistent moods. Establish stable momentum early in the morning.",
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    lineHeight = 16.sp
                )
            }

            // Spark 2: SANSKRIT BHAGAVAD GITA CARD
            ExpandableInteractiveCard(
                title = "BHAGAVAD GITA SLOKA",
                subtitle = "Focus on duty, leave results to the divine.",
                caption = "Scripture Lesson",
                expanded = gitaExpanded,
                onHeaderClick = { gitaExpanded = !gitaExpanded },
                headerIcon = Icons.Default.Info,
                theme = theme
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.bg, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = content.gitaSloka,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentSaffron,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Telugu Transliteration / Meaning:\n${content.gitaMeaningTelugu}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textPrim,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content.gitaMeaningEnglish,
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    lineHeight = 16.sp
                )
            }

            // Spark 3: TELUGU CHITKALA CARD
            ExpandableInteractiveCard(
                title = "TELUGU CHITKALA (TRADITIONAL TIP)",
                subtitle = "Telugu lifestyle wisdom for health & peace.",
                caption = "Heritage tip",
                expanded = tipExpanded,
                onHeaderClick = { tipExpanded = !tipExpanded },
                headerIcon = Icons.Default.Favorite,
                theme = theme
            ) {
                Text(
                    text = content.teluguChitka,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textPrim,
                    lineHeight = 20.sp
                )
            }

            // Spark 4: FINANCIAL WISDOM CARD
            ExpandableInteractiveCard(
                title = "DAILY FINANCIAL WISDOM",
                subtitle = "Topic: ${content.financialTopic}",
                caption = "Wealth Academy",
                expanded = financeExpanded,
                onHeaderClick = { financeExpanded = !financeExpanded },
                headerIcon = Icons.Default.Star,
                theme = theme
            ) {
                Text(
                    text = content.financialExplanation,
                    fontSize = 13.sp,
                    color = theme.textPrim,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceMedium, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        "Disclaimer: This lesson is purely educational and does not constitute personalized financial investment advice.",
                        fontSize = 11.sp,
                        color = theme.textMuted,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Spark 5: LEARN SOMETHING NEW
            ExpandableInteractiveCard(
                title = "LEARN TODAY",
                subtitle = "Topic: ${content.learningTopic}",
                caption = "General Knowledge",
                expanded = learnExpanded,
                onHeaderClick = { learnExpanded = !learnExpanded },
                headerIcon = Icons.Default.Info,
                theme = theme
            ) {
                Text(
                    text = content.learningExplanation,
                    fontSize = 13.sp,
                    color = theme.textPrim,
                    lineHeight = 18.sp
                )
            }
        }

        // DAILY MOTIVATED LOGS CORNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            border = BorderStroke(1.dp, theme.border)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "LOG MOOD & GOALS FOR COACH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.accentSaffron,
                    letterSpacing = 0.5.sp
                )

                // Goals input text area
                OutlinedTextField(
                    value = topGoalsInput,
                    onValueChange = { topGoalsInput = it },
                    label = { Text("What are your Top 3 Goals today?", color = theme.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrim,
                        unfocusedTextColor = theme.textPrim,
                        focusedBorderColor = theme.accentSaffron,
                        unfocusedBorderColor = theme.border
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                // Mood selector choices
                Column {
                    Text("Select Mood: $currentMood", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val moods = listOf("Calm", "Motivated", "Tired", "Distracted", "Peaceful")
                        for (m in moods) {
                            val active = currentMood == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) theme.accentSaffron else theme.surfaceMedium)
                                    .clickable { currentMood = m }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(m, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else theme.textPrim)
                            }
                        }
                    }
                }

                // Energy slider log
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Energy Level:", fontSize = 12.sp, color = theme.textPrim)
                        Text("$currentEnergyLevel / 10", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = theme.accentGold)
                    }
                    Slider(
                        value = currentEnergyLevel.toFloat(),
                        onValueChange = { currentEnergyLevel = it.toInt() },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentGold,
                            activeTrackColor = theme.accentSaffron,
                            inactiveTrackColor = theme.border
                        )
                    )
                }

                // Call local AI companion planner
                Button(
                    onClick = {
                        val goalsText = if (topGoalsInput.isEmpty()) "Discipline, peace, healthy diet" else topGoalsInput
                        viewModel.generateDailyOllamaPlan(
                            goals = goalsText,
                            mood = currentMood,
                            energy = currentEnergyLevel
                        )
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isPlanGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Generate AI Personal Day Plan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Show AI Generated Plan
                if (aiPlanOutput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.bg, RoundedCornerShape(16.dp))
                            .border(1.dp, theme.accentGold, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = aiPlanOutput,
                            fontSize = 12.sp,
                            color = theme.textPrim,
                            lineHeight = 16.sp
                        )
                    }
                    Toast.makeText(context, "Day Plan saved securely in local Journal!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Expandable list items component
@Composable
fun ExpandableInteractiveCard(
    title: String,
    subtitle: String,
    caption: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    headerIcon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: AppThemePreset,
    contents: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = BorderStroke(1.dp, theme.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                .clickable { onHeaderClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = "",
                            tint = theme.accentSaffron,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.accentGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = theme.textMuted
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(theme.border)
                )
                Spacer(modifier = Modifier.height(12.dp))
                contents()
            }
        }
    }
}

// ==================== TAB 1: GUIDED MORNING ROUTINE & HABITS ====================
@Composable
fun HabitsTab(
    viewModel: RoutineViewModel,
    theme: AppThemePreset,
    habits: List<Habit>,
    habitLogs: List<HabitLog>
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var displaySection by remember { mutableIntStateOf(0) } // 0: Morning Steps, 1: Deep Breathe Counter, 2: Habits Tracker

    // Breathing counter local states
    var breathTimerActive by remember { mutableStateOf(false) }
    var breathStage by remember { mutableStateOf("Inhale") } // Inhale, Hold, Exhale
    var breathCyclesRemaining by remember { mutableIntStateOf(5) }
    var breathTickCountdown by remember { mutableIntStateOf(4) }
    var scaleFactor by remember { mutableStateOf(1.0f) }

    // Forest sound simulations
    var selectedSoundTrack by remember { mutableStateOf("Silent Monk") }

    // Form variable for habits creation
    var customHabitName by remember { mutableStateOf("") }
    var customHabitCategory by remember { mutableStateOf("Discipline") }

    val context = LocalContext.current

    // Breathing state simulation coroutine
    LaunchedEffect(breathTimerActive, breathTickCountdown) {
        if (breathTimerActive && breathCyclesRemaining > 0) {
            delay(1000)
            if (breathTickCountdown > 1) {
                breathTickCountdown--
                if (breathStage == "Inhale") {
                    scaleFactor = 1.5f - (breathTickCountdown.toFloat() / 4f) * 0.5f
                } else if (breathStage == "Exhale") {
                    scaleFactor = 1.0f + (breathTickCountdown.toFloat() / 6f) * 0.5f
                }
            } else {
                // Advance cycle stage
                when (breathStage) {
                    "Inhale" -> {
                        breathStage = "Hold"
                        breathTickCountdown = 4
                        scaleFactor = 1.5f
                    }
                    "Hold" -> {
                        breathStage = "Exhale"
                        breathTickCountdown = 6
                    }
                    "Exhale" -> {
                        breathStage = "Inhale"
                        breathTickCountdown = 4
                        breathCyclesRemaining--
                        scaleFactor = 1.0f
                    }
                }
            }
        } else if (breathCyclesRemaining == 0) {
            breathTimerActive = false
            breathStage = "Relaxed"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TAB HEADER TABS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val sections = listOf("Morning Flow", "Mona Breath", "Habits Tracking")
            for (i in sections.indices) {
                val active = displaySection == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) theme.accentSaffron else theme.surface)
                        .border(1.dp, if (active) theme.accentSaffron else theme.border, RoundedCornerShape(12.dp))
                        .clickable { displaySection = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        sections[i],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else theme.textPrim
                    )
                }
            }
        }

        when (displaySection) {
            0 -> {
                // WAKE UP GENTLE EARLY MORNING STEPS FLOW
                Text(
                    "EARLY MORNING 10-STEP SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                val defaultSteps = listOf(
                    Pair("Wake up early", "Shake off laziness to align with solar frequencies (Brahma Muhurtham)."),
                    Pair("Drink 500 ml water", "Flushes night waste, restores hydration block levels directly."),
                    Pair("Freshen up hygienic care", "Cleansing sensory nodes for fresh focus."),
                    Pair("Cold shower flow", "Extreme shock therapy to wake the cardiovascular system."),
                    Pair("Deep Box breathing", "Calm active thoughts using 4-4-6 counting breath blocks."),
                    Pair("Silent meditation", "Rest inside spiritual quietude. Do not check your phone."),
                    Pair("Re-examine daily goals", "Write top priority outcomes on Today's dashboard."),
                    Pair("Write daily journal", "Engage conscious self-reflection early in the day."),
                    Pair("Praise yesterday achievements", "Congratulate yourself for yesterday's completed tasks."),
                    Pair("Nourishing healthy breakfast", "Eat protein and complex grains (Ragi Java with nuts).")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    defaultSteps.forEachIndexed { idx, pair ->
                        val isBrush = idx == 2
                        val isBath = idx == 3
                        val isWater = idx == 1
                        
                        // Check state variables dynamically
                        val checked = when {
                            isBrush -> viewModel.routineState.collectAsStateWithLifecycle().value.brush
                            isBath -> viewModel.routineState.collectAsStateWithLifecycle().value.bath
                            isWater -> viewModel.routineState.collectAsStateWithLifecycle().value.waterIntakeMl >= 500
                            else -> false // Dummy indicator fallback
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surface),
                            border = BorderStroke(1.dp, theme.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Step ${idx + 1}/10: ${pair.first}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (checked) theme.accentGold else theme.textPrim
                                    )
                                    Text(
                                        text = pair.second,
                                        fontSize = 11.sp,
                                        color = theme.textMuted,
                                        lineHeight = 15.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (checked) theme.accentGold else theme.surfaceMedium)
                                        .border(2.dp, theme.accentGold, CircleShape)
                                        .clickable {
                                            when {
                                                isBrush -> viewModel.toggleBrush(!checked)
                                                isBath -> viewModel.toggleBath(!checked)
                                                isWater -> viewModel.addWaterIntake(if (checked) -500 else 500)
                                                else -> Toast.makeText(context, "Small step completed! Building discipline.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (checked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = theme.bg,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // BOX BREATHING SECTION
                Text(
                    "BOX BREATHING & GUIDED MEDITATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "BOX BREATH 4-4-6 SECONDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.accentGold
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Pulsing breathing circle representation
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceMedium)
                                .border(
                                    BorderStroke(
                                        4.dp,
                                        Brush.sweepGradient(
                                            listOf(
                                                theme.accentSaffron,
                                                theme.accentGold,
                                                theme.accentSaffron
                                            )
                                        )
                                    ), CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing inner bubble
                            Box(
                                modifier = Modifier
                                    .size((130f * scaleFactor).dp)
                                    .clip(CircleShape)
                                    .background(theme.accentSaffron.copy(alpha = 0.25f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (breathTimerActive) breathStage.uppercase() else "TAP START",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black, color = theme.textPrim
                                )
                                if (breathTimerActive) {
                                    Text(
                                        text = "$breathTickCountdown sec",
                                        fontSize = 12.sp,
                                        color = theme.textMuted
                                    )
                                    Text(
                                        text = "Cycle Progress: $breathCyclesRemaining / 5",
                                        fontSize = 10.sp,
                                        color = theme.accentGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sound track chooser
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Meditation Audio Track:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                              ) {
                                val tracks = listOf("Silent Monk", "Forest Silence", "Ocean Waves")
                                for (t in tracks) {
                                    val act = selectedSoundTrack == t
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (act) theme.accentSaffron else theme.surfaceMedium)
                                        .clickable { selectedSoundTrack = t }
                                        .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (act) Color.White else theme.textPrim)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (breathTimerActive) {
                                    breathTimerActive = false
                                } else {
                                    breathCyclesRemaining = 5
                                    breathTickCountdown = 4
                                    breathStage = "Inhale"
                                    breathTimerActive = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (breathTimerActive) "Pause Breathe Practice" else "Start Deep Breathe Practice",
                                color = Color.White, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            2 -> {
                // HABIT TRACKING GRID WITH CUSTOM HABITS & LOGS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MANAGE HABITS STREAKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.accentSaffron,
                        letterSpacing = 1.sp
                    )
                }

                // Add custom habit field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Custom Lifestyle Habit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customHabitName,
                                onValueChange = { customHabitName = it },
                                label = { Text("Habit name, eg: Read Gita", color = theme.textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrim,
                                    unfocusedTextColor = theme.textPrim,
                                    focusedBorderColor = theme.accentSaffron,
                                    unfocusedBorderColor = theme.border
                                ),
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (customHabitName.isNotEmpty()) {
                                        viewModel.addHabit(customHabitName, customHabitCategory)
                                        customHabitName = ""
                                        Toast.makeText(context, "New habit added!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Add", color = Color.White)
                            }
                        }
                    }
                }

                // List existing habits
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    habits.forEach { habit ->
                        val isLoggedCompleted = habitLogs.any { it.habitId == habit.id && it.status == "Completed" }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surface),
                            border = BorderStroke(1.dp, theme.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeHabit(habit.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Habit",
                                            tint = theme.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = habit.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrim
                                        )
                                        Text(
                                            text = "Category: ${habit.category}",
                                            fontSize = 11.sp,
                                            color = theme.textMuted
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleHabit(habit.id, !isLoggedCompleted) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLoggedCompleted) theme.accentGold else theme.surfaceMedium
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            if (isLoggedCompleted) "Logged Done" else "Log Done",
                                            fontSize = 11.sp,
                                            color = if (isLoggedCompleted) theme.bg else theme.textPrim,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: LOGS & MEDIA (MEALS, RECORDER & WHITEBOARD PHOTOS) ====================
@Composable
fun LogsAndMediaTab(
    viewModel: RoutineViewModel,
    theme: AppThemePreset,
    mealLogs: List<MealLog>,
    voiceNotes: List<VoiceNote>
) {
    val scrollState = rememberScrollState()
    var displaySubSection by remember { mutableIntStateOf(0) } // 0: Telangana Food, 1: Meal Logger & Photo capture, 2: Voice notes

    // Local meal logging form entries
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var enteredMealDescription by remember { mutableStateOf("") }
    var inputCalories by remember { mutableStateOf("") }
    var inputProtein by remember { mutableStateOf("") }

    // Simulated voice recordings variables
    var simTextJournalDictation by remember { mutableStateOf("") }
    val recordTimerActive = remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TAB HEADER TABS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val sections = listOf("Telangana Diet", "Log Food", "Voice Journal")
            for (i in sections.indices) {
                val active = displaySubSection == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) theme.accentSaffron else theme.surface)
                        .border(1.dp, if (active) theme.accentSaffron else theme.border, RoundedCornerShape(12.dp))
                        .clickable { displaySubSection = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        sections[i],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else theme.textPrim
                    )
                }
            }
        }

        when (displaySubSection) {
            0 -> {
                // SUGGEST ROTATING TELANGANA PLANNED COMBOS
                val rotationMeals = DailyRoutineData.getTelanganaFoodRotationsForDay(
                    pref = "Non-Vegetarian",
                    dateKey = viewModel.getTodayDateKey()
                )

                Text(
                    "DAILY ROTATING LIFESTYLE FOOD DIET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "TELANGANA SPECIFIC HEALTHY COMBOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.accentGold
                        )

                        rotationMeals.forEach { (meal, suggestion) ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = meal.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accentSaffron
                                )
                                Text(
                                    text = suggestion,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = theme.textPrim
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(theme.border)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Hydration Advisory: Drink another 500ml water following meals. Limit heavy spicy masala after 7 PM.",
                            fontSize = 11.sp,
                            color = theme.textMuted,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            1 -> {
                // LOG FOOD WITH MOCK CAPTURED MEAL PHOTO
                Text(
                    "LOG MEALS & NOTE PHOTOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add Eaten Food Diary", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                            for (t in types) {
                                val selected = selectedMealType == t
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) theme.accentSaffron else theme.bg)
                                        .clickable { selectedMealType = t }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = t,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else theme.textPrim
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = enteredMealDescription,
                            onValueChange = { enteredMealDescription = it },
                            label = { Text("What did you eat?", color = theme.textMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrim,
                                unfocusedTextColor = theme.textPrim,
                                focusedBorderColor = theme.accentSaffron,
                                unfocusedBorderColor = theme.border
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputCalories,
                                onValueChange = { inputCalories = it },
                                label = { Text("Cals (kcal)", color = theme.textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrim,
                                    unfocusedTextColor = theme.textPrim,
                                    focusedBorderColor = theme.accentSaffron,
                                    unfocusedBorderColor = theme.border
                                ),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = inputProtein,
                                onValueChange = { inputProtein = it },
                                label = { Text("Protein (g)", color = theme.textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrim,
                                    unfocusedTextColor = theme.textPrim,
                                    focusedBorderColor = theme.accentSaffron,
                                    unfocusedBorderColor = theme.border
                                ),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Meal photo section
                        var currentImageUriState by remember { mutableStateOf<String?>(null) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceMedium)
                                .clickable {
                                    currentImageUriState = "meal_captured_proof_at_${System.currentTimeMillis()}"
                                    Toast.makeText(context, "Meal photo captured successfully!", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentImageUriState == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Use guaranteed present Add icon for simulator trigger
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = theme.textMuted)
                                    Text("Simulate Camera Capture", fontSize = 11.sp, color = theme.textMuted)
                                }
                            } else {
                                Text(
                                    "📸 Proof: Verified capture saved securely in Local Database",
                                    fontSize = 12.sp,
                                    color = theme.accentGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (enteredMealDescription.isNotEmpty()) {
                                    val cal = inputCalories.toIntOrNull() ?: 350
                                    val prot = inputProtein.toIntOrNull() ?: 15
                                    viewModel.logMeal(
                                        mealType = selectedMealType,
                                        description = enteredMealDescription,
                                        imageUri = currentImageUriState,
                                        calories = cal,
                                        protein = prot
                                    )
                                    enteredMealDescription = ""
                                    inputCalories = ""
                                    inputProtein = ""
                                    currentImageUriState = null
                                    Toast.makeText(context, "Meal logged to DB!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log Meal", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Show today eaten food histories
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TODAY'S DIARY ITEMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    if (mealLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No logged meals logged today. Use logger to add delicious foods.", fontSize = 12.sp, color = theme.textMuted)
                        }
                    } else {
                        mealLogs.forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surface),
                                border = BorderStroke(1.dp, theme.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(theme.accentSaffron.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(log.mealType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.accentSaffron)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(log.description, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                                        Text("${log.estimatedCalories ?: 300} kcal | ${log.estimatedProtein ?: 12}g protein", fontSize = 11.sp, color = theme.textMuted)
                                    }
                                    IconButton(onClick = { viewModel.removeMealLog(log.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = theme.accentSaffron)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // VOICE JOURNAL & DICTATED NOTES
                Text(
                    "VOICE REFLECTION RECORDER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Simulated Speech-to-Text Recorder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrim
                        )

                        // Clear Microphone trigger with active visual pulse
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (recordTimerActive.value) theme.accentSaffron else theme.surfaceMedium)
                                .border(2.dp, theme.accentGold, CircleShape)
                                .clickable {
                                    recordTimerActive.value = !recordTimerActive.value
                                    if (!recordTimerActive.value && simTextJournalDictation.isEmpty()) {
                                        simTextJournalDictation = "I woke up early today. Had a warm ginger tea, sat quietly meditating. Feeling highly energetic and ready to focus on constructing my coding modules."
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recordTimerActive.value) Icons.Default.Close else Icons.Default.PlayArrow, // Safe present icon replacements
                                contentDescription = "Mic Trigger",
                                tint = if (recordTimerActive.value) Color.White else theme.accentSaffron,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = if (recordTimerActive.value) "🔴 RECORDING ACTIVE: SPEAK NOW..." else "Tap icon to start simulated audio logs",
                            fontSize = 11.sp,
                            color = if (recordTimerActive.value) theme.accentSaffron else theme.textMuted,
                            fontWeight = FontWeight.Bold
                        )

                        // Editable captured text
                        OutlinedTextField(
                            value = simTextJournalDictation,
                            onValueChange = { simTextJournalDictation = it },
                            label = { Text("Dictation Transcript Preview", color = theme.textMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrim,
                                unfocusedTextColor = theme.textPrim,
                                focusedBorderColor = theme.accentSaffron,
                                unfocusedBorderColor = theme.border
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                if (simTextJournalDictation.isNotEmpty()) {
                                    viewModel.saveVoiceNote(
                                        transcript = simTextJournalDictation,
                                        summary = "A warm morning log. Highlights: morning meditation completed, energetic mood.",
                                        category = "Morning"
                                    )
                                    simTextJournalDictation = ""
                                    Toast.makeText(context, "Voice note summarized & added!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = simTextJournalDictation.isNotEmpty()
                        ) {
                            Text("Summarize with AI & Save Note", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Show historical voice notes list with play simulator
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SAVED VOICE JOURNAL NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    if (voiceNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No saved recordings log yet.", fontSize = 12.sp, color = theme.textMuted)
                        }
                    } else {
                        voiceNotes.forEach { note ->
                            var activeNotePlayerSim by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = theme.surface),
                                border = BorderStroke(1.dp, theme.border)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(onClick = { activeNotePlayerSim = !activeNotePlayerSim }) {
                                                Icon(
                                                    imageVector = if (activeNotePlayerSim) Icons.Default.Close else Icons.Default.PlayArrow, // Safe present icon replacements
                                                    contentDescription = null,
                                                    tint = theme.accentGold,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                            Column {
                                                Text("Voice log: ${note.date}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                                                Text("Category: ${note.category ?: "Notes"}", fontSize = 11.sp, color = theme.textMuted)
                                            }
                                        }
                                        IconButton(onClick = { viewModel.removeVoiceNote(note.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = theme.accentSaffron)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "*STT Transcript Preview:*\n\"${note.transcript ?: ""}\"",
                                        fontSize = 12.sp,
                                        color = theme.textMuted,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(theme.surfaceMedium, RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "💡 **AI Extraction:** ${note.aiSummary ?: "No summary extracted"}",
                                            fontSize = 11.sp,
                                            color = theme.textPrim
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 3: CONTACT REMINDERS & WHATSAPP GENERATION ====================
@Composable
fun WhatsAppTab(
    viewModel: RoutineViewModel,
    theme: AppThemePreset,
    contacts: List<ContactReminder>
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // WhatsApp generation local forms
    var selectedContact by remember { mutableStateOf<ContactReminder?>(null) }
    var selectedContextCategory by remember { mutableStateOf("Morning Greeting") }

    var inputContactName by remember { mutableStateOf("") }
    var inputContactNumber by remember { mutableStateOf("") }
    var inputCategorySelected by remember { mutableStateOf("Family") }

    val whatsAppDraft by viewModel.whatsAppDraft.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: CONTACT REMINDERS LIST
        Text(
            "COMMUNICATION RELATIONSHIP COMPANION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = theme.accentSaffron,
            letterSpacing = 1.sp
        )

        // Add custom contact area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            border = BorderStroke(1.dp, theme.border)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add New Relationship Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                OutlinedTextField(
                    value = inputContactName,
                    onValueChange = { inputContactName = it },
                    label = { Text("Contact Name (eg: Amma)", color = theme.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrim,
                        unfocusedTextColor = theme.textPrim,
                        focusedBorderColor = theme.accentSaffron,
                        unfocusedBorderColor = theme.border
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = inputContactNumber,
                    onValueChange = { inputContactNumber = it },
                    label = { Text("WhatsApp Phone Number (with + country code)", color = theme.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrim,
                        unfocusedTextColor = theme.textPrim,
                        focusedBorderColor = theme.accentSaffron,
                        unfocusedBorderColor = theme.border
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Button(
                    onClick = {
                        if (inputContactName.isNotEmpty() && inputContactNumber.isNotEmpty()) {
                            viewModel.addNewContact(
                                name = inputContactName,
                                phone = inputContactNumber,
                                category = inputCategorySelected,
                                frequency = "Weekly",
                                notes = "Stay connected."
                            )
                            inputContactName = ""
                            inputContactNumber = ""
                            Toast.makeText(context, "Contact created in DB!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Contact Record", color = Color.White)
                }
            }
        }

        // List contacts
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CHOOSE CONTACT TO DRAFT MESSAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
            contacts.forEach { contact ->
                val activeSelection = selectedContact?.id == contact.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (activeSelection) theme.accentGold else theme.border
                            ), RoundedCornerShape(18.dp)
                        )
                        .clickable { selectedContact = contact },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeSelection) theme.surfaceMedium else theme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(contact.contactName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                            Text(contact.phoneNumber, fontSize = 11.sp, color = theme.textMuted)
                            Text("Class: ${contact.category} | Reminder: ${contact.reminderFrequency}", fontSize = 11.sp, color = theme.accentSaffron)
                        }
                        IconButton(onClick = { viewModel.removeContact(contact.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = theme.accentSaffron)
                        }
                    }
                }
            }
        }

        // WhatsApp message generator
        if (selectedContact != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                border = BorderStroke(1.dp, theme.border)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "DRAFT WARM AI GREETING FOR ${selectedContact?.contactName?.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.accentGold
                    )

                    // Draft context chooser
                    Column {
                        Text("Draft Context purpose:", fontSize = 12.sp, color = theme.textPrim)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val contextsLabels = listOf("Morning Greeting", "Weekly Check-in", "Express Gratitude")
                            for (c in contextsLabels) {
                                val act = selectedContextCategory == c
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (act) theme.accentSaffron else theme.surfaceMedium)
                                        .clickable { selectedContextCategory = c }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(c, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (act) Color.White else theme.textPrim)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            selectedContact?.let { contact ->
                                viewModel.generateWhatsAppDraft(contact, selectedContextCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Draft Message with local AI", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (whatsAppDraft.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(theme.bg, RoundedCornerShape(12.dp))
                                .border(1.dp, theme.accentGold, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(whatsAppDraft, fontSize = 13.sp, color = theme.textPrim, lineHeight = 18.sp)
                        }

                        // Share confirmation button
                        Button(
                            onClick = {
                                val targetNumber = selectedContact?.phoneNumber ?: ""
                                try {
                                    // Construct WhatsApp share intent safely requiring user trigger confirmation on their device
                                    val sendUri = Uri.parse("https://api.whatsapp.com/send?phone=$targetNumber&text=" + Uri.encode(whatsAppDraft))
                                    val sendIntent = Intent(Intent.ACTION_VIEW, sendUri)
                                    context.startActivity(sendIntent)
                                    Toast.makeText(context, "Opening WhatsApp confirmation...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    // Fallback plain share sheet
                                    val textShareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, whatsAppDraft)
                                    }
                                    context.startActivity(Intent.createChooser(textShareIntent, "Share Draft Message"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = theme.bg)
                                Text("Send manually via WhatsApp", color = theme.bg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 4: COMPANION COACH & USER SETTINGS ====================
@Composable
fun CoachSettingsTab(
    viewModel: RoutineViewModel,
    theme: AppThemePreset,
    profile: UserProfile
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var displaySubTab by remember { mutableIntStateOf(0) } // 0: AI Coach chat, 1: Profile settings

    // Coach choices states
    var activeCoachSpecialty by remember { mutableStateOf("Discipline Coach") }
    var chatMessageEntered by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()

    // Configuration states matching Room profile fields
    var usernameField by remember { mutableStateOf(profile.name) }
    var wakeTimeField by remember { mutableStateOf(profile.wakeUpTime) }
    var sleepTimeField by remember { mutableStateOf(profile.sleepTime) }
    var waterGoalField by remember { mutableStateOf(profile.waterGoalMl.toString()) }
    var ollamaUrlField by remember { mutableStateOf(profile.ollamaBaseUrl) }
    var ollamaModelField by remember { mutableStateOf(profile.ollamaModel) }

    val context = LocalContext.current

    // GGUF Native Inference states
    val isModelLoaded by viewModel.isModelLoaded.collectAsStateWithLifecycle()
    val lastModelLoadStatus by viewModel.lastModelLoadStatus.collectAsStateWithLifecycle()
    val lastGenerationLatency by viewModel.lastGenerationLatency.collectAsStateWithLifecycle()

    var importedModels by remember { mutableStateOf(ModelManager.listImportedModels(context)) }
    var importInProgress by remember { mutableStateOf(false) }

    var ggufCtxField by remember { mutableStateOf(profile.ggufContextSize.toString()) }
    var ggufTokensField by remember { mutableStateOf(profile.ggufMaxTokens.toString()) }
    var ggufTempField by remember { mutableStateOf(profile.ggufTemperature.toString()) }
    var ggufThreadsField by remember { mutableStateOf(profile.ggufThreads.toString()) }
    var ggufBatchField by remember { mutableStateOf(profile.ggufBatchSize.toString()) }

    var selectedGgufFile by remember { mutableStateOf<java.io.File?>(
        importedModels.find { it.absolutePath == profile.selectedGgufPath } ?: importedModels.firstOrNull()
    ) }

    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                importInProgress = true
                scope.launch {
                    val importedFile = withContext(Dispatchers.IO) {
                        ModelManager.importGgufModel(context, uri)
                    }
                    importInProgress = false
                    if (importedFile != null) {
                        importedModels = ModelManager.listImportedModels(context)
                        selectedGgufFile = importedFile
                        Toast.makeText(context, "GGUF model imported!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Import failed. Verify valid storage permissions.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // INTERNAL TABS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val sections = listOf("Speech Companion", "Settings")
            for (i in sections.indices) {
                val active = displaySubTab == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) theme.accentSaffron else theme.surface)
                        .border(1.dp, if (active) theme.accentSaffron else theme.border, RoundedCornerShape(12.dp))
                        .clickable { displaySubTab = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        sections[i],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else theme.textPrim
                    )
                }
            }
        }

        when (displaySubTab) {
            0 -> {
                // CHAT INTERFACE WITH DIRECT GEMMA/OLLAMA ENDPOINTS
                Text(
                    "CONVERSE WITH LOCAL AI COMPANION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                // Specialty Coach buttons chooser
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Specialty Mode:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val specialists = listOf("Discipline", "Food", "Spiritual", "Telugu tips")
                        for (s in specialists) {
                            val mappingSpec = s + " Coach"
                            val active = activeCoachSpecialty == mappingSpec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) theme.accentSaffron else theme.surfaceMedium)
                                .clickable { activeCoachSpecialty = mappingSpec }
                                .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else theme.textPrim
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (chatHistory.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = theme.accentSaffron)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Start chatting with your local ${activeCoachSpecialty}!\nAsk for wellness guidelines or Gita verses.",
                                            fontSize = 11.sp,
                                            color = theme.textMuted,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(chatHistory) { message ->
                                val fromUser = message.first == "user"
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (fromUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 240.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (fromUser) 16.dp else 4.dp,
                                                    bottomEnd = if (fromUser) 4.dp else 16.dp
                                                )
                                            )
                                            .background(if (fromUser) theme.surfaceMedium else theme.bg)
                                            .border(0.5.dp, theme.border, RoundedCornerShape(16.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = message.second,
                                            fontSize = 12.sp,
                                            color = theme.textPrim,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatMessageEntered,
                        onValueChange = { chatMessageEntered = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask something to Coach...", color = theme.textMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrim,
                            unfocusedTextColor = theme.textPrim,
                            focusedBorderColor = theme.accentSaffron,
                            unfocusedBorderColor = theme.border
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (chatMessageEntered.isNotEmpty()) {
                                viewModel.submitChatMessage(chatMessageEntered, activeCoachSpecialty)
                                chatMessageEntered = ""
                            }
                        })
                    )
                    IconButton(
                        onClick = {
                            if (chatMessageEntered.isNotEmpty()) {
                                viewModel.submitChatMessage(chatMessageEntered, activeCoachSpecialty)
                                chatMessageEntered = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(theme.accentSaffron)
                    ) {
                        if (isChatGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }

                Button(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceMedium),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear Chat History", color = theme.textPrim, fontSize = 11.sp)
                }
            }

            1 -> {
                // USER PROFILE SETTINGS CONFIGURATION
                Text(
                    "LIFESTYLE PROFILE CONTEXTS & OLLAMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentSaffron,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    border = BorderStroke(1.dp, theme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Edit Companion Contexts Data",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrim
                        )

                        // Name input
                        OutlinedTextField(
                            value = usernameField,
                            onValueChange = { usernameField = it },
                            label = { Text("Your name", color = theme.textMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrim,
                                unfocusedTextColor = theme.textPrim,
                                focusedBorderColor = theme.accentSaffron,
                                unfocusedBorderColor = theme.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Symmetrical timing config fields
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = wakeTimeField,
                                onValueChange = { wakeTimeField = it },
                                label = { Text("Wake-up Time", color = theme.textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrim,
                                    unfocusedTextColor = theme.textPrim,
                                    focusedBorderColor = theme.accentSaffron,
                                    unfocusedBorderColor = theme.border
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sleepTimeField,
                                onValueChange = { sleepTimeField = it },
                                label = { Text("Sleep Wind-down", color = theme.textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrim,
                                    unfocusedTextColor = theme.textPrim,
                                    focusedBorderColor = theme.accentSaffron,
                                    unfocusedBorderColor = theme.border
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Water goal field
                        OutlinedTextField(
                            value = waterGoalField,
                            onValueChange = { waterGoalField = it },
                            label = { Text("Water daily goal (ml)", color = theme.textMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrim,
                                unfocusedTextColor = theme.textPrim,
                                focusedBorderColor = theme.accentSaffron,
                                unfocusedBorderColor = theme.border
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(theme.border)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // GGUF Native Llama Local Settings Card
                        Text("NATIVE LOCAL GGUF MODEL MANAGEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.accentGold)

                        // 1. Current Status Panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, theme.border, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isModelLoaded) Color(0xFF4CAF50) else Color(0xFFFF5722))
                                    )
                                    Text(
                                        text = if (isModelLoaded) "ACTIVE ENGINE: GGUF ONLINE" else "STATUS: OFFLINE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isModelLoaded) Color(0xFF4CAF50) else theme.textMuted
                                    )
                                }
                                Text(
                                    text = "Details: $lastModelLoadStatus",
                                    fontSize = 11.sp,
                                    color = theme.textMuted
                                )
                                if (lastGenerationLatency > 0L) {
                                    Text(
                                        text = "Last Native Inference Latency: $lastGenerationLatency ms",
                                        fontSize = 11.sp,
                                        color = theme.accentGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 2. Models list selector
                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("AVAILABLE ON-DEVICE MODEL FILES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)

                                if (importedModels.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No GGUF models imported yet. Use button below.", fontSize = 12.sp, color = theme.textMuted)
                                    }
                                } else {
                                    importedModels.forEach { file ->
                                        val isSelected = selectedGgufFile?.absolutePath == file.absolutePath
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) theme.bg else Color.Transparent)
                                                .clickable { selectedGgufFile = file }
                                                .border(1.dp, if (isSelected) theme.accentSaffron else Color.Transparent, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(file.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrim)
                                                Text("Size: ${ModelManager.formatFileSize(file.length())}", fontSize = 10.sp, color = theme.textMuted)
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedGgufFile = file },
                                                colors = RadioButtonDefaults.colors(selectedColor = theme.accentSaffron)
                                            )
                                        }
                                    }
                                }

                                if (importInProgress) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = theme.accentSaffron, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Copying GGUF to secure models directory...", fontSize = 11.sp, color = theme.textMuted)
                                    }
                                }

                                Button(
                                    onClick = { safPickerLauncher.launch(arrayOf("*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.bg),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Import", tint = theme.accentSaffron, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import GGUF model from Storage", color = theme.textPrim, fontSize = 11.sp)
                                }
                            }
                        }

                        // 3. Parameters configuration Form
                        Text("GGUF LOADER & DECODE PARAMETERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ggufCtxField,
                                onValueChange = { ggufCtxField = it },
                                label = { Text("Context Size", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textPrim, unfocusedTextColor = theme.textPrim, focusedBorderColor = theme.accentSaffron, unfocusedBorderColor = theme.border),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = ggufTokensField,
                                onValueChange = { ggufTokensField = it },
                                label = { Text("Max Tokens", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textPrim, unfocusedTextColor = theme.textPrim, focusedBorderColor = theme.accentSaffron, unfocusedBorderColor = theme.border),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ggufTempField,
                                onValueChange = { ggufTempField = it },
                                label = { Text("Temperature", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textPrim, unfocusedTextColor = theme.textPrim, focusedBorderColor = theme.accentSaffron, unfocusedBorderColor = theme.border),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = ggufThreadsField,
                                onValueChange = { ggufThreadsField = it },
                                label = { Text("CPU Threads", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.textPrim, unfocusedTextColor = theme.textPrim, focusedBorderColor = theme.accentSaffron, unfocusedBorderColor = theme.border),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // 4. Memory warning Advisory
                        selectedGgufFile?.let { selected ->
                            if (selected.length() > 2L * 1024 * 1024 * 1024) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x26FF9800)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFA726), RoundedCornerShape(10.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("⚠️", fontSize = 16.sp)
                                        Column {
                                            Text("Advisory: Large GGUF Size", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFFB74D))
                                            Text("This file is large (${ModelManager.formatFileSize(selected.length())}). Loading it might exceed system RAM limits and crash. Closing back-apps is advised.", fontSize = 10.sp, color = theme.textPrim)
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Load / Unload Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isModelLoaded) {
                                Button(
                                    onClick = { viewModel.unloadLocalLlamaModel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFFFF5722), RoundedCornerShape(12.dp))
                                ) {
                                    Text("Unload GGUF RAM", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        selectedGgufFile?.let { file ->
                                            val cSize = ggufCtxField.toIntOrNull() ?: 512
                                            val mTokens = ggufTokensField.toIntOrNull() ?: 120
                                            val tempVal = ggufTempField.toFloatOrNull() ?: 0.3f
                                            val th = ggufThreadsField.toIntOrNull() ?: 4
                                            val bSize = ggufBatchField.toIntOrNull() ?: 128
                                            viewModel.loadLocalLlamaModel(
                                                modelPath = file.absolutePath,
                                                contextSize = cSize,
                                                maxTokens = mTokens,
                                                temperature = tempVal,
                                                threads = th,
                                                batchSize = bSize
                                            )
                                        } ?: Toast.makeText(context, "Select or import GGUF file first!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentSaffron),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Load Local GGUF", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 6. Save Overall Profile
                        Button(
                            onClick = {
                                val wGoal = waterGoalField.toIntOrNull() ?: 2500
                                viewModel.changeUserProfile(
                                    name = usernameField,
                                    wake = wakeTimeField,
                                    sleep = sleepTimeField,
                                    food = "Non-Vegetarian",
                                    water = wGoal
                                )
                                Toast.makeText(context, "Lifestyle configurations saved locally!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

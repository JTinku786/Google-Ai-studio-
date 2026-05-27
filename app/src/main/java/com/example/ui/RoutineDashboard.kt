package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyRoutineState
import com.example.data.GitaVerse
import com.example.data.MotivationalQuote
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// Re-defining Immersive Palette locally to ensure total consistency with the design file
val ImmersiveBg = Color(0xFF121212)
val ImmersiveTextPrim = Color(0xFFE6E1E5)
val ImmersiveTextMuted = Color(0xFFCAC4D0)
val ImmersivePurple = Color(0xFFD0BCFF)
val ImmersiveSurface = Color(0xFF1C1B1F)
val ImmersiveSurfaceMedium = Color(0xFF2B2930)
val ImmersiveActiveBg = Color(0xFF352F3E)
val ImmersiveBorder = Color(0xFF49454F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDashboardScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val todayDateFormatted = remember { viewModel.getFormattedDisplayDate() }
    val lastStateUpdated by viewModel.routineState.collectAsStateWithLifecycle()
    val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()
    val currentVerse by viewModel.currentVerse.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MORNING FLOW",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = todayDateFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmersiveTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // Small user avatar matching Immersive UI Design SVG placeholder
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ImmersiveActiveBg)
                            .border(1.dp, ImmersiveBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = ImmersivePurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ImmersiveBg,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ImmersiveBg,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(BorderStroke(0.5.dp, ImmersiveBorder))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(80.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    modifier = Modifier.testTag("bottom_nav_flow_tab"),
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Daily Flow",
                            tint = if (selectedTab == 0) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    label = {
                        Text(
                            "Flow",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ImmersiveActiveBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    modifier = Modifier.testTag("bottom_nav_wealth_tab"),
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Prep & Deals",
                            tint = if (selectedTab == 1) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    label = {
                        Text(
                            "Prep & Wealth",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ImmersiveActiveBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    modifier = Modifier.testTag("bottom_nav_mind_tab"),
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Mind & Feed",
                            tint = if (selectedTab == 2) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    label = {
                        Text(
                            "Mind & Info",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ImmersiveActiveBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    modifier = Modifier.testTag("bottom_nav_vitals_tab"),
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Vitals & Inbox",
                            tint = if (selectedTab == 3) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    label = {
                        Text(
                            "Vitals & Gmail",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) ImmersivePurple else ImmersiveTextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ImmersiveActiveBg
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ImmersiveBg)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "TabSelectionAnim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> RoutineTabContent(
                        quote = currentQuote,
                        verse = currentVerse,
                        state = lastStateUpdated,
                        onShuffleQuote = { viewModel.shuffleQuote() },
                        onShuffleVerse = { viewModel.shuffleVerse() },
                        onToggleBrush = { viewModel.toggleBrush(it) },
                        onToggleBath = { viewModel.toggleBath(it) }
                    )
                    1 -> WealthTab(
                        state = lastStateUpdated,
                        onToggleTime = { viewModel.toggleTradingTime(it) },
                        onToggleLiquidity = { viewModel.toggleTradingLiquidity(it) },
                        onToggleDisplacement = { viewModel.toggleTradingDisplacement(it) }
                    )
                    2 -> GrowthIntellectTab(
                        state = lastStateUpdated,
                        onSolveTeaser = { viewModel.solvePuzzle(true) },
                        onResetTeaser = { viewModel.solvePuzzle(false) }
                    )
                    3 -> GmailVitalsTab()
                }
            }
        }
    }
}

// ==================== TAB 0: FLOW (MORNING ESSENTIALS & TELUGU CHITKALU) ====================
@Composable
fun RoutineTabContent(
    quote: MotivationalQuote,
    verse: GitaVerse,
    state: DailyRoutineState,
    onShuffleQuote: () -> Unit,
    onShuffleVerse: () -> Unit,
    onToggleBrush: (Boolean) -> Unit,
    onToggleBath: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 1: MOTIVATION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("motivation_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0x1AD0BCFF), Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.75f
                        )
                        drawRect(brush = brush)
                    }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Spark",
                            color = ImmersivePurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(1.dp)
                                .background(ImmersiveBorder)
                        )
                    }
                    IconButton(
                        onClick = onShuffleQuote,
                        modifier = Modifier
                            .testTag("shuffle_quote_button")
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Shuffle Quote",
                            tint = ImmersivePurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\"${quote.text}\"",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— ${quote.author}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Feature 2: BHAGAVAD GITA CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gita_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0x06FFFFFF), Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = size.width * 0.6f
                        )
                        drawRect(brush = brush)
                    }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BHAGAVAD GITA • CH ${verse.chapter}, VERSE ${verse.verse}",
                        color = ImmersivePurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onShuffleVerse,
                        modifier = Modifier
                            .testTag("shuffle_verse_button")
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Shuffle Teachings",
                            tint = ImmersivePurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ImmersiveBg, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = verse.sanskrit,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePurple,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = verse.transliteration,
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    color = ImmersiveTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = verse.translation,
                    fontSize = 13.sp,
                    color = ImmersiveTextPrim,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ImmersiveBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "REFLECTIONS:",
                    fontSize = 10.sp,
                    color = ImmersivePurple,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = verse.explanation,
                    fontSize = 12.sp,
                    color = ImmersiveTextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Feature 3: MORNING HYGIENE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hygiene_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Morning Hygiene",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Brush Checklist Item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (state.brush) ImmersiveActiveBg else ImmersiveSurface)
                            .clickable { onToggleBrush(!state.brush) }
                            .border(1.dp, if (state.brush) ImmersivePurple else ImmersiveBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (state.brush) ImmersivePurple else ImmersiveTextMuted.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.brush) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ImmersivePurple)
                                    )
                                }
                            }
                            Text(
                                text = "Brush",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.brush) ImmersivePurple else Color.White
                            )
                        }
                    }

                    // Bath Checklist Item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (state.bath) ImmersiveActiveBg else ImmersiveSurface)
                            .clickable { onToggleBath(!state.bath) }
                            .border(1.dp, if (state.bath) ImmersivePurple else ImmersiveBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (state.bath) ImmersivePurple else ImmersiveTextMuted.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.bath) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ImmersivePurple)
                                    )
                                }
                            }
                            Text(
                                text = "Bath",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.bath) ImmersivePurple else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Feature 4: TELUGU CHITKALU WELLNESS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wellness_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0x15FFB74D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "",
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "TELUGU CHITKALU (WELLNESS)",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ChitkaluRow(
                    telugu = "ఉదయాన్నే గోరువెచ్చని నీటిలో కొద్దిగా నిమ్మరసం, తేనె కలుపుకుని తాగితే జీర్ణశక్తి మెరుగవుతుంది మరియు శరీరంలో అనవసర కొవ్వు కరుగుతుంది.",
                    english = "Drinking warm water mixed with lemon juice and organic honey in the morning enhances metabolic rate, improves digestion, and naturally flushes toxins."
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ImmersiveBorder)

                ChitkaluRow(
                    telugu = "కొబ్బరి నూనెలో ఎండిన ఉసిరికాయ ముక్కలను వేసి మరిగించి, ఆ నూనెను తలకు రాసుకోవడం వల్ల జుట్టు నల్లగా, బలంగా ఎదుగుతుంది.",
                    english = "Boiling dried amla slices in virgin coconut oil and gently massaging it onto your scalp acts as a deep structural tonic, strengthening roots & avoiding grays."
                )
            }
        }
    }
}

@Composable
fun ChitkaluRow(telugu: String, english: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = telugu,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE4DCCF),
            lineHeight = 19.sp
        )
        Text(
            text = english,
            fontSize = 11.sp,
            color = ImmersiveTextMuted,
            lineHeight = 15.sp,
            fontStyle = FontStyle.Italic
        )
    }
}


// ==================== TAB 1: WEALTH & PREP (TRADING, OFFER CAROUSEL, FINANCIAL STORY) ====================
// Custom offers data class
data class CuratedOffer(
    val platformName: String,
    val discount: String,
    val promoCode: String,
    val cardColorHex: Long
)

val mockOffers = listOf(
    CuratedOffer("MYNTRA", "FLAT 40% OFF ACTIVEWEAR", "ACTIVE40", 0xFFE91E63),
    CuratedOffer("AMAZON", "UP TO 33% OFF KEYBOARDS", "NO CODE REQ", 0xFFFF9900),
    CuratedOffer("FLIPKART", "SUMMER SMARTWEAR 50% OFF", "FLP50", 0xFF2874F0),
    CuratedOffer("TATACLIQ", "20% OFF LUXURY BRANDS", "CLIQLUXE", 0xFF008080)
)

@Composable
fun WealthTab(
    state: DailyRoutineState,
    onToggleTime: (Boolean) -> Unit,
    onToggleLiquidity: (Boolean) -> Unit,
    onToggleDisplacement: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var displayFinancialDetails by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 5: TRADING BREAD & BUTTER STRATEGY CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trading_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveBg),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ImmersiveSurface)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRADING: B&B STRATEGY",
                        color = ImmersivePurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .background(Color(0x23D0BCFF), RoundedCornerShape(100.dp))
                            .border(1.dp, Color(0x73D0BCFF), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "LIVE TIMEFRAME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ImmersivePurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The 3 mechanical steps connected visually
                StepperStepItem(
                    num = "01",
                    title = "TIME (EST KILLZONE)",
                    desc = "Verify session time: Only operate inside designated volume windows (NY Open 7AM-10AM EST or London Open 2AM-5AM EST). Unaligned times invalidates setups.",
                    checked = state.tradingTime,
                    testTag = "step_time_toggle",
                    onCheckedChange = onToggleTime
                )

                StepperStepItem(
                    num = "02",
                    title = "LIQUIDITY SWEEP",
                    desc = "Wait for price to breach swing highs (Buy-side BSL) or swing lows (Sell-side SSL) on HTF. We must hunt stops to absorb institutional fuel.",
                    checked = state.tradingLiquidity,
                    testTag = "step_liquidity_toggle",
                    onCheckedChange = onToggleLiquidity
                )

                StepperStepItem(
                    num = "03",
                    title = "DISPLACEMENT SHIFT",
                    desc = "Detect high-velocity market structure shifts leaving visible Fair Value Gaps. Safe limit pending entries are placed purely on the displacement zone.",
                    checked = state.tradingDisplacement,
                    testTag = "step_displacement_toggle",
                    onCheckedChange = onToggleDisplacement
                )
            }
        }

        // Feature 6: CURATED OFFERS CAROUSEL
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CURATED DAILY DEALS & OFFERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersiveTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offers_carousel"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mockOffers) { offer ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(120.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
                        border = BorderStroke(1.dp, ImmersiveBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val b = Brush.linearGradient(
                                        colors = listOf(Color(offer.cardColorHex).copy(alpha = 0.2f), Color.Transparent),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    )
                                    drawRect(brush = b)
                                }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = offer.platformName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(offer.cardColorHex)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(offer.cardColorHex).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "DEAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(offer.cardColorHex)
                                    )
                                }
                            }

                            Text(
                                text = offer.discount,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PROMO CODE", fontSize = 9.sp, color = ImmersiveTextMuted)
                                Text(
                                    text = offer.promoCode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feature 7: FINANCIAL WISDOM DEEP-DIVE EXPANDABLE STORY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wisdom_expandable_card")
                .clickable { displayFinancialDetails = !displayFinancialDetails },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0x2034D399)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DAILY INVESTMENT STORY",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Compounding high-conviction assets",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = if (displayFinancialDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Story",
                        tint = ImmersiveTextMuted
                    )
                }

                if (displayFinancialDetails) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ImmersiveBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "THE MECHANICS OF ASYMMETRICAL RISKS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "True wealth accumulation is mathematically simple but psychologically demanding. Success starts with mastering asymmetric risk profile ratios, where downside constraints are locked while upside returns capitalize on geometric scaling expansion.",
                        fontSize = 12.sp,
                        color = ImmersiveTextPrim,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "Key Strategy Formulas:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ImmersiveBg, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "• Downside Threshold: Set stop limits at precisely 1.5% max capital exposure.",
                            fontSize = 11.sp,
                            color = ImmersiveTextMuted
                        )
                        Text(
                            text = "• Position Sizing = (Capital × Risk%) / ATR Margin.",
                            fontSize = 11.sp,
                            color = ImmersiveTextMuted
                        )
                        Text(
                            text = "• Compound Multiplication Rule: Retain 80% dividends for manual compounding reinvestments.",
                            fontSize = 11.sp,
                            color = ImmersiveTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Actionable Advice: Do not invest based on short-term high-hype cycles. Consistently scale positions into cash-generative systems with positive expectancy models. Rebalance monthly with mathematical precision.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted,
                        lineHeight = 16.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tapping reveals structural compound strategies and mechanical risk formulas to protect and expand capital.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StepperStepItem(
    num: String,
    title: String,
    desc: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = num,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (checked) ImmersivePurple else ImmersiveTextMuted.copy(alpha = 0.3f),
            modifier = Modifier.width(30.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) ImmersivePurple else Color.White
                )
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = ImmersivePurple,
                        uncheckedColor = ImmersiveBorder,
                        checkmarkColor = ImmersiveBg
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = if (checked) ImmersiveTextPrim else ImmersiveTextMuted,
                lineHeight = 16.sp
            )
        }
    }
}


// ==================== TAB 2: GROWTH & INTELLECT (BRAIN TEASERS, ENTREPRENEUR SHORTS, AI FEED) ====================
@Composable
fun GrowthIntellectTab(
    state: DailyRoutineState,
    onSolveTeaser: () -> Unit,
    onResetTeaser: () -> Unit
) {
    val scrollState = rememberScrollState()
    var userAnsText by remember { mutableStateOf("") }
    var ansError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 8: BRAIN TEASERS & IQ (VANISHING SYSTEM)
        AnimatedVisibility(
            visible = !state.puzzleSolved,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("iq_adventure_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
                border = BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY IQ CHALLENGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ImmersivePurple,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE91E63).copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                .border(0.5.dp, Color(0xFFE91E63), RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "DISAPPEARS ONCE SOLVED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "I have keys but no physical locks. I have plenty of space, but there is no room. You can enter, yet nobody can go outside.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text input field
                    OutlinedTextField(
                        value = userAnsText,
                        onValueChange = {
                            userAnsText = it
                            if (ansError) ansError = false
                        },
                        label = { Text("Enter Your Solution", color = ImmersiveTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("puzzle_input_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImmersivePurple,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (ansError) {
                        Text(
                            text = "Incorrect solution. Hint: Modern developers type their entire code on me.",
                            fontSize = 11.sp,
                            color = Color(0xFFEF5350),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (userAnsText.trim().lowercase() == "keyboard") {
                                onSolveTeaser()
                                userAnsText = ""
                            } else {
                                ansError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_puzzle_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersivePurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Submit Check",
                            color = ImmersiveBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Feature 8-B: SNEAKY RESET FOR TESTING & REVIEW (Only shown if puzzle is solved)
        if (state.puzzleSolved) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "IQ Puzzle Complete!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Well done. Disappeared from interface.",
                                fontSize = 10.sp,
                                color = ImmersiveTextMuted
                            )
                        }
                    }
                    TextButton(
                        onClick = onResetTeaser,
                        modifier = Modifier.testTag("reset_puzzle_button")
                    ) {
                        Text("Reset Puzzle", color = ImmersivePurple, fontSize = 11.sp)
                    }
                }
            }
        }

        // Feature 9: ENTREPRENEUR SHORTS (Mindset at 30)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("entrepreneur_shorts_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FOUNDER'S SHORTS •mindset at 30•",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ImmersivePurple,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The 70/30 Profit Margin Guardrail",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "At 30, bootstrap philosophy is simple: profit isn't vanity; it is dry powder. I structure raw margins around a rigid 70/30 baseline. 70% of gross inflow directly feeds raw customer feature engineering and security, while 30% is frozen instantly as structural contingency reserve.",
                    fontSize = 12.sp,
                    color = ImmersiveTextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Actionable Takeaways:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersivePurple
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. Cashflow > Growth: Validate product-market match with payment swipes, not feedback forms.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted
                    )
                    Text(
                        text = "2. Contingency buffer: Build at least 12 months of operational survival pools before expanding staff.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted
                    )
                }
            }
        }

        // Feature 10: AI ADVANCEMENTS NEWS FEED
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_feed_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "",
                        tint = ImmersivePurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AI ADVANCEMENTS DISPATCH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = ImmersivePurple,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // AI Article 1
                Column {
                    Text(
                        text = "GraphRAG vs Standard Vector Indexes",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Multi-hop queries now mapping relationships natively. Graph structure handles dense, structured code repositories, eliminating the classic out-of-context retrieval failures.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ImmersiveBorder)

                // AI Article 2
                Column {
                    Text(
                        text = "Quantized SLMs Run Local on Edge Chips",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1.5B param models compressed into 4-bit INT representations achieving sub-100ms next-token response loops directly on edge silicon, preserving complete user data privacy.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ImmersiveBorder)

                // AI Article 3
                Column {
                    Text(
                        text = "Agentic Workflows & Multi-Agent Planning",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Transitioning from standard single-step inference to autonomous loops. Agents perform self-debugging and execute shell tools iteratively until verify tests pass.",
                        fontSize = 11.sp,
                        color = ImmersiveTextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}


// ==================== TAB 3: VISUAL VITALS & GMAIL (GOOGLE FIT & GMAIL SIMULATOR) ====================
@Composable
fun GmailVitalsTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 11: GOOGLE FIT INSIGHTS (PROGRESS RING & HEART waveform & SLEEP SLIDER)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_fit_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceMedium),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GOOGLE FIT DIRECT TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ImmersivePurple,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Text(
                            text = "SYNCED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Visual components (Step progress ring & Stats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Canvas
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw background circle track
                            drawCircle(
                                color = ImmersiveBorder,
                                radius = size.minDimension / 2 - 8,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Draw active step tracking sweep
                            drawArc(
                                color = ImmersivePurple,
                                startAngle = -90f,
                                sweepAngle = 270f, // represents 75% progress
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "8,420",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Steps",
                                fontSize = 9.sp,
                                color = ImmersiveTextMuted
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FitTelemetryRow(
                            label = "Daily Step Target",
                            stat = "8,420 / 10,000",
                            suffix = "(84%)",
                            tint = ImmersivePurple
                        )
                        FitTelemetryRow(
                            label = "Estimated Caloric Burn",
                            stat = "412 kcal",
                            suffix = "net active",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = ImmersiveBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Vital 2: Live heart rate Bezier waveform canvas
                Text(
                    text = "HEART TELEMETRY STATUS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pulse animated simulation icon
                        val infiniteTransition = rememberInfiniteTransition(label = "HeartPulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.85f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "heartScale"
                        )

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier
                                .size((20 * scale).dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = "72 BPM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "RESTING: 64 BPM",
                        fontSize = 10.sp,
                        color = ImmersiveTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Bezier dynamic telemetry path drawn directly on Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(ImmersiveBg, RoundedCornerShape(10.dp))
                        .border(1.dp, ImmersiveBorder, RoundedCornerShape(10.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.5f)
                            lineTo(size.width * 0.15f, size.height * 0.5f)
                            // heartbeat pulse spike
                            lineTo(size.width * 0.20f, size.height * 0.2f)
                            lineTo(size.width * 0.25f, size.height * 0.85f)
                            lineTo(size.width * 0.30f, size.height * 0.5f)
                            lineTo(size.width * 0.55f, size.height * 0.5f)
                            // second spike
                            lineTo(size.width * 0.60f, size.height * 0.15f)
                            lineTo(size.width * 0.65f, size.height * 0.80f)
                            lineTo(size.width * 0.70f, size.height * 0.5f)
                            lineTo(size.width, size.height * 0.5f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFEF5350),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ImmersiveBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Vital 3: Sleep Telemetry Slider block (Segmented block)
                Text(
                    text = "SLEEP METRICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7h 45m sleep session",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "EF: 94% (RESTFUL)",
                        fontSize = 10.sp,
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom segmented tracking bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Deep sleep segment
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxHeight()
                            .background(Color(0xFF352F3E))
                    )
                    // Light sleep segment
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .background(ImmersivePurple)
                    )
                    // REM segment
                    Box(
                        modifier = Modifier
                            .weight(0.2f)
                            .fillMaxHeight()
                            .background(Color(0xFF34D399))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Deep (2h 10m)", fontSize = 9.sp, color = ImmersiveTextMuted)
                    Text("Light (4h 15m)", fontSize = 9.sp, color = ImmersiveTextMuted)
                    Text("REM (1h 20m)", fontSize = 9.sp, color = ImmersiveTextMuted)
                }
            }
        }

        // Feature 12: EMAIL SUMMARIZER CARD (SIMULATED INBOX)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gmail_box_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with simulated badge count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x20EF5350)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MailOutline,
                                contentDescription = "",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "GMAIL INTELLIGENT WRITER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF5350),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF5350), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "3 UNREAD SUMMARIZED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email Row 1
                InboxSimulatedRow(
                    sender = "Google Cloud Console",
                    badge = "FIN",
                    subject = "Daily Budget Threshold Trigger",
                    bulletSummary = "Warning: Monthly compute engine billing target exceeded 88%. Current spend pace tracks to $14.20 over expected run values."
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ImmersiveBorder)

                // Email Row 2
                InboxSimulatedRow(
                    sender = "Robinhood Orders Team",
                    badge = "STK",
                    subject = "Limit Order Filled: SPY Standard ETF",
                    bulletSummary = "Executed 10 shares of SPY standard index trackers at $521.10. Total direct settlement clears dynamically in 1 trading day."
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ImmersiveBorder)

                // Email Row 3
                InboxSimulatedRow(
                    sender = "Product Hunt Daily Dispatch",
                    badge = "AI",
                    subject = "Top Frameworks for Local Agent Automation",
                    bulletSummary = "Our daily newsletter reviews state-of-the-art quantized Agent execution layers. Multi-hop memory planning algorithms trend highest."
                )
            }
        }
    }
}

@Composable
fun InboxSimulatedRow(
    sender: String,
    badge: String,
    subject: String,
    bulletSummary: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Platform Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ImmersiveSurfaceMedium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = ImmersivePurple
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "AI Review",
                    fontSize = 9.sp,
                    color = ImmersiveTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = subject,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ImmersivePurple,
                modifier = Modifier.padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bulletSummary,
                fontSize = 11.sp,
                color = ImmersiveTextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun FitTelemetryRow(label: String, stat: String, suffix: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = ImmersiveTextMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stat,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = suffix,
                fontSize = 10.sp,
                color = ImmersiveTextMuted
            )
        }
    }
}

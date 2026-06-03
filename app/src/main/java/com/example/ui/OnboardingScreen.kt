package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    viewModel: RoutineViewModel,
    currentTheme: AppThemePreset,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Onboarding variables
    var name by remember { mutableStateOf("") }
    var selectedIdentity by remember { mutableStateOf("Disciplined Founder") }
    var preferredLanguage by remember { mutableStateOf("Telugu") }

    var selectedWake by remember { mutableStateOf("05:30 AM") }
    var selectedSleep by remember { mutableStateOf("10:00 PM") }
    var waterGoalMl by remember { mutableIntStateOf(2500) }

    var sevenDayGoal by remember { mutableStateOf("Build strict sleep-wake rhythm and drink 3L water") }
    var selectedWeakness by remember { mutableStateOf("Late phone scrolling") }
    var preferredTone by remember { mutableStateOf("Warrior Directive (Strict)") }

    var isPlanCompiling by remember { mutableStateOf(false) }

    val identities = listOf(
        "Disciplined Founder", "UPSC / Exam Aspirant", "Sanskrit Student",
        "Corporate Tech Worker", "Young Father", "Active Trader", "Fitness Comeback"
    )

    val weaknesses = listOf(
        "Late phone scrolling", "Laziness / Procrastination", "Junk food snacking",
        "Inconsistent morning hours", "Impulsive daily trading"
    )

    val tones = listOf(
        "Warrior Directive (Strict)", "Gita Serenity (Spiritual)",
        "Elder Brother (Supportive)", "No-Nonsense Founder (CTO)"
    )

    val languages = listOf("Telugu", "English-Telugu (Teenglish)", "English")

    val wakeTimes = listOf(
        "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM", "06:30 AM",
        "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM", "09:00 AM"
    )
    val sleepTimes = listOf(
        "09:00 PM", "09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM",
        "11:30 PM", "12:00 AM", "12:30 AM", "01:00 AM", "01:30 AM",
        "02:00 AM"
    )
    val waterGoals = listOf(2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon / Header Accent
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(currentTheme.surfaceMedium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Guru Icon",
                    tint = currentTheme.accentSaffron,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PRATIDINAM AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = currentTheme.textPrim
            )

            Text(
                text = "Initiate your private daily discipline loop",
                fontSize = 13.sp,
                color = currentTheme.textMuted,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val active = i == step
                    val passed = i < step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (active) currentTheme.accentSaffron
                                else if (passed) currentTheme.accentGold
                                else currentTheme.border
                            )
                            .padding(horizontal = 4.dp)
                    )
                    if (i < 3) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Step $step of 3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.accentSaffron
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isPlanCompiling) {
                // Loading spinner step
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(0.5.dp, currentTheme.border)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = currentTheme.accentSaffron,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "COMPILING YOUR 7-DAY BATTLE PLAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = currentTheme.textPrim,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Synthesizing custom morning commands, weekly milestones, food recommendations, and routine adjustments securely on your device...",
                            fontSize = 12.sp,
                            color = currentTheme.textMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                when (step) {
                    1 -> {
                        // Phase 1 - Name, Path & Language
                        OnboardingCard(currentTheme = currentTheme) {
                            Text(
                                text = "SPIRITUAL IDENTIFICATION",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.accentSaffron,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "What is your Name?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Enter your name", color = currentTheme.textMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentTheme.accentSaffron,
                                    unfocusedBorderColor = currentTheme.border,
                                    focusedTextColor = currentTheme.textPrim,
                                    unfocusedTextColor = currentTheme.textPrim
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Choose your Current Life Path",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            identities.forEach { identity ->
                                val selected = selectedIdentity == identity
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) currentTheme.surfaceMedium else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                if (selected) 1.dp else 0.5.dp,
                                                if (selected) currentTheme.accentSaffron else currentTheme.border
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedIdentity = identity }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { selectedIdentity = identity },
                                        colors = RadioButtonDefaults.colors(selectedColor = currentTheme.accentSaffron)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = identity,
                                        fontSize = 13.sp,
                                        color = currentTheme.textPrim,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Preferred Coach Language",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                languages.forEach { lang ->
                                    val sel = preferredLanguage == lang
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) currentTheme.surfaceMedium else Color.Transparent)
                                            .border(
                                                BorderStroke(
                                                    if (sel) 1.dp else 0.5.dp,
                                                    if (sel) currentTheme.accentSaffron else currentTheme.border
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { preferredLanguage = lang }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lang.split(" ").first(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) currentTheme.accentSaffron else currentTheme.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { if (name.trim().isNotEmpty()) step = 2 },
                            enabled = name.trim().isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_next_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.accentSaffron,
                                disabledContainerColor = currentTheme.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Next: Discipline Parameters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (name.trim().isNotEmpty()) Color.White else currentTheme.textMuted
                            )
                        }
                    }

                    2 -> {
                        // Phase 2 - Times & Hydration
                        OnboardingCard(currentTheme = currentTheme) {
                            Text(
                                text = "ROUTINE TIME & HYDRATION MANDATES",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.accentSaffron,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Target Wake-up Hour",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                wakeTimes.forEach { rawTime ->
                                    val sel = selectedWake == rawTime
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) currentTheme.surfaceMedium else Color.Transparent)
                                            .border(
                                                BorderStroke(
                                                    if (sel) 1.dp else 0.5.dp,
                                                    if (sel) currentTheme.accentSaffron else currentTheme.border
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedWake = rawTime }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rawTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) currentTheme.accentSaffron else currentTheme.textPrim
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Target Sleep Wind-down Hour",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                sleepTimes.forEach { rawTime ->
                                    val sel = selectedSleep == rawTime
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) currentTheme.surfaceMedium else Color.Transparent)
                                            .border(
                                                BorderStroke(
                                                    if (sel) 1.dp else 0.5.dp,
                                                    if (sel) currentTheme.accentSaffron else currentTheme.border
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedSleep = rawTime }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rawTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) currentTheme.accentSaffron else currentTheme.textPrim
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Daily Water Hydration Goal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                waterGoals.forEach { qty ->
                                    val sel = waterGoalMl == qty
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) currentTheme.surfaceMedium else Color.Transparent)
                                            .border(
                                                BorderStroke(
                                                    if (sel) 1.dp else 0.5.dp,
                                                    if (sel) currentTheme.accentSaffron else currentTheme.border
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { waterGoalMl = qty }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${qty / 1000}L (${qty}ml)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) currentTheme.accentSaffron else currentTheme.textPrim
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                border = BorderStroke(1.dp, currentTheme.border),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = currentTheme.textPrim)
                            ) {
                                Text("Back", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = { step = 3 },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentSaffron),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Next: Weaknesses", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    3 -> {
                        // Phase 3 - Milestone, weakness & coach tone
                        OnboardingCard(currentTheme = currentTheme) {
                            Text(
                                text = "BATTLE STRATEGY & DEVIATION SHIELDS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.accentSaffron,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Specify Your Critical 7-Day Goal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = sevenDayGoal,
                                onValueChange = { sevenDayGoal = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentTheme.accentSaffron,
                                    unfocusedBorderColor = currentTheme.border,
                                    focusedTextColor = currentTheme.textPrim,
                                    unfocusedTextColor = currentTheme.textPrim
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Your Biggest Ritual Bottleneck / Weakness",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            weaknesses.forEach { weakness ->
                                val selected = selectedWeakness == weakness
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) currentTheme.surfaceMedium else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                if (selected) 1.dp else 0.5.dp,
                                                if (selected) currentTheme.accentSaffron else currentTheme.border
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedWeakness = weakness }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { selectedWeakness = weakness },
                                        colors = RadioButtonDefaults.colors(selectedColor = currentTheme.accentSaffron)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = weakness,
                                        fontSize = 12.sp,
                                        color = currentTheme.textPrim,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Select Coach Action Tone",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.textPrim
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            tones.forEach { tone ->
                                val selected = preferredTone == tone
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) currentTheme.surfaceMedium else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                if (selected) 1.dp else 0.5.dp,
                                                if (selected) currentTheme.accentSaffron else currentTheme.border
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { preferredTone = tone }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { preferredTone = tone },
                                        colors = RadioButtonDefaults.colors(selectedColor = currentTheme.accentSaffron)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tone,
                                        fontSize = 12.sp,
                                        color = currentTheme.textPrim,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                border = BorderStroke(1.dp, currentTheme.border),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = currentTheme.textPrim)
                            ) {
                                Text("Back", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    isPlanCompiling = true
                                    viewModel.submitOnboarding(
                                        name = name,
                                        lifeIdentity = selectedIdentity,
                                        wake = selectedWake,
                                        sleep = selectedSleep,
                                        water = waterGoalMl,
                                        mainGoal = sevenDayGoal,
                                        weakness = selectedWeakness,
                                        tone = preferredTone,
                                        lang = preferredLanguage
                                    )
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp)
                                    .testTag("onboarding_submit_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentSaffron),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Build 7-Day Plan ✓",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun OnboardingCard(
    currentTheme: AppThemePreset,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = currentTheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, currentTheme.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

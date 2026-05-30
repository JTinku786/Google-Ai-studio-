package com.example.data

import kotlin.random.Random

data class MotivationalQuote(
    val id: Int,
    val text: String,
    val author: String
)

data class GitaVerse(
    val id: Int,
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val transliteration: String,
    val translationEnglish: String,
    val translationTelugu: String,
    val application: String,
    val reflection: String
)

data class TeluguChitka(
    val id: Int,
    val text: String,
    val translation: String,
    val category: String
)

data class FinancialTopic(
    val title: String,
    val explanation: String,
    val priority: String,
    val rule: String,
    val action: String
)

data class LearningTopic(
    val title: String,
    val explanation: String,
    val example: String,
    val action: String
)

data class MealSuggestion(
    val breakfast: List<String>,
    val lunch: List<String>,
    val dinner: List<String>,
    val snacks: List<String>
)

object DailyRoutineData {

    val quotes = listOf(
        MotivationalQuote(1, "Discipline is freedom.", "Jocko Willink"),
        MotivationalQuote(2, "Win the morning, win the day.", "Tim Ferriss"),
        MotivationalQuote(3, "Peace is also a form of strength.", "Unknown"),
        MotivationalQuote(4, "Small habits create big destiny.", "Lao Tzu"),
        MotivationalQuote(5, "Do your duty. Leave the noise.", "Bhagavad Gita"),
        MotivationalQuote(6, "The secret of getting ahead is getting started.", "Mark Twain"),
        MotivationalQuote(7, "Energy and persistence conquer all things.", "Benjamin Franklin"),
        MotivationalQuote(8, "Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        MotivationalQuote(9, "Diligence is the mother of good luck.", "Benjamin Franklin"),
        MotivationalQuote(10, "You don't have to be great to start, but you have to start to be great.", "Zig Ziglar")
    )

    val gitaVerses = listOf(
        GitaVerse(
            id = 1, chapter = 2, verse = 47,
            sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            transliteration = "Karmanye vadhikaraste ma phaleshu kadachana...",
            translationEnglish = "You have a right to perform your prescribed duty, but you are not entitled to the fruits of activity.",
            translationTelugu = "నీవు చేయవలసిన పనిని చేయడానికే నీకు అధికారం కలదు, దాని ప్రతిఫలాన్ని ఆశించడానికి కాదు.",
            application = "Focus on your tasks as a duty today. Do not overthink the outcome.",
            reflection = "What is my absolute priority duty today that I must do with complete presence?"
        ),
        GitaVerse(
            id = 2, chapter = 2, verse = 48,
            sanskrit = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।\nसिद्ध्यसिद्ध्योः సమో భూత్వా సమత్వం యోగ ఉచ్యతే॥",
            transliteration = "Yogasthah kuru karmani sangam tyaktva dhananjaya...",
            translationEnglish = "Perform your duty with equilibrium, abandoning all attachment to success or failure.",
            translationTelugu = "విజయపరాజయాలను సమానంగా స్వీకరిస్తూ సమత్వ బుద్ధితో నీ కర్తవ్యాన్ని నిర్వహించు.",
            application = "Treat small wins and delays equally. Keep moving without emotional disruption.",
            reflection = "How can I maintain a calm, stable state of mind when things do not go as planned today?"
        ),
        GitaVerse(
            id = 3, chapter = 6, verse = 5,
            sanskrit = "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥",
            transliteration = "Uddhared atmanatmanam natmanam avasadayet...",
            translationEnglish = "Elevate yourself by your own mind; do not let yourself degrade. Your mind is your best friend, and your worst enemy.",
            translationTelugu = "నిన్ను నీవే పైకి లేపుకోవాలి. నిన్ను నీవు సడలించుకోకు. నీ మనస్సే నీకు మిత్రుడు, నీ మనస్సే నీకు శత్రువు.",
            application = "Build strong morning routines to turn your mind into a reliable ally.",
            reflection = "Is my current internal self-talk serving as a supportive friend or dragging me down?"
        )
    )

    val teluguChitkalu = listOf(
        TeluguChitka(1, "ఉదయం నిద్ర లేచిన వెంటనే ఫోన్ చూడకండి. ముందుగా ఒక గ్లాసు గోరువెచ్చని నీళ్లు తాగండి, శ్వాసను గమనించండి.", "Do not check your phone immediately after waking up. First, drink a glass of warm water and observe your breath.", "Mindset"),
        TeluguChitka(2, "రోజూ ఉదయం రగి జావ తాగడం వల్ల శరీరానికి మంచి శక్తి లభిస్తుంది మరియు ఎముకలు బలంగా తయారవుతాయి.", "Drinking Ragi Java every morning provides great energy and strengthens your skeletal structure.", "Health"),
        TeluguChitka(3, "భోజనం చేసేటప్పుడు ఫోన్ చూడటం మానేయండి. తింటున్న ఆహారాన్ని పూర్తీ శ్రద్ధతో నమిలి తినండి.", "Avoid screen time during meals. Eat consciously by chewing thoroughly, enjoying the taste.", "Food Habits"),
        TeluguChitka(4, "రాత్రి వేళల్లో మసాలాలు ఎక్కువగా ఉండే భారీ భోజనం తీసుకోకండి. తేలికపాటి ఆహారం తీసుకోవడం వల్ల నిద్ర బాగా పడుతుంది.", "Avoid heavy spiced meals at night. A lighter dinner promotes deeper, restorative sleep hygiene.", "Sleep"),
        TeluguChitka(5, "వారానికి ఒక్కసారైనా మీ బంధువులు లేదా స్నేహితులతో మనస్పూర్తిగా మాట్లాడండి. మానసిక ప్రశాంతత లభిస్తుంది.", "Talk to your family or friends with absolute presence at least once a week. It grounds social peace.", "Relationships")
    )

    val financialTopics = listOf(
        FinancialTopic(
            title = "Emergency Fund",
            explanation = "An emergency fund is money kept separately in liquid form for unexpected critical circumstances like health issues, repair, or job disruptions.",
            priority = "Extremely High",
            rule = "Safeguard exactly 6 months of absolute basic expenses.",
            action = "Calculate your exact monthly survival costs today and set up a separate savings target."
        ),
        FinancialTopic(
            title = "Compounding Magic",
            explanation = "Compounding is earning interest on your accumulated interest alongside the principal over long investment durations.",
            priority = "High",
            rule = "Start early and do not interrupt compounding unnecessarily.",
            action = "Analyze your passive mutual funds or regular savings and visualize their compounding at 12% CAGR over 15 years."
        ),
        FinancialTopic(
            title = "Asymmetric Risk Sizing",
            explanation = "Trading and business strategy where the potential loss is strictly defined and very minor, while potential upside is multi-multiplier.",
            priority = "Medium-High",
            rule = "Risk exactly 1-2% of capital per venture, aiming for at least a 1:3 hazard-to-reward ratio.",
            action = "Check your budget outflows and limit speculative trades strictly to 1.5% exposure limits."
        )
    )

    val learningTopics = listOf(
        LearningTopic(
            title = "First Principles Thinking",
            explanation = "Deconstructing complex problems down to their fundamental truths instead of relying on external comparisons or consensus.",
            example = "Instead of choosing popular diets, ask: 'What structural macros does my body require?' Hydration, pure protein, safe movement, sleep, and calorie deficits.",
            action = "Take one challenge you are facing today and decompose it into its core indisputable components."
        ),
        LearningTopic(
            title = "The Feynman Technique",
            explanation = "To truly understand complex concepts, explain them in the simplest possible terms to a child.",
            example = "If explaining block building, describe it as building a house with lego bricks, avoiding academic jargon.",
            action = "Write a 3-sentence summary of one complex topic you recently learned, using everyday words."
        ),
        LearningTopic(
            title = "80/20 Pareto Rule",
            explanation = "80% of all progressive results originate from exactly 20% of your maximum effort blocks.",
            example = "In lifestyle discipline, sleeping on time, morning hydration, and daily priority goals account for 8% of wellness yields.",
            action = "Map out your goal targets and isolate the top 2 variables that are responsible for the biggest outcomes."
        )
    )

    val defaultMealSuggestions = MealSuggestion(
        breakfast = listOf(
            "Idli + sambar + peanut chutney + boiled eggs",
            "Dosa + coconut chutney + fresh curd",
            "Pesarattu + upma + ginger chutney",
            "Ragi java + banana + soaked almonds",
            "Vegetable upma with fresh curd",
            "Millet dosa with sambar"
        ),
        lunch = listOf(
            "Millet rice + Dal (pappu) + leafy vegetable curry + curd",
            "Jowar roti + light chicken curry + fresh cucumber salad",
            "Brown rice + rasam + boiled egg fry + salad",
            "Rice + pappu charu + bendakaya vepudu (okra fry) + curd",
            "Jowar roti + palak pappu + boiled chickpeas"
        ),
        dinner = listOf(
            "Two chapatis + tomato dal + small cup of curd",
            "Millet upma with fresh curry",
            "Small portion of brown rice + lemon grass rasam + egg omelette",
            "Jowar roti + palak dal",
            "Pesarattu (moong dal pancakes) with ginger chutney (less oil)"
        ),
        snacks = listOf(
            "Fresh buttermilk with coriander and ginger",
            "Sprouts bowl with chopped onions, tomato, and lime juice",
            "Roasted chana (Bengal gram)",
            "A bowl of fresh papaya/watermelon fruits",
            "Soaked almonds and walnuts (5-6 pieces)",
            "Nutritious coconut water"
        )
    )

    fun getDailyContentForDate(dateKey: String): DailyContent {
        val seed = dateKey.hashCode().toLong()
        val random = Random(seed)

        val quote = quotes[random.nextInt(quotes.size)]
        val sloka = gitaVerses[random.nextInt(gitaVerses.size)]
        val tip = teluguChitkalu[random.nextInt(teluguChitkalu.size)]
        val fin = financialTopics[random.nextInt(financialTopics.size)]
        val learn = learningTopics[random.nextInt(learningTopics.size)]

        return DailyContent(
            date = dateKey,
            quote = "\"${quote.text}\" — ${quote.author}",
            gitaSloka = sloka.sanskrit,
            gitaMeaningEnglish = "${sloka.translationEnglish}\n\n*Application:* ${sloka.application}",
            gitaMeaningTelugu = sloka.translationTelugu,
            teluguChitka = "ఈరోజు చిట్కా:\n${tip.text}\n\n*Meaning:* ${tip.translation}",
            financialTopic = fin.title,
            financialExplanation = "${fin.explanation}\n\n*Rule Details:* ${fin.rule}\n\n*Today's Activity:* ${fin.action}",
            learningTopic = learn.title,
            learningExplanation = "${learn.explanation}\n\n*Practical Example:* ${learn.example}\n\n*How to implement:* ${learn.action}"
        )
    }

    fun getTelanganaFoodRotationsForDay(pref: String, dateKey: String): Map<String, String> {
        val seed = dateKey.hashCode().toLong()
        val random = Random(seed)

        val isVeg = pref.equals("Vegetarian", ignoreCase = true)
        
        val bf = defaultMealSuggestions.breakfast[random.nextInt(defaultMealSuggestions.breakfast.size)]
        var ln = defaultMealSuggestions.lunch[random.nextInt(defaultMealSuggestions.lunch.size)]
        var dn = defaultMealSuggestions.dinner[random.nextInt(defaultMealSuggestions.dinner.size)]
        val sn = defaultMealSuggestions.snacks[random.nextInt(defaultMealSuggestions.snacks.size)]

        if (isVeg) {
            // Replace chicken with dal or paneer
            ln = ln.replace("chicken curry", "paneer butter masala").replace("egg fry", "gobi curry")
            dn = dn.replace("egg omelette", "mixed vegetable dry sabji")
        }

        return mapOf(
            "Breakfast" to bf,
            "Lunch" to ln,
            "Dinner" to dn,
            "Snack" to sn
        )
    }
}

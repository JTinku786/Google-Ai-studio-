package com.example.data

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
    val translation: String,
    val explanation: String
)

object DailyRoutineData {
    val quotes = listOf(
        MotivationalQuote(
            1,
            "The secret of getting ahead is getting started.",
            "Mark Twain"
        ),
        MotivationalQuote(
            2,
            "Do not wait for extraordinary circumstances to do good action; try to use ordinary situations.",
            "Charles Richter"
        ),
        MotivationalQuote(
            3,
            "Your focus determines your reality. Discipline is the bridge between goals and accomplishment.",
            "Unknown"
        ),
        MotivationalQuote(
            4,
            "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            "Aristotle"
        ),
        MotivationalQuote(
            5,
            "Energy and persistence conquer all things. Small daily actions lead to monumental life results.",
            "Benjamin Franklin"
        ),
        MotivationalQuote(
            6,
            "Success is the sum of small efforts, repeated day in and day out.",
            "Robert Collier"
        ),
        MotivationalQuote(
            7,
            "In the middle of difficulty lies opportunity. Keep moving forward, step by step.",
            "Albert Einstein"
        ),
        MotivationalQuote(
            8,
            "The best way to predict the future is to create it.",
            "Peter Drucker"
        ),
        MotivationalQuote(
            9,
            "He who has a why to live can bear almost any how.",
            "Friedrich Nietzsche"
        ),
        MotivationalQuote(
            10,
            "It is not that we have a short time to live, but that we waste a lot of it.",
            "Seneca"
        ),
        MotivationalQuote(
            11,
            "Concentrate all your thoughts upon the work at hand. The sun's rays do not burn until brought to a focus.",
            "Alexander Graham Bell"
        ),
        MotivationalQuote(
            12,
            "Plan your work today and work your plan. Consistent preparation is the foundation of execution.",
            "Unknown"
        ),
        MotivationalQuote(
            13,
            "Do not let what you cannot do interfere with what you can do.",
            "John Wooden"
        ),
        MotivationalQuote(
            14,
            "Diligence is the mother of good luck.",
            "Benjamin Franklin"
        ),
        MotivationalQuote(
            15,
            "You don't have to be great to start, but you have to start to be great.",
            "Zig Ziglar"
        )
    )

    val gitaVerses = listOf(
        GitaVerse(
            1,
            2,
            47,
            "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            "karmaṇy-evādhikāras te mā phaleṣu kadācana\nmā karma-phala-hetur bhūr mā te saṅgo ’stv akarmaṇi",
            "You have a right to perform your prescribed duties, but you are not entitled to the fruits of your actions. Never consider yourself to be the cause of the results of your activities, nor be attached to inactive status.",
            "Focus completely on the process and execution of your routine, not the rewards. In trading or life, execute your setup flawlessly; the outcome is beyond your control."
        ),
        GitaVerse(
            2,
            2,
            48,
            "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।\nसिद्ध्यसिद्ध्योः समो भूत्वा समत्वं योग उच्यते॥",
            "yoga-sthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya\nsiddhy-asiddhyoḥ samo bhūtvā samatvaṁ yoga ucyate",
            "Be steadfast in yoga, O Arjuna. Perform your duty and abandon all attachment to success or failure. Such evenness of mind is called yoga.",
            "Achieve equanimity. Accept both wins and losses, checks completed and missed. Emotional stability is the ultimate edge of any developer or trader."
        ),
        GitaVerse(
            3,
            2,
            50,
            "बुद्धियुक्तो जहातीह उभे सुकृतदुष्कृते।\nतस्माद्योगाय युज्यस्व योगः कर्मसु कौशलम्॥",
            "buddhi-yukto jahātīha ubhe sukṛta-duṣkṛte\ntasmād yogāya yujyasva yogaḥ karmasu kauśalam",
            "One who is united in devotion rids himself of both good and evil actions in this life. Therefore, strive for yoga, which is the art of all work.",
            "Yoga is skill in action. Perform your daily duties with total efficiency, presence of mind, and continuous refinement."
        ),
        GitaVerse(
            4,
            6,
            5,
            "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥",
            "uddhared ātmanātmānaṁ nātmānam avasādayet\nātmaiva hy ātmano bandhur ātmaiva ripur ātmanaḥ",
            "Elevate yourself by your own mind, and do not degrade yourself. Mind is the friend of the conditioned soul, and his enemy as well.",
            "You are your own coach. Through a structured start, positive habits, and clear morning checklists, you discipline the mind to act as your ultimate ally."
        ),
        GitaVerse(
            5,
            6,
            6,
            "बन्धुरात्मात्मनस्तस्य येनात्मैवात्मना जितः।\nअनात्मनस्तु शत्रुत्वे वर्तेतात्मैव शत्रुवत्॥",
            "bandhur ātmā tmanas tasya yenātmaivātmanā jitaḥ\nanātmanas tu śatrutve vartetātmaiva śatru-vat",
            "For him who has conquered the mind, the mind is the best of friends; but for one who has failed to do so, his mind will remain the greatest enemy.",
            "Self-mastery begins with routines. By mastering your body and morning hygiene first, you prepare your mind for complex cognitive tasks like trading."
        ),
        GitaVerse(
            6,
            18,
            20,
            "सर्वभूतेषु येनैकं भावमव्ययमीक्षते।\nअविभक्तं विभक्तेषु तज्ज्ञानं विद्धि सात्त्विकम्॥",
            "sarva-bhūteṣu yenaikaṁ bhāvam avyayam īkṣate\navibhaktaṁ vibhakteṣu taj jñānaṁ viddhi sāttvikam",
            "That knowledge by which one undivided spiritual nature is seen in all living entities, though they are divided into many forms, is in the mode of goodness.",
            "Develop a unified perspective. Understand how small habits connect to your macro vision. Everything starts from a clean morning and ends with clean execute orders."
        ),
        GitaVerse(
            7,
            3,
            19,
            "तस्मादसक्तः सततं कार्यं कर्म समाचर।\nअसक्तो ह्याचरन्कर्म परमाप्नोति पूरुषः॥",
            "tasmād asaktaḥ satataṁ kāryaṁ karma samācara\nasakto hy ācaran karma param āpnoti pūruṣaḥ",
            "Therefore, without being attached to the fruits of activities, one should act as a matter of duty, for by working without attachment one attains the Supreme.",
            "Show up and do the work regardless of how you feel. Emotional attachment blockades productivity. Action is your designated calling."
        ),
        GitaVerse(
            8,
            18,
            47,
            "श्रेयान्स्वधर्मो विगुणः परधर्मात्स्वनुष्ठितात्।\nस्वभावनियतं कर्म कुर्वन्नाप्नोति किल्बिषम्॥",
            "śreyān sva-dharmo viguṇaḥ para-dharmāt sv-anuṣṭhitāt\nsva-bhāva-niyataṁ karma kurvan nāpnoti kilbiṣam",
            "It is far better to discharge one’s own prescribed duties, even though they may be performed imperfectly, than another’s duties. Duties prescribed according to one’s own nature are never affected by sinful reactions.",
            "Stay true to your individual strategy. Avoid mimicking other traders or routines that do not fit your personal lifestyle and rules."
        )
    )
}

package com.thesecretplace.app.data

import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory

// Exact port of all 28 meditations from MeditationDataModel.swift
val sampleMeditations: List<Meditation> = listOf(

    // MARK: - Morning
    Meditation(
        id = "morningMeditation",
        title = "Morning Meditation",
        duration = "4 min",
        description = "A gentle start to your day — settling your mind and setting a peaceful intention before the world begins.",
        imageName = "morning_beach",
        audioFileName = "morningMeditation",
        category = MeditationCategory.MORNING,
        isNew = true
    ),
    Meditation(
        id = "sunlightMeditation",
        title = "Sunlight Meditation",
        duration = "11 min",
        description = "Invite the warmth and clarity of sunlight into your mind and body as you ease into a new day.",
        imageName = "morning_beach",
        audioFileName = "sunlightMeditation",
        category = MeditationCategory.MORNING
    ),

    // MARK: - Breathwork
    Meditation(
        id = "breathe1Minute",
        title = "Breathe for 1 Minute",
        duration = "1 min",
        description = "A quick reset. One focused minute of breathing to clear your head before the day begins.",
        imageName = "morning_beach",
        audioFileName = "breathe1Minute",
        category = MeditationCategory.BREATHWORK
    ),
    Meditation(
        id = "breathe3Minutes",
        title = "Breathe for 3 Minutes",
        duration = "3 min",
        description = "Three minutes of intentional breath to ground yourself and step into your morning with calm.",
        imageName = "morning_beach",
        audioFileName = "breathe3Minutes",
        category = MeditationCategory.BREATHWORK
    ),
    Meditation(
        id = "breathe6Minutes",
        title = "Breathe for 6 Minutes",
        duration = "6 min",
        description = "A longer breathing practice to steady your nervous system and set a peaceful tone.",
        imageName = "morning_beach",
        audioFileName = "breathe6Minutes",
        category = MeditationCategory.BREATHWORK
    ),
    Meditation(
        id = "boxBreathing",
        title = "Box Breathing",
        duration = "4 min",
        description = "Inhale, hold, exhale, hold — the box breathing technique used by athletes and Navy SEALs to find calm under pressure.",
        imageName = "beach",
        audioFileName = "boxBreathing",
        category = MeditationCategory.BREATHWORK
    ),
    Meditation(
        id = "breathworkWithRain",
        title = "Breathwork with Rain",
        duration = "8 min",
        description = "Guided breathwork layered with gentle rain sounds — a deeply soothing practice for anxious moments.",
        imageName = "rock_image",
        audioFileName = "breathworkWithRain",
        category = MeditationCategory.BREATHWORK
    ),

    // MARK: - Stress Relief
    Meditation(
        id = "bodyScan",
        title = "Body Scan",
        duration = "6 min",
        description = "A guided journey through your body from head to toe, releasing tension you didn't know you were holding.",
        imageName = "feather",
        audioFileName = "bodyScan",
        category = MeditationCategory.STRESS_RELIEF
    ),
    Meditation(
        id = "somaticExercises",
        title = "Somatic Exercises",
        duration = "9 min",
        description = "Gentle body-based movements and awareness to help your nervous system release stored stress.",
        imageName = "rock_stack",
        audioFileName = "somaticExercises",
        category = MeditationCategory.STRESS_RELIEF
    ),
    Meditation(
        id = "focusedAttention",
        title = "Focused Attention",
        duration = "9 min",
        description = "Train your mind to return to the present moment. A classic mindfulness practice for clarity and calm.",
        imageName = "feather",
        audioFileName = "focusedAttention",
        category = MeditationCategory.STRESS_RELIEF
    ),
    Meditation(
        id = "brainHeartCoherence",
        title = "Brain-Heart Coherence",
        duration = "7 min",
        description = "A science-backed practice to synchronize your heart and brain rhythms, bringing your whole system into balance.",
        imageName = "rock_stack",
        audioFileName = "brainHeartCoherence",
        category = MeditationCategory.STRESS_RELIEF
    ),
    Meditation(
        id = "healingWaters",
        title = "Healing Waters Visualization",
        duration = "9 min",
        description = "A guided visualization using the imagery of flowing water to wash away stress and restore your spirit.",
        imageName = "beach",
        audioFileName = "healingWaters",
        category = MeditationCategory.STRESS_RELIEF
    ),
    Meditation(
        id = "seasideRailway",
        title = "Seaside Railway",
        duration = "7 min",
        description = "Let the rhythm of the rails and the sound of the sea carry you to a place of deep rest and release.",
        imageName = "beach",
        audioFileName = "seasideRailway",
        category = MeditationCategory.SOUNDSCAPES
    ),

    // MARK: - Soundscapes
    Meditation(
        id = "calmWaves",
        title = "Calm Waves",
        duration = "11 min",
        description = "Uninterrupted ocean waves to quiet your mind and ease you into deep, restful sleep.",
        imageName = "beach",
        audioFileName = "calmWaves",
        category = MeditationCategory.SOUNDSCAPES
    ),
    Meditation(
        id = "rainSounds",
        title = "Rain Sounds",
        duration = "10 min",
        description = "Soft, steady rain to settle the noise of the day and invite your body into rest.",
        imageName = "rock_image",
        audioFileName = "rainSounds",
        category = MeditationCategory.SOUNDSCAPES
    ),
    Meditation(
        id = "nighttimeForest",
        title = "Nighttime Forest",
        duration = "10 min",
        description = "Crickets, rustling leaves, and the quiet of the forest — nature's own lullaby.",
        imageName = "rock_stack",
        audioFileName = "nighttimeForest",
        category = MeditationCategory.SOUNDSCAPES
    ),
    Meditation(
        id = "cicadas",
        title = "Cicadas",
        duration = "2 min",
        description = "A short burst of summer night sound. Perfect for settling in before a longer sleep track.",
        imageName = "rock_image",
        audioFileName = "cicadas",
        category = MeditationCategory.SOUNDSCAPES
    ),

    // MARK: - Sleep
    Meditation(
        id = "driftOffToSleep",
        title = "Drift Off to Sleep",
        duration = "20 min",
        description = "A long, unhurried journey into rest. Let your body go heavy and your mind grow quiet as sleep finds you.",
        imageName = "floating_water_candles",
        audioFileName = "driftOffToSleep",
        category = MeditationCategory.SLEEP,
        isNew = true
    ),
    Meditation(
        id = "permissionToRest",
        title = "Permission to Rest",
        duration = "11 min",
        description = "You don't have to earn rest. A gentle reminder that stillness is not laziness — it is wisdom.",
        imageName = "floating_water_candles",
        audioFileName = "permissionToRest",
        category = MeditationCategory.SLEEP,
        secondaryCategory = MeditationCategory.STRESS_RELIEF,
        isNew = true
    ),

    // MARK: - Morning (continued)
    Meditation(
        id = "waitingWithPiano",
        title = "Waiting",
        duration = "8 min",
        description = "A meditation for seasons of waiting — learning to rest in the in-between and trust what is coming.",
        imageName = "feather",
        audioFileName = "waitingWithPiano",
        category = MeditationCategory.MORNING
    ),
    Meditation(
        id = "meetingJesusByTheRiver",
        title = "Meeting Jesus by the River",
        duration = "9 min",
        description = "A contemplative visualization of encountering Jesus at the water's edge. Come as you are.",
        imageName = "bible_coffeecup",
        audioFileName = "meetingJesusByTheRiver",
        category = MeditationCategory.MORNING,
        isNew = true
    ),
    Meditation(
        id = "takingEveryThought",
        title = "Taking Every Thought Captive",
        duration = "13 min",
        description = "Based on 2 Corinthians 10:5 — a guided practice for surrendering anxious thoughts and renewing your mind.",
        imageName = "bible_coffeecup",
        audioFileName = "takingEveryThought",
        category = MeditationCategory.MORNING
    ),

    // MARK: - Stress Relief (continued)
    Meditation(
        id = "surrenderToStillness",
        title = "Surrender to Stillness",
        duration = "5 min",
        description = "Release the need to control and simply be. A quiet invitation to let go and rest in the present moment.",
        imageName = "feather",
        audioFileName = "surrenderToStillness",
        category = MeditationCategory.STRESS_RELIEF,
        isNew = true
    ),

    // MARK: - Meditatio
    Meditation(
        id = "romans828Affirmations",
        title = "Romans 8:28 Affirmations",
        duration = "4 min",
        description = "Grounded in Romans 8:28 — speak truth over your life and anchor your heart in the promise that all things work together for good.",
        imageName = "bible_coffeecup",
        audioFileName = "romans828Affirmations",
        category = MeditationCategory.MEDITATIO,
        isNew = true
    ),
    Meditation(
        id = "knowingMyValue",
        title = "Knowing My Value in Christ",
        duration = "5 min",
        description = "A prayerful meditation on your identity and worth as seen through the eyes of God.",
        imageName = "bible_coffeecup",
        audioFileName = "knowingMyValue",
        category = MeditationCategory.MEDITATIO,
        isNew = true
    ),
    Meditation(
        id = "letThereBeLight",
        title = "Let There Be Light",
        duration = "9 min",
        description = "Meditate on the presence and power of God's light — driving out darkness, bringing clarity and peace.",
        imageName = "bible_coffeecup",
        audioFileName = "letThereBeLight",
        category = MeditationCategory.MEDITATIO
    )
)

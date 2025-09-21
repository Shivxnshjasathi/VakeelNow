package com.example.ailegalassistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

// --- Localization: String Resources ---
object AppStrings {
    data class Language(val code: String, val name: String, val locale: Locale)
    val supportedLanguages = listOf(
        Language("en", "English", Locale.ENGLISH),
        Language("hi", "हिन्दी", Locale("hi", "IN")), // Hindi
        Language("bn", "বাংলা", Locale("bn", "IN")), // Bengali
        Language("gu", "ગુજરાતી", Locale("gu", "IN")), // Gujarati
        Language("pa", "ਪੰਜਾਬੀ", Locale("pa", "IN")), // Punjabi
        Language("kn", "ಕನ್ನಡ", Locale("kn", "IN")), // Kannada
        Language("ml", "മലയാളം", Locale("ml", "IN")), // Malayalam
        Language("or", "ଓଡ଼ିଆ", Locale("or", "IN")), // Odia
        Language("ur", "اردو", Locale("ur", "IN")), // Urdu
        Language("ta", "தமிழ்", Locale("ta", "IN")), // Tamil
        Language("te", "తెలుగు", Locale("te", "IN")) // Telugu
    )

    private val strings = mapOf(
        "en" to mapOf(
            "welcome_message" to "Welcome to Vakeelnow! I am your AI Legal Assistant. How can I assist you today?",
            "new_chat" to "New Chat",
            "start_new_chat" to "Start New Chat",
            "find_lawyer" to "Find a Lawyer",
            "recent_chats" to "Recent Chats",
            "theme" to "Theme",
            "language" to "Language",
            "voice" to "Voice",
            "app_title" to "VakeelNow",
            "app_subtitle" to "A Legal Assistant to help you with legal matters",
            "ask_anything" to "Ask me anything...",
            "share_conversation" to "Share Conversation",
            "find_local_lawyer" to "Find a Local Lawyer",
            "enter_city" to "Enter City",
            "area_of_law" to "Area of Law",
            "search" to "Search",
            "error_message" to "Sorry, an error occurred: %s"
        ),
        "hi" to mapOf(
            "welcome_message" to "वकीलनॉउ में आपका स्वागत है! मैं आपका एआई कानूनी सहायक हूं। मैं आज आपकी कैसे सहायता कर सकता हूं?",
            "new_chat" to "नई चैट",
            "start_new_chat" to "नई चैट शुरू करें",
            "find_lawyer" to "वकील खोजें",
            "recent_chats" to "हाल की चैट",
            "theme" to "थीम",
            "language" to "भाषा",
            "voice" to "आवाज़",
            "app_title" to "वकीलनॉउ",
            "app_subtitle" to "कानूनी मामलों में आपकी मदद करने के लिए एक कानूनी सहायक",
            "ask_anything" to "मुझसे कुछ भी पूछें...",
            "share_conversation" to "बातचीत साझा करें",
            "find_local_lawyer" to "स्थानीय वकील खोजें",
            "enter_city" to "शहर दर्ज करें",
            "area_of_law" to "कानून का क्षेत्र",
            "search" to "खोजें",
            "error_message" to "क्षमा करें, एक त्रुटि हुई: %s"
        ),
        "bn" to mapOf(
            "welcome_message" to "Vakeelnow-তে স্বাগতম! আমি আপনার AI আইনি সহকারী। আমি আজ আপনাকে কিভাবে সাহায্য করতে পারি?",
            "new_chat" to "নতুন চ্যাট",
            "start_new_chat" to "নতুন চ্যাট শুরু করুন",
            "find_lawyer" to "আইনজীবী খুঁজুন",
            "recent_chats" to "সাম্প্রতিক চ্যাট",
            "theme" to "থিম",
            "language" to "ভাষা",
            "voice" to "কণ্ঠ",
            "app_title" to "VakeelNow",
            "app_subtitle" to "আইনি বিষয়ে আপনাকে সাহায্য করার জন্য একজন আইনি সহকারী",
            "ask_anything" to "আমাকে কিছু জিজ্ঞাসা করুন...",
            "share_conversation" to "কথোপকথন শেয়ার করুন",
            "find_local_lawyer" to "স্থানীয় আইনজীবী খুঁজুন",
            "enter_city" to "শহর লিখুন",
            "area_of_law" to "আইনের ক্ষেত্র",
            "search" to "অনুসন্ধান করুন",
            "error_message" to "দুঃখিত, একটি ত্রুটি ঘটেছে: %s"
        ),
        "gu" to mapOf(
            "welcome_message" to "વકીલનાઉમાં આપનું સ્વાગત છે! હું તમારો AI કાનૂની સહાયક છું. હું આજે તમને કેવી રીતે મદદ કરી શકું?",
            "new_chat" to "નવી ચેટ",
            "start_new_chat" to "નવી ચેટ શરૂ કરો",
            "find_lawyer" to "વકીલ શોધો",
            "recent_chats" to "તાજેતરની ચેટ્સ",
            "theme" to "થીમ",
            "language" to "ભાષા",
            "voice" to "અવાજ",
            "app_title" to "વકીલનાઉ",
            "app_subtitle" to "કાનૂની બાબતોમાં તમને મદદ કરવા માટે કાનૂની સહાયક",
            "ask_anything" to "મને કંઈપણ પૂછો...",
            "share_conversation" to "વાર્તાલાપ શેર કરો",
            "find_local_lawyer" to "સ્થાનિક વકીલ શોધો",
            "enter_city" to "શહેર દાખલ કરો",
            "area_of_law" to "કાયદાનું ક્ષેત્ર",
            "search" to "શોધો",
            "error_message" to "માફ કરશો, એક ભૂલ આવી: %s"
        ),
        "pa" to mapOf(
            "welcome_message" to "ਵਕੀਲਨਾਉ ਵਿੱਚ ਤੁਹਾਡਾ ਸੁਆਗਤ ਹੈ! ਮੈਂ ਤੁਹਾਡਾ ਏਆਈ ਕਾਨੂੰਨੀ ਸਹਾਇਕ ਹਾਂ। ਮੈਂ ਅੱਜ ਤੁਹਾਡੀ ਕਿਵੇਂ ਮਦਦ ਕਰ ਸਕਦਾ ਹਾਂ?",
            "new_chat" to "ਨਵੀਂ ਗੱਲਬਾਤ",
            "start_new_chat" to "ਨਵੀਂ ਗੱਲਬਾਤ ਸ਼ੁਰੂ ਕਰੋ",
            "find_lawyer" to "ਵਕੀਲ ਲੱਭੋ",
            "recent_chats" to "ਹਾਲੀਆ ਗੱਲਬਾਤਾਂ",
            "theme" to "ਥੀਮ",
            "language" to "ਭਾਸ਼ਾ",
            "voice" to "ਆਵਾਜ਼",
            "app_title" to "ਵਕੀਲਨਾਉ",
            "app_subtitle" to "ਕਾਨੂੰਨੀ ਮਾਮਲਿਆਂ ਵਿੱਚ ਤੁਹਾਡੀ ਮਦਦ ਕਰਨ ਲਈ ਇੱਕ ਕਾਨੂੰਨੀ ਸਹਾਇਕ",
            "ask_anything" to "ਮੈਨੂੰ ਕੁਝ ਵੀ ਪੁੱਛੋ...",
            "share_conversation" to "ਗੱਲਬਾਤ ਸਾਂਝੀ ਕਰੋ",
            "find_local_lawyer" to "ਸਥਾਨਕ ਵਕੀਲ ਲੱਭੋ",
            "enter_city" to "ਸ਼ਹਿਰ ਦਾਖਲ ਕਰੋ",
            "area_of_law" to "ਕਾਨੂੰਨ ਦਾ ਖੇਤਰ",
            "search" to "ਖੋਜ",
            "error_message" to "ਮੁਆਫ ਕਰਨਾ, ਇੱਕ ਗਲਤੀ ਹੋਈ: %s"
        ),
        "kn" to mapOf(
            "welcome_message" to "ವಕೀಲ್‌ನೌಗೆ ಸುಸ್ವಾಗತ! ನಾನು ನಿಮ್ಮ AI ಕಾನೂನು ಸಹಾಯಕ. ನಾನು ಇಂದು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಲಿ?",
            "new_chat" to "ಹೊಸ ಚಾಟ್",
            "start_new_chat" to "ಹೊಸ ಚಾಟ್ ಪ್ರಾರಂಭಿಸಿ",
            "find_lawyer" to "ವಕೀಲರನ್ನು ಹುಡುಕಿ",
            "recent_chats" to "ಇತ್ತೀಚಿನ ಚಾಟ್‌ಗಳು",
            "theme" to "ಥೀಮ್",
            "language" to "ಭಾಷೆ",
            "voice" to "ಧ್ವನಿ",
            "app_title" to "ವಕೀಲ್‌ನೌ",
            "app_subtitle" to "ಕಾನೂನು ವಿಷಯಗಳಲ್ಲಿ ನಿಮಗೆ ಸಹಾಯ ಮಾಡಲು ಕಾನೂನು ಸಹಾಯಕ",
            "ask_anything" to "ನನ್ನನ್ನು ಏನು ಬೇಕಾದರೂ ಕೇಳಿ...",
            "share_conversation" to "ಸಂಭಾಷಣೆಯನ್ನು ಹಂಚಿಕೊಳ್ಳಿ",
            "find_local_lawyer" to "ಸ್ಥಳೀಯ ವಕೀಲರನ್ನು ಹುಡುಕಿ",
            "enter_city" to "ನಗರವನ್ನು ನಮೂದಿಸಿ",
            "area_of_law" to "ಕಾನೂನಿನ ಕ್ಷೇತ್ರ",
            "search" to "ಹುಡುಕಿ",
            "error_message" to "ಕ್ಷಮಿಸಿ, ದೋಷವೊಂದು ಸಂಭವಿಸಿದೆ: %s"
        ),
        "ml" to mapOf(
            "welcome_message" to "വക്കീൽനൗ-ലേക്ക് സ്വാഗതം! ഞാൻ നിങ്ങളുടെ AI നിയമ സഹായിയാണ്. ഇന്ന് ഞാൻ നിങ്ങളെ എങ്ങനെ സഹായിക്കും?",
            "new_chat" to "പുതിയ ചാറ്റ്",
            "start_new_chat" to "പുതിയ ചാറ്റ് ആരംഭിക്കുക",
            "find_lawyer" to "അഭിഭാഷകനെ കണ്ടെത്തുക",
            "recent_chats" to "സമീപകാല ചാറ്റുകൾ",
            "theme" to "തീം",
            "language" to "ഭാഷ",
            "voice" to "ശബ്ദം",
            "app_title" to "വക്കീൽനൗ",
            "app_subtitle" to "നിയമപരമായ കാര്യങ്ങളിൽ നിങ്ങളെ സഹായിക്കാൻ ഒരു നിയമ സഹായി",
            "ask_anything" to "എന്നെ എന്തും ചോദിക്കൂ...",
            "share_conversation" to "സംഭാഷണം പങ്കിടുക",
            "find_local_lawyer" to "പ്രാദേശിക അഭിഭാഷകനെ കണ്ടെത്തുക",
            "enter_city" to "നഗരം നൽകുക",
            "area_of_law" to "നിയമ മേഖല",
            "search" to "തിരയുക",
            "error_message" to "ക്ഷമിക്കണം, ഒരു പിശക് സംഭവിച്ചു: %s"
        ),
        "or" to mapOf(
            "welcome_message" to "Vakeelnowକୁ ସ୍ଵାଗତ! ମୁଁ ଆପଣଙ୍କର AI ଆଇନଗତ ସହାୟକ। ମୁଁ ଆଜି ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?",
            "new_chat" to "ନୂଆ ଚାଟ୍",
            "start_new_chat" to "ନୂଆ ଚାଟ୍ ଆରମ୍ଭ କରନ୍ତୁ",
            "find_lawyer" to "ଓକିଲ ଖୋଜନ୍ତୁ",
            "recent_chats" to "ସାମ୍ପ୍ରତିକ ଚାଟ୍",
            "theme" to "ଥିମ୍",
            "language" to "ଭାଷା",
            "voice" to "ଭଏସ୍",
            "app_title" to "VakeelNow",
            "app_subtitle" to "ଆଇନଗତ ମାମଲାରେ ଆପଣଙ୍କୁ ସାହାଯ୍ୟ କରିବା ପାଇଁ ଜଣେ ଆଇନଗତ ସହାୟକ",
            "ask_anything" to "ମୋତେ କିଛି ବି ପଚାରନ୍ତୁ...",
            "share_conversation" to "ବାର୍ତ୍ତାଳାପ ସେୟାର କରନ୍ତୁ",
            "find_local_lawyer" to "ସ୍ଥାନୀୟ ଓକିଲ ଖୋଜନ୍ତୁ",
            "enter_city" to "ସହର ପ୍ରବେଶ କରନ୍ତୁ",
            "area_of_law" to "ଆଇନର କ୍ଷେତ୍ର",
            "search" to "ଖୋଜନ୍ତୁ",
            "error_message" to "ଦୁଃଖିତ, ଏକ ତ୍ରୁଟି ଘଟିଛି: %s"
        ),
        "ur" to mapOf(
            "welcome_message" to "Vakeelnow میں خوش آمدید! میں آپ کا AI قانونی اسسٹنٹ ہوں۔ میں آج آپ کی کس طرح مدد کر سکتا ہوں؟",
            "new_chat" to "نئی چیٹ",
            "start_new_chat" to "نئی چیٹ شروع کریں",
            "find_lawyer" to "وکیل تلاش کریں",
            "recent_chats" to "حالیہ چیٹس",
            "theme" to "تھیم",
            "language" to "زبان",
            "voice" to "آواز",
            "app_title" to "VakeelNow",
            "app_subtitle" to "قانونی معاملات میں آپ کی مدد کے لیے ایک قانونی اسسٹنٹ",
            "ask_anything" to "مجھ سے کچھ بھی پوچھیں...",
            "share_conversation" to "گفتگو کا اشتراک کریں",
            "find_local_lawyer" to "مقامی وکیل تلاش کریں",
            "enter_city" to "شہر درج کریں",
            "area_of_law" to "قانون کا شعبہ",
            "search" to "تلاش کریں",
            "error_message" to "معذرت، ایک خرابی پیش آگئی: %s"
        ),
        "ta" to mapOf(
            "welcome_message" to "Vakeelnow-க்கு வரவேற்கிறோம்! நான் உங்கள் AI சட்ட உதவியாளர். இன்று நான் உங்களுக்கு எப்படி உதவ முடியும்?",
            "new_chat" to "புதிய அரட்டை",
            "start_new_chat" to "புதிய அரட்டையைத் தொடங்கு",
            "find_lawyer" to "வழக்கறிஞரைக் கண்டுபிடி",
            "recent_chats" to "சமீபத்திய அரட்டைகள்",
            "theme" to "தீம்",
            "language" to "மொழி",
            "voice" to "குரல்",
            "app_title" to "VakeelNow",
            "app_subtitle" to "சட்ட விஷயங்களில் உங்களுக்கு உதவ ஒரு சட்ட உதவியாளர்",
            "ask_anything" to "என்னிடம் எதையும் கேளுங்கள்...",
            "share_conversation" to "உரையாடலைப் பகிரவும்",
            "find_local_lawyer" to "உள்ளூர் வழக்கறிஞரைக் கண்டுபிடி",
            "enter_city" to "நகரத்தை உள்ளிடவும்",
            "area_of_law" to "சட்டப் பகுதி",
            "search" to "தேடு",
            "error_message" to "மன்னிக்கவும், ஒரு பிழை ஏற்பட்டது: %s"
        ),
        "te" to mapOf(
            "welcome_message" to "వకీల్‌నౌకు స్వాగతం! నేను మీ AI లీగల్ అసిస్టెంట్‌ని. ఈ రోజు నేను మీకు ఎలా సహాయపడగలను?",
            "new_chat" to "కొత్త చాట్",
            "start_new_chat" to "కొత్త చాట్ ప్రారంభించండి",
            "find_lawyer" to "న్యాయవాదిని కనుగొనండి",
            "recent_chats" to "ఇటీవలి చాట్‌లు",
            "theme" to "థీమ్",
            "language" to "భాష",
            "voice" to "వాయిస్",
            "app_title" to "వకీల్‌నౌ",
            "app_subtitle" to "చట్టపరమైన విషయాలలో మీకు సహాయం చేయడానికి ఒక లీగల్ అసిస్టెంట్",
            "ask_anything" to "నన్ను ఏదైనా అడగండి...",
            "share_conversation" to "సంభాషణను పంచుకోండి",
            "find_local_lawyer" to "స్థానిక న్యాయవాదిని కనుగొనండి",
            "enter_city" to "నగరాన్ని నమోదు చేయండి",
            "area_of_law" to "చట్టం యొక్క ప్రాంతం",
            "search" to "వెతకండి",
            "error_message" to "క్షమించండి, లోపం సంభవించింది: %s"
        )
    )

    fun getString(langCode: String, key: String): String {
        return strings[langCode]?.get(key) ?: strings["en"]?.get(key) ?: ""
    }
}


// --- Data Classes for Chat and History ---
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    val messages: MutableList<Message>,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Theme Definition (Enhanced) ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7B9BFF), onPrimary = Color.White, background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E), onBackground = Color(0xFFEAEAEA), onSurface = Color(0xFFEAEAEA),
    primaryContainer = Color(0xFF3A3A5A), onPrimaryContainer = Color(0xFFD0D0FF)
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006AFF), onPrimary = Color.White, background = Color(0xFFF4F6FC),
    surface = Color(0xFFFFFFFF), onBackground = Color(0xFF1C1C1E), onSurface = Color(0xFF1C1C1E),
    primaryContainer = Color(0xFFD8E2FF), onPrimaryContainer = Color(0xFF001C5A)
)

@Composable
fun AILegalAssistantTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography(
        bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    ), content = content)
}

// --- Local Storage: Conversation Repository ---
class ConversationRepository(context: Context) {
    private val prefs = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val historyKey = "conversation_history"
    private val themeKey = "theme_preference"
    private val languageKey = "language_preference"
    private val voiceKey = "voice_preference"


    fun saveConversations(conversations: List<Conversation>) {
        val json = gson.toJson(conversations)
        prefs.edit().putString(historyKey, json).apply()
    }
    fun loadConversations(): MutableList<Conversation> {
        val json = prefs.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Conversation>>() {}.type
            gson.fromJson(json, type)
        } else { mutableListOf() }
    }
    fun saveTheme(isDark: Boolean) = prefs.edit().putBoolean(themeKey, isDark).apply()
    fun loadTheme(isSystemDark: Boolean): Boolean = prefs.getBoolean(themeKey, isSystemDark)

    fun saveLanguage(langCode: String) = prefs.edit().putString(languageKey, langCode).apply()
    fun loadLanguage(): String = prefs.getString(languageKey, "en") ?: "en"

    fun saveVoice(voiceName: String) = prefs.edit().putString(voiceKey, voiceName).apply()
    fun loadVoice(): String = prefs.getString(voiceKey, "") ?: ""
}

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var playingMessageId by mutableStateOf<String?>(null)

        setContent {
            val context = LocalContext.current
            val repository = remember { ConversationRepository(context) }
            val isSystemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(repository.loadTheme(isSystemDark)) }
            var languageCode by remember { mutableStateOf(repository.loadLanguage()) }
            var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
            var selectedVoiceName by remember { mutableStateOf(repository.loadVoice()) }


            // Initialize TTS here, reacting to language changes
            LaunchedEffect(languageCode) {
                tts?.stop()
                tts?.shutdown()
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val selectedLocale = AppStrings.supportedLanguages.find { it.code == languageCode }?.locale ?: Locale.US
                        val voicesForLocale = tts?.voices?.filter { it.locale == selectedLocale }?.sortedBy { it.name } ?: emptyList()
                        availableVoices = voicesForLocale

                        val currentVoiceIsValid = voicesForLocale.any { it.name == selectedVoiceName }
                        if (!currentVoiceIsValid) {
                            selectedVoiceName = voicesForLocale.firstOrNull()?.name ?: ""
                            repository.saveVoice(selectedVoiceName)
                        }

                        tts?.language = selectedLocale
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) { runOnUiThread { playingMessageId = utteranceId } }
                            override fun onDone(utteranceId: String?) { runOnUiThread { if (playingMessageId == utteranceId) playingMessageId = null } }
                            override fun onError(utteranceId: String?) { runOnUiThread { if (playingMessageId == utteranceId) playingMessageId = null } }
                        })
                    }
                }
            }

            AILegalAssistantTheme(darkTheme = isDarkTheme) {
                LegalAssistantChatScreen(
                    repository = repository,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it; repository.saveTheme(it) },
                    textToSpeech = tts,
                    playingMessageId = playingMessageId,
                    setPlayingMessageId = { id -> playingMessageId = id },
                    languageCode = languageCode,
                    onLanguageChange = { newLang ->
                        languageCode = newLang
                        repository.saveLanguage(newLang)
                        // Voice will be updated automatically by the LaunchedEffect
                    },
                    availableVoices = availableVoices,
                    selectedVoiceName = selectedVoiceName,
                    onVoiceChange = { voiceName ->
                        selectedVoiceName = voiceName
                        repository.saveVoice(voiceName)
                    }
                )
            }
        }
    }
    override fun onDestroy() {
        tts?.stop(); tts?.shutdown(); super.onDestroy()
    }
}

// --- Main Chat Screen with Drawer ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAssistantChatScreen(
    repository: ConversationRepository, isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit,
    textToSpeech: TextToSpeech?, playingMessageId: String?, setPlayingMessageId: (String?) -> Unit,
    languageCode: String, onLanguageChange: (String) -> Unit,
    availableVoices: List<Voice>, selectedVoiceName: String, onVoiceChange: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var conversations by remember { mutableStateOf(repository.loadConversations()) }
    var currentConversation by remember { mutableStateOf(createNewConversation(languageCode)) }
    var showLawyerSearchDialog by remember { mutableStateOf(false) }

    // React to language changes to update the initial message in a new chat
    LaunchedEffect(languageCode) {
        if (currentConversation.messages.size == 1 && !currentConversation.messages.first().isFromUser) {
            val newTitle = AppStrings.getString(languageCode, "new_chat")
            val newMessage = AppStrings.getString(languageCode, "welcome_message")
            currentConversation = currentConversation.copy(
                title = newTitle,
                messages = mutableListOf(Message(text = newMessage, isFromUser = false))
            )
        }
    }


    fun saveState() {
        if (currentConversation.messages.any { it.isFromUser }) {
            val index = conversations.indexOfFirst { it.id == currentConversation.id }
            val updatedList = conversations.toMutableList()
            if (index != -1) updatedList[index] = currentConversation.copy(timestamp = System.currentTimeMillis())
            else updatedList.add(0, currentConversation)
            val sortedList = updatedList.sortedByDescending { it.timestamp }
            repository.saveConversations(sortedList)
            conversations = sortedList.toMutableList()
        }
    }
    fun handleNewConversation() { saveState(); currentConversation = createNewConversation(languageCode); coroutineScope.launch { drawerState.close() } }
    fun switchConversation(conversationId: String) { saveState(); conversations.find { it.id == conversationId }?.let { currentConversation = it }; coroutineScope.launch { drawerState.close() } }
    fun deleteConversation(conversationId: String) {
        val updatedList = conversations.filterNot { it.id == conversationId }
        repository.saveConversations(updatedList); conversations = updatedList.toMutableList()
        if (currentConversation.id == conversationId) { currentConversation = conversations.firstOrNull() ?: createNewConversation(languageCode) }
    }

    if (showLawyerSearchDialog) { FindLawyerDialog(languageCode = languageCode, onDismiss = { showLawyerSearchDialog = false }) }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        DrawerContent(
            conversations = conversations,
            isDarkTheme = isDarkTheme,
            onConversationClick = ::switchConversation,
            onNewChatClick = ::handleNewConversation,
            onDeleteConversation = ::deleteConversation,
            onThemeChange = onThemeChange,
            onFindLawyerClick = { coroutineScope.launch { drawerState.close() }; showLawyerSearchDialog = true },
            languageCode = languageCode,
            onLanguageChange = onLanguageChange,
            availableVoices = availableVoices,
            selectedVoiceName = selectedVoiceName,
            onVoiceChange = onVoiceChange
        )
    }) {
        ChatScreenContent(
            conversation = currentConversation,
            onSendMessage = ::saveState,
            onMenuClick = { coroutineScope.launch { drawerState.open() } },
            textToSpeech = textToSpeech,
            playingMessageId = playingMessageId,
            setPlayingMessageId = setPlayingMessageId,
            languageCode = languageCode,
            selectedVoiceName = selectedVoiceName
        )
    }
}

fun createNewConversation(langCode: String): Conversation {
    return Conversation(
        title = AppStrings.getString(langCode, "new_chat"),
        messages = mutableListOf(Message(text = AppStrings.getString(langCode, "welcome_message"), isFromUser = false))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    conversations: List<Conversation>, isDarkTheme: Boolean, onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit, onDeleteConversation: (String) -> Unit, onThemeChange: (Boolean) -> Unit,
    onFindLawyerClick: () -> Unit, languageCode: String, onLanguageChange: (String) -> Unit,
    availableVoices: List<Voice>, selectedVoiceName: String, onVoiceChange: (String) -> Unit
) {
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var voiceDropdownExpanded by remember { mutableStateOf(false) }


    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.85f)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(AppStrings.getString(languageCode, "app_title"), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(5.dp));
                    Text(AppStrings.getString(languageCode, "app_subtitle"), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Button(onClick = onNewChatClick, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)) { Icon(Icons.Default.Add, "New Chat"); Spacer(Modifier.width(8.dp)); Text(AppStrings.getString(languageCode, "start_new_chat")) }

            OutlinedButton(onClick = onFindLawyerClick, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)) { Icon(Icons.Default.Search, "Find a Lawyer"); Spacer(Modifier.width(8.dp)); Text(AppStrings.getString(languageCode, "find_lawyer")) }
            Spacer(Modifier.height(16.dp)); Divider(modifier = Modifier.padding(horizontal = 24.dp))
            Text(AppStrings.getString(languageCode, "recent_chats"), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) { items(conversations, key = { it.id }) { conv -> ConversationHistoryItem(conv, { onConversationClick(conv.id) }, { onDeleteConversation(conv.id) }) } }
            Divider(modifier = Modifier.padding(horizontal = 24.dp))

            // Theme Toggle
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(AppStrings.getString(languageCode, "theme"), style = MaterialTheme.typography.bodyLarge); ThemeToggleButton(isDarkTheme, onThemeChange)
            }

            // Language Selector
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(AppStrings.getString(languageCode, "language"), style = MaterialTheme.typography.bodyLarge)
                ExposedDropdownMenuBox(expanded = languageDropdownExpanded, onExpandedChange = { languageDropdownExpanded = !languageDropdownExpanded }) {
                    Row(
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { languageDropdownExpanded = true }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            AppStrings.supportedLanguages.find { it.code == languageCode }?.name ?: "English",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded)
                    }
                    ExposedDropdownMenu(expanded = languageDropdownExpanded, onDismissRequest = { languageDropdownExpanded = false }) {
                        AppStrings.supportedLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.name) },
                                onClick = {
                                    onLanguageChange(lang.code)
                                    languageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Voice Selector
            if (availableVoices.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(AppStrings.getString(languageCode, "voice"), style = MaterialTheme.typography.bodyLarge)
                    ExposedDropdownMenuBox(
                        expanded = voiceDropdownExpanded,
                        onExpandedChange = { voiceDropdownExpanded = !voiceDropdownExpanded }) {
                        Row(
                            modifier = Modifier
                                .menuAnchor()
                                .clickable { voiceDropdownExpanded = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice", tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = selectedVoiceName.split("-").lastOrNull() ?: "Default",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceDropdownExpanded)
                        }
                        ExposedDropdownMenu(
                            expanded = voiceDropdownExpanded,
                            onDismissRequest = { voiceDropdownExpanded = false }) {
                            availableVoices.forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text(voice.name.split("-").lastOrNull() ?: voice.name) },
                                    onClick = {
                                        onVoiceChange(voice.name)
                                        voiceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun ThemeToggleButton(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val icon = if (isDarkTheme) Icons.Default.Nightlight else Icons.Default.WbSunny
    Switch(checked = isDarkTheme, onCheckedChange = onThemeChange, thumbContent = { Icon(icon, "Theme Icon", modifier = Modifier.size(SwitchDefaults.IconSize)) })
}

@Composable
fun ConversationHistoryItem(conversation: Conversation, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(conversation.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
            Text(conversation.messages.lastOrNull()?.text ?: "No messages", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    conversation: Conversation, onSendMessage: () -> Unit, onMenuClick: () -> Unit,
    textToSpeech: TextToSpeech?, playingMessageId: String?, setPlayingMessageId: (String?) -> Unit,
    languageCode: String, selectedVoiceName: String
) {
    val coroutineScope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val basePrompt = "I need legal advice regarding a personal issue. Please act as a knowledgeable legal assistant specializing in Indian law and the Indian Constitution. Do not provide a definitive legal opinion, but rather explain the relevant legal principles, key points to consider, and suggest the typical next steps. Explain the relevant legal context, including applicable Acts, Sections, and landmark court judgments, while also highlighting important considerations and potential challenges. Remember, this is for informational purposes only and is not a substitute for advice from a qualified lawyer. Here is my situation: "

    fun sendMessage() {
        if (userInput.isNotBlank()) {
            val userMessage = Message(text = userInput, isFromUser = true)
            conversation.messages.add(userMessage)
            if (conversation.messages.count { it.isFromUser } == 1) conversation.title = userInput
            val query = userInput; userInput = ""; isLoading = true
            coroutineScope.launch {
                listState.animateScrollToItem(conversation.messages.size)
                try {
                    val fullPrompt = "$basePrompt $query"
                    val encodedPrompt = withContext(Dispatchers.IO) { URLEncoder.encode(fullPrompt, "UTF-8") }
                    val result = withContext(Dispatchers.IO) { URL("https://text.pollinations.ai/$encodedPrompt").readText() }
                    conversation.messages.add(Message(text = result, isFromUser = false))
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMessage = AppStrings.getString(languageCode, "error_message").format(e.message)
                    conversation.messages.add(Message(text = errorMessage, isFromUser = false))
                } finally {
                    isLoading = false; onSendMessage(); listState.animateScrollToItem(conversation.messages.size)
                }
            }
            onSendMessage()
        }
    }

    fun onPlaybackToggle(message: Message) {
        if (playingMessageId == message.id) {
            textToSpeech?.stop(); setPlayingMessageId(null)
        } else {
            textToSpeech?.let { tts ->
                val voice = tts.voices.find { it.name == selectedVoiceName }
                if (voice != null) {
                    tts.voice = voice
                }
                tts.speak(message.text, TextToSpeech.QUEUE_FLUSH, null, message.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(conversation.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, "Menu") } },
                actions = {
                    IconButton(onClick = {
                        val shareText = conversation.messages.joinToString("\n\n") { "${if (it.isFromUser) "You" else "AI"}: ${it.text}" }
                        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
                        context.startActivity(Intent.createChooser(intent, AppStrings.getString(languageCode, "share_conversation")))
                    }) { Icon(Icons.Default.Share, "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            LazyColumn(
                state = listState, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = conversation.messages, key = { it.id }, contentType = { if (it.isFromUser) "user" else "bot" }) { message ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start) {
                        ChatMessageBubble(message)
                        MessageActions(message, isPlaying = message.id == playingMessageId, onPlaybackToggle = { onPlaybackToggle(message) })
                    }
                }
                if (isLoading) { item(contentType = "indicator") { TypingIndicator() } }
            }
            ChatInputBar(value = userInput, onValueChange = { userInput = it }, onSend = ::sendMessage, hint = AppStrings.getString(languageCode, "ask_anything"))
        }
    }
}

@Composable
fun MessageActions(message: Message, isPlaying: Boolean, onPlaybackToggle: () -> Unit) {
    val context = LocalContext.current
    Row(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(modifier = Modifier.size(20.dp), onClick = {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("message", message.text))
        }) { Icon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }

        if (!message.isFromUser) {
            IconButton(modifier = Modifier.size(20.dp), onClick = onPlaybackToggle) {
                Icon(imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.VolumeUp, contentDescription = if (isPlaying) "Stop" else "Read Aloud", tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: Message) {
    val isUser = message.isFromUser
    val userBubbleGradient = Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF3A86FF)))
    val bubbleColor = if (isUser) Color.Transparent else MaterialTheme.colorScheme.surface
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val bubbleModifier = if (isUser) Modifier.widthIn(max = 320.dp) else Modifier.fillMaxWidth()

    Surface(
        color = bubbleColor, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (isUser) 20.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 20.dp),
        modifier = bubbleModifier, shadowElevation = 2.dp
    ) {
        Box(modifier = if (isUser) Modifier.background(userBubbleGradient) else Modifier) {
            if (isUser) {
                Text(message.text, color = textColor, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            } else {
                FormattedMarkdownText(markdown = message.text, textColor = textColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindLawyerDialog(languageCode: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var city by remember { mutableStateOf("") }
    var areaOfLaw by remember { mutableStateOf("Civil") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val lawAreas = listOf("Civil", "Criminal", "Family", "Corporate", "Tax", "Intellectual Property")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(AppStrings.getString(languageCode, "find_local_lawyer"), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text(AppStrings.getString(languageCode, "enter_city")) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(value = areaOfLaw, onValueChange = {}, readOnly = true, label = { Text(AppStrings.getString(languageCode, "area_of_law")) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }, modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor())
                    ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                        lawAreas.forEach { area -> DropdownMenuItem(text = { Text(area) }, onClick = { areaOfLaw = area; isDropdownExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    if (city.isNotBlank()) {
                        val query = "$areaOfLaw lawyer in $city"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"))
                        context.startActivity(intent); onDismiss()
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text(AppStrings.getString(languageCode, "search")) }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
        val infiniteTransition = rememberInfiniteTransition("typing_dots")
        val dotScales = (0..2).map {
            infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(400, delayMillis = it * 150, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "dot_scale_$it")
        }
        Surface(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dotScales.forEach { Box(modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { scaleX = it.value; scaleY = it.value }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))) }
            }
        }
    }
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit, hint: String) {
    Surface(shadowElevation = 8.dp) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(value = value, onValueChange = onValueChange, placeholder = { Text(hint) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val isEnabled = value.isNotBlank()
            val animatedColor by animateColorAsState(if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), label = "button color")
            val animatedScale by animateFloatAsState(if(isEnabled) 1f else 0.9f, label = "button scale")
            IconButton(onClick = onSend, enabled = isEnabled, modifier = Modifier
                .size(48.dp)
                .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
                .clip(CircleShape)
                .background(animatedColor)) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
            }
        }
    }
}

// --- FULL, CORRECT MARKDOWN PARSER ---
@Composable
fun FormattedMarkdownText(markdown: String, textColor: Color) {
    val blocks = remember(markdown, textColor) { parseMarkdownToBlocks(markdown, textColor) }
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> Text(block.content, style = getHeaderStyle(block.level, textColor))
                is MarkdownBlock.Paragraph -> Text(block.content, style = MaterialTheme.typography.bodyLarge.copy(color = textColor))
                is MarkdownBlock.ListItem -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(block.bullet, style = MaterialTheme.typography.bodyLarge.copy(color = textColor, fontWeight = FontWeight.Bold))
                    Text(block.content, style = MaterialTheme.typography.bodyLarge.copy(color = textColor), modifier = Modifier.weight(1f))
                }
                is MarkdownBlock.Blockquote -> Blockquote(block.content, textColor)
                is MarkdownBlock.CodeBlock -> CodeBlock(block.content)
                is MarkdownBlock.Table -> MarkdownTable(block.headers, block.rows, textColor)
                is MarkdownBlock.Divider -> Divider(color = textColor.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
private sealed class MarkdownBlock {
    data class Header(val level: Int, val content: AnnotatedString) : MarkdownBlock()
    data class Paragraph(val content: AnnotatedString) : MarkdownBlock()
    data class ListItem(val content: AnnotatedString, val bullet: String) : MarkdownBlock()
    data class Blockquote(val content: AnnotatedString) : MarkdownBlock()
    data class CodeBlock(val content: String) : MarkdownBlock()
    data class Table(val headers: List<AnnotatedString>, val rows: List<List<AnnotatedString>>) : MarkdownBlock()
    object Divider : MarkdownBlock()
}
@Composable
private fun getHeaderStyle(level: Int, color: Color): TextStyle = when (level) {
    1 -> MaterialTheme.typography.headlineSmall; 2 -> MaterialTheme.typography.titleLarge
    3 -> MaterialTheme.typography.titleMedium; else -> MaterialTheme.typography.titleSmall
}.copy(color = color)
@Composable private fun Blockquote(content: AnnotatedString, color: Color) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
        .padding(start = 4.dp)) {
        Text(content, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge.copy(color = color, fontStyle = FontStyle.Italic))
    }
}
@Composable private fun CodeBlock(code: String) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(16.dp)
        )
    }
}
@Composable private fun MarkdownTable(headers: List<AnnotatedString>, rows: List<List<AnnotatedString>>, textColor: Color) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.background(textColor.copy(alpha = 0.1f))) {
            headers.forEach { header ->
                Text(header, Modifier.weight(1f).padding(8.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = textColor))
            }
        }
        rows.forEach { row ->
            Row {
                row.forEach { cell ->
                    Text(cell, Modifier.weight(1f).padding(8.dp), style = MaterialTheme.typography.bodyMedium.copy(color = textColor))
                }
            }
            Divider(color = textColor.copy(alpha = 0.2f))
        }
    }
}
private fun parseMarkdownToBlocks(markdown: String, textColor: Color): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0
    val codeColor = textColor.copy(alpha = 0.1f)
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("```") -> { val codeBlockLines = lines.subList(i + 1, lines.size).takeWhile { it != "```" }; blocks.add(MarkdownBlock.CodeBlock(codeBlockLines.joinToString("\n"))); i += codeBlockLines.size + 2; continue }
            isTable(lines, i) -> {
                val tableLines = lines.subList(i, lines.size).takeWhile { it.trim().startsWith("|") || it.trim().endsWith("|") }
                val headers = tableLines.first().split("|").map { parseInlineMarkdown(it.trim(), codeColor) }.drop(1).dropLast(1)
                val rows = tableLines.drop(2).map { row -> row.split("|").map { parseInlineMarkdown(it.trim(), codeColor) }.drop(1).dropLast(1) }
                blocks.add(MarkdownBlock.Table(headers, rows)); i += tableLines.size; continue
            }
            line.startsWith("#") -> { val level = line.takeWhile { it == '#' }.count(); val text = line.removePrefix("#".repeat(level)).trim(); blocks.add(MarkdownBlock.Header(level, parseInlineMarkdown(text, codeColor))) }
            line.startsWith(">") -> { val blockLines = mutableListOf<String>()
                var currentLine = i
                while(currentLine < lines.size && lines[currentLine].startsWith(">")) {
                    blockLines.add(lines[currentLine].removePrefix(">").trim())
                    currentLine++
                }
                blocks.add(MarkdownBlock.Blockquote(parseInlineMarkdown(blockLines.joinToString("\n"), codeColor)))
                i = currentLine -1
            }
            line.startsWith("* ") || line.startsWith("- ") -> { val prefix = if (line.startsWith("* ")) "* " else "- "; blocks.add(MarkdownBlock.ListItem(parseInlineMarkdown(line.removePrefix(prefix).trim(), codeColor), "•")) }
            line.matches(Regex("^\\d+\\.\\s.*")) -> { val bullet = line.substringBefore(". ") + "."; val content = line.substringAfter(". ").trim(); blocks.add(MarkdownBlock.ListItem(parseInlineMarkdown(content, codeColor), bullet)) }
            line.matches(Regex("^\\s*---*\\s*$")) -> blocks.add(MarkdownBlock.Divider)
            line.isNotBlank() -> {
                val paraLines = mutableListOf(line)
                var nextLine = i + 1
                while (nextLine < lines.size && lines[nextLine].isNotBlank() && !isMarkdownBlockStart(lines[nextLine])) {
                    paraLines.add(lines[nextLine])
                    nextLine++
                }
                blocks.add(MarkdownBlock.Paragraph(parseInlineMarkdown(paraLines.joinToString(" "), codeColor)))
                i = nextLine - 1
            }
        }
        i++
    }
    return blocks
}
private fun isMarkdownBlockStart(line: String): Boolean {
    return line.startsWith("#") || line.startsWith(">") || line.startsWith("* ") || line.startsWith("- ") ||
            line.matches(Regex("^\\d+\\.\\s.*")) || line.matches(Regex("^\\s*---*\\s*$")) ||
            line.startsWith("```") || line.trim().startsWith("|")
}
private fun isTable(lines: List<String>, currentIndex: Int): Boolean {
    if (currentIndex + 1 >= lines.size) return false
    val header = lines[currentIndex].trim(); val separator = lines[currentIndex + 1].trim()
    return header.count{ it == '|' } > 1 && separator.count{ it == '|' } > 1 && separator.matches(Regex("^[|\\s:-]+$"))
}
private fun parseInlineMarkdown(text: String, codeColor: Color): AnnotatedString {
    val pattern = Pattern.compile("(\\*\\*(.*?)\\*\\*)|(\\*(.*?)\\*)|(`(.*?)`)")
    val matcher = pattern.matcher(text)
    return buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            val startIndex = matcher.start()
            if (startIndex > lastIndex) append(text.substring(lastIndex, startIndex))
            val boldText = matcher.group(2); val italicText = matcher.group(4); val codeText = matcher.group(6)
            when {
                boldText != null -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(boldText) }
                italicText != null -> withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) { append(italicText) }
                codeText != null -> withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, background = codeColor, fontWeight = FontWeight.Medium)) { append(codeText) }
            }
            lastIndex = matcher.end()
        }
        if (lastIndex < text.length) append(text.substring(lastIndex))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ChatScreenPreviewLight() {
    AILegalAssistantTheme(darkTheme = false) {
        LegalAssistantChatScreen(
            repository = ConversationRepository(LocalContext.current),
            isDarkTheme = false,
            onThemeChange = {},
            textToSpeech = null,
            playingMessageId = null,
            setPlayingMessageId = {},
            languageCode = "en",
            onLanguageChange = {},
            availableVoices = emptyList(),
            selectedVoiceName = "",
            onVoiceChange = {}
        )
    }
}


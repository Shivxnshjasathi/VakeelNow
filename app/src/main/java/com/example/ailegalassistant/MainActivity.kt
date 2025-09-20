package com.example.ailegalassistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.res.painterResource
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern


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
}

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        setContent {
            val context = LocalContext.current
            val repository = remember { ConversationRepository(context) }
            val isSystemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(repository.loadTheme(isSystemDark)) }

            AILegalAssistantTheme(darkTheme = isDarkTheme) {
                LegalAssistantChatScreen(
                    repository = repository,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it; repository.saveTheme(it) },
                    textToSpeech = tts
                )
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// --- Main Chat Screen with Drawer ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAssistantChatScreen(
    repository: ConversationRepository,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    textToSpeech: TextToSpeech?
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var conversations by remember { mutableStateOf(repository.loadConversations()) }
    var currentConversation by remember { mutableStateOf(createNewConversation()) }

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

    fun handleNewConversation() {
        saveState()
        currentConversation = createNewConversation()
        coroutineScope.launch { drawerState.close() }
    }

    fun switchConversation(conversationId: String) {
        saveState()
        conversations.find { it.id == conversationId }?.let { currentConversation = it }
        coroutineScope.launch { drawerState.close() }
    }

    fun deleteConversation(conversationId: String) {
        val updatedList = conversations.filterNot { it.id == conversationId }
        repository.saveConversations(updatedList)
        conversations = updatedList.toMutableList()
        if (currentConversation.id == conversationId) {
            currentConversation = conversations.firstOrNull() ?: createNewConversation()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = conversations, isDarkTheme = isDarkTheme,
                onConversationClick = ::switchConversation,
                onNewChatClick = ::handleNewConversation,
                onDeleteConversation = ::deleteConversation,
                onThemeChange = onThemeChange
            )
        }
    ) {
        ChatScreenContent(
            conversation = currentConversation,
            onSendMessage = ::saveState,
            onMenuClick = { coroutineScope.launch { drawerState.open() } },
            textToSpeech = textToSpeech
        )
    }
}

fun createNewConversation(): Conversation {
    return Conversation(
        title = "New Chat",
        messages = mutableListOf(Message(text = "Welcome! I am your AI Legal Assistant. How can I assist you today?", isFromUser = false))
    )
}

@Composable
fun DrawerContent(
    conversations: List<Conversation>, isDarkTheme: Boolean, onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit, onDeleteConversation: (String) -> Unit, onThemeChange: (Boolean) -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.85f)) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_launcher_foreground), "App Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(16.dp))
                Text("Chat History", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onNewChatClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Icon(Icons.Default.Add, "New Chat"); Spacer(Modifier.width(8.dp)); Text("Start New Chat")
            }
            Spacer(Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 24.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(conversations, key = { it.id }) { conv -> ConversationHistoryItem(conv, { onConversationClick(conv.id) }, { onDeleteConversation(conv.id) }) }
            }
            Divider(modifier = Modifier.padding(horizontal = 24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Theme", style = MaterialTheme.typography.bodyLarge)
                ThemeToggleButton(isDarkTheme, onThemeChange)
            }
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
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
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
    conversation: Conversation,
    onSendMessage: () -> Unit,
    onMenuClick: () -> Unit,
    textToSpeech: TextToSpeech?
) {
    val coroutineScope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val basePrompt = "I need legal advice..."

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
                    conversation.messages.add(Message(text = "Sorry, an error occurred: ${e.message}", isFromUser = false))
                } finally {
                    isLoading = false; onSendMessage(); listState.animateScrollToItem(conversation.messages.size)
                }
            }
            onSendMessage()
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
                        context.startActivity(Intent.createChooser(intent, "Share Conversation"))
                    }) { Icon(Icons.Default.Share, "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = conversation.messages, key = { it.id }, contentType = { if (it.isFromUser) "user" else "bot" }) { message ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start) {
                        ChatMessageBubble(message)
                        MessageActions(message, textToSpeech)
                    }
                }
                if (isLoading) {
                    item(contentType = "indicator") { TypingIndicator() }
                }
            }
            ChatInputBar(value = userInput, onValueChange = { userInput = it }, onSend = ::sendMessage)
        }
    }
}

@Composable
fun MessageActions(message: Message, tts: TextToSpeech?) {
    val context = LocalContext.current
    Row(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(modifier = Modifier.size(20.dp), onClick = {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("message", message.text))
        }) { Icon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }

        if (!message.isFromUser) {
            IconButton(modifier = Modifier.size(20.dp), onClick = { tts?.speak(message.text, TextToSpeech.QUEUE_FLUSH, null, null) }) {
                Icon(Icons.Default.VolumeUp, "Read Aloud", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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

    // --- KEY CHANGE: Conditional modifier for width ---
    val bubbleModifier = if (isUser) {
        Modifier.widthIn(max = 320.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Surface(
        color = bubbleColor,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (isUser) 20.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 20.dp),
        modifier = bubbleModifier,
        shadowElevation = 2.dp
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

// --- FULL, CORRECT MARKDOWN PARSER (Unchanged, included for completeness) ---
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

@Composable
private fun Blockquote(content: AnnotatedString, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).height(20.dp).background(color.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(12.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge.copy(color = color.copy(alpha = 0.8f), fontStyle = FontStyle.Italic))
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(code, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MarkdownTable(headers: List<AnnotatedString>, rows: List<List<AnnotatedString>>, textColor: Color) {
    Surface(border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f)), shape = RoundedCornerShape(8.dp), color = Color.Transparent) {
        Column {
            Row(Modifier.background(textColor.copy(alpha = 0.05f))) {
                headers.forEach { header -> Text(header, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = textColor), modifier = Modifier.weight(1f).padding(12.dp)) }
            }
            Divider(color = textColor.copy(alpha = 0.2f))
            rows.forEach { row -> Row { row.forEach { cell -> Text(cell, style = MaterialTheme.typography.bodyMedium.copy(color = textColor), modifier = Modifier.weight(1f).padding(12.dp)) } } }
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
            line.startsWith("```") -> {
                val codeBlockLines = lines.subList(i + 1, lines.size).takeWhile { it != "```" }; blocks.add(MarkdownBlock.CodeBlock(codeBlockLines.joinToString("\n"))); i += codeBlockLines.size + 2; continue
            }
            isTable(lines, i) -> {
                val tableLines = lines.subList(i, lines.size).takeWhile { it.trim().startsWith("|") || it.trim().endsWith("|") }
                val headers = tableLines.first().split("|").map { parseInlineMarkdown(it.trim(), codeColor) }.drop(1).dropLast(1)
                val rows = tableLines.drop(2).map { row -> row.split("|").map { parseInlineMarkdown(it.trim(), codeColor) }.drop(1).dropLast(1) }
                blocks.add(MarkdownBlock.Table(headers, rows)); i += tableLines.size; continue
            }
            line.startsWith("#") -> {
                val level = line.takeWhile { it == '#' }.count(); val text = line.removePrefix("#".repeat(level)).trim()
                blocks.add(MarkdownBlock.Header(level, parseInlineMarkdown(text, codeColor)))
            }
            line.startsWith(">") -> blocks.add(MarkdownBlock.Blockquote(parseInlineMarkdown(line.removePrefix(">").trim(), codeColor)))
            line.startsWith("* ") || line.startsWith("- ") -> {
                val prefix = if (line.startsWith("* ")) "* " else "- "; blocks.add(MarkdownBlock.ListItem(parseInlineMarkdown(line.removePrefix(prefix).trim(), codeColor), "•"))
            }
            line.matches(Regex("^\\d+\\.\\s.*")) -> {
                val bullet = line.substringBefore(". ") + "."; val content = line.substringAfter(". ").trim()
                blocks.add(MarkdownBlock.ListItem(parseInlineMarkdown(content, codeColor), bullet))
            }
            line.matches(Regex("^\\s*---*\\s*$")) -> blocks.add(MarkdownBlock.Divider)
            line.isNotBlank() -> blocks.add(MarkdownBlock.Paragraph(parseInlineMarkdown(line, codeColor)))
        }
        i++
    }
    return blocks
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

// --- RESTORED COMPOSABLES ---
@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
        val infiniteTransition = rememberInfiniteTransition("typing_dots")
        val dotScales = (0..2).map {
            infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(400, delayMillis = it * 150, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "dot_scale_$it")
        }
        Surface(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dotScales.forEach { Box(modifier = Modifier.size(8.dp).graphicsLayer { scaleX = it.value; scaleY = it.value }.clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))) }
            }
        }
    }
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(value = value, onValueChange = onValueChange, placeholder = { Text("Ask me anything...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val isEnabled = value.isNotBlank()
            val animatedColor by animateColorAsState(if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), label = "button color")
            val animatedScale by animateFloatAsState(if(isEnabled) 1f else 0.9f, label = "button scale")
            IconButton(onClick = onSend, enabled = isEnabled, modifier = Modifier.size(48.dp).graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }.clip(CircleShape).background(animatedColor)) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ChatScreenPreviewLight() {
    AILegalAssistantTheme(darkTheme = false) {
        LegalAssistantChatScreen(repository = ConversationRepository(LocalContext.current), isDarkTheme = false, onThemeChange = {}, textToSpeech = null)
    }
}


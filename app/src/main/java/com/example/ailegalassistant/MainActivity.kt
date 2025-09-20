package com.example.ailegalassistant

import android.content.Context
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    primary = Color(0xFF7B9BFF),
    onPrimary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFEAEAEA),
    onSurface = Color(0xFFEAEAEA),
    primaryContainer = Color(0xFF3A3A5A),
    onPrimaryContainer = Color(0xFFD0D0FF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006AFF),
    onPrimary = Color.White,
    background = Color(0xFFF4F6FC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001C5A)
)

@Composable
fun AILegalAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            labelMedium = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            )
        ),
        content = content
    )
}

// --- Local Storage: Conversation Repository ---

class ConversationRepository(context: Context) {
    private val prefs = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val historyKey = "conversation_history"

    fun saveConversations(conversations: List<Conversation>) {
        val json = gson.toJson(conversations)
        prefs.edit().putString(historyKey, json).apply()
    }

    fun loadConversations(): MutableList<Conversation> {
        val json = prefs.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Conversation>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}


// --- Main Activity ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AILegalAssistantTheme {
                val context = LocalContext.current
                val repository = remember { ConversationRepository(context) }
                LegalAssistantChatScreen(repository = repository)
            }
        }
    }
}

// --- Main Chat Screen with Drawer ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAssistantChatScreen(repository: ConversationRepository) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var conversations by remember { mutableStateOf(repository.loadConversations()) }
    var currentConversation by remember {
        mutableStateOf(
            conversations.firstOrNull() ?: createNewConversation()
        )
    }

    fun saveState() {
        val index = conversations.indexOfFirst { it.id == currentConversation.id }
        val updatedList = conversations.toMutableList()

        if (index != -1) {
            updatedList[index] = currentConversation.copy(timestamp = System.currentTimeMillis())
        } else {
            updatedList.add(0, currentConversation)
        }

        val sortedList = updatedList.sortedByDescending { it.timestamp }
        repository.saveConversations(sortedList)
        conversations = sortedList.toMutableList()
    }

    fun handleNewConversation() {
        val newConversation = createNewConversation()
        conversations = (listOf(newConversation) + conversations).distinctBy { it.id }.toMutableList()
        currentConversation = newConversation
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun switchConversation(conversationId: String) {
        conversations.find { it.id == conversationId }?.let {
            currentConversation = it
        }
        coroutineScope.launch {
            drawerState.close()
        }
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
                conversations = conversations,
                onConversationClick = { switchConversation(it) },
                onNewChatClick = { handleNewConversation() },
                onDeleteConversation = { deleteConversation(it) }
            )
        }
    ) {
        ChatScreenContent(
            conversation = currentConversation,
            onSendMessage = {
                saveState()
            },
            onMenuClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            }
        )
    }
}

fun createNewConversation(): Conversation {
    return Conversation(
        title = "New Chat",
        messages = mutableListOf(Message(text = "Welcome to your AI Legal Assistant! How can I help you today?", isFromUser = false))
    )
}

@Composable
fun DrawerContent(
    conversations: List<Conversation>,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Chat History", style = MaterialTheme.typography.titleLarge)
            }
            Divider()
            Button(
                onClick = onNewChatClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "New Chat")
                Spacer(Modifier.width(8.dp))
                Text("Start New Chat")
            }
            Divider()
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(conversations, key = { it.id }) { conversation ->
                    ConversationHistoryItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.id) },
                        onDelete = { onDeleteConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationHistoryItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = conversation.messages.lastOrNull()?.text ?: "No messages yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Conversation",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    conversation: Conversation,
    onSendMessage: () -> Unit,
    onMenuClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val basePrompt = """
    I need legal advice. Act as a legal assistant specializing in Indian law. Format your response using rich Markdown for clarity (headings, bold, italics, lists, tables, code blocks, and blockquotes). Do not provide a definitive legal opinion. Instead, explain the relevant legal principles, key points, and next steps. Include applicable Acts, Sections, and landmark judgments. This is for informational purposes only. My situation is:
    """.trimIndent()

    fun sendMessage() {
        if (userInput.isNotBlank()) {
            val userMessage = Message(text = userInput, isFromUser = true)
            conversation.messages.add(userMessage)

            if (conversation.messages.count { it.isFromUser } == 1) {
                conversation.title = userInput
            }

            val query = userInput
            userInput = ""
            isLoading = true

            coroutineScope.launch {
                listState.animateScrollToItem(conversation.messages.size - 1)
                try {
                    val fullPrompt = "$basePrompt $query"
                    val encodedPrompt = withContext(Dispatchers.IO) { URLEncoder.encode(fullPrompt, "UTF-8") }
                    val apiUrl = "https://text.pollinations.ai/$encodedPrompt"
                    val result = withContext(Dispatchers.IO) { URL(apiUrl).readText() }
                    conversation.messages.add(Message(text = result, isFromUser = false))
                } catch (e: Exception) {
                    e.printStackTrace()
                    conversation.messages.add(
                        Message(
                            text = "Sorry, an error occurred: ${e.message}",
                            isFromUser = false
                        )
                    )
                } finally {
                    isLoading = false
                    onSendMessage()
                    if (conversation.messages.isNotEmpty()) {
                        listState.animateScrollToItem(conversation.messages.size - 1)
                    }
                }
            }
            onSendMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(conversation.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp), // Screen edge padding
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(conversation.messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(400)
                        ),
                    ){
                        ChatMessageBubble(message)
                    }
                }
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            ChatInputBar(
                value = userInput,
                onValueChange = { userInput = it },
                onSend = { sendMessage() }
            )
        }
    }
}


@Composable
fun ChatMessageBubble(message: Message) {
    val isUser = message.isFromUser
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    val horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                // *** KEY CHANGE: AI bubble uses weight to fill width, user bubble has max width ***
                .then(if (isUser) Modifier.widthIn(max = 320.dp) else Modifier.weight(1f))
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(bubbleColor)
        ) {
            if (isUser) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                FormattedMarkdownText(
                    markdown = message.text,
                    textColor = textColor
                )
            }
        }
    }
}

// --- ENHANCED MARKDOWN PARSER ---

@Composable
fun FormattedMarkdownText(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val lines = markdown.lines()
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.startsWith("```") -> {
                    val codeBlockLines = lines.subList(i + 1, lines.size).takeWhile { it != "```" }
                    CodeBlock(codeBlockLines.joinToString("\n"))
                    i += codeBlockLines.size + 2
                }
                isTable(lines, i) -> {
                    val tableLines = lines.subList(i, lines.size).takeWhile { it.trim().startsWith("|") || it.trim().endsWith("|") }
                    MarkdownTable(tableLines, textColor)
                    i += tableLines.size
                }
                line.startsWith("#") -> {
                    Header(line, textColor)
                    i++
                }
                line.startsWith(">") -> {
                    Blockquote(line.removePrefix(">").trim(), textColor)
                    i++
                }
                line.startsWith("* ") || line.startsWith("- ") -> {
                    val prefix = if (line.startsWith("* ")) "* " else "- "
                    ListItem(line.removePrefix(prefix).trim(), textColor)
                    i++
                }
                line.matches(Regex("^\\d+\\.\\s.*")) -> {
                    ListItem(line, textColor, isOrdered = true)
                    i++
                }
                line.matches(Regex("^\\s*---*\\s*$")) -> {
                    Divider(color = textColor.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    i++
                }
                line.isNotBlank() -> {
                    Paragraph(line, textColor)
                    i++
                }
                else -> { i++ }
            }
        }
    }
}

@Composable
private fun Header(line: String, color: Color) {
    val level = line.takeWhile { it == '#' }.count()
    val text = line.removePrefix("#".repeat(level)).trim()
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineSmall
        2 -> MaterialTheme.typography.titleLarge
        3 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    Text(parseInlineMarkdown(text, color.copy(alpha = 0.1f)), style = style.copy(color = color))
}

@Composable
private fun Paragraph(text: String, color: Color) {
    Text(parseInlineMarkdown(text, color.copy(alpha = 0.1f)), style = MaterialTheme.typography.bodyLarge.copy(color = color))
}

@Composable
private fun ListItem(text: String, color: Color, isOrdered: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val bullet = if (isOrdered) text.substringBefore(". ") + "." else "•"
        val content = if (isOrdered) text.substringAfter(". ").trim() else text

        Text(bullet, style = MaterialTheme.typography.bodyLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(parseInlineMarkdown(content, color.copy(alpha = 0.1f)), style = MaterialTheme.typography.bodyLarge.copy(color = color))
    }
}


@Composable
private fun Blockquote(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .align(Alignment.CenterVertically)
                .fillMaxHeight()
                .background(color.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            parseInlineMarkdown(text, color.copy(alpha = 0.1f)),
            style = MaterialTheme.typography.bodyLarge.copy(color = color.copy(alpha = 0.8f), fontStyle = FontStyle.Italic)
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun isTable(lines: List<String>, currentIndex: Int): Boolean {
    if (currentIndex + 1 >= lines.size) return false
    val header = lines[currentIndex].trim()
    val separator = lines[currentIndex + 1].trim()
    return header.count{ it == '|' } > 1 && separator.count{ it == '|' } > 1 && separator.matches(Regex("^[|\\s:-]+$"))
}

@Composable
private fun MarkdownTable(lines: List<String>, textColor: Color) {
    val headerCells = lines.first().split("|").map { it.trim() }.drop(1).dropLast(1)
    val rows = lines.drop(2).map { row -> row.split("|").map { it.trim() }.drop(1).dropLast(1) }
    val codeColor = textColor.copy(alpha = 0.1f)

    Surface(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Column {
            // Header Row
            Row(Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))) {
                headerCells.forEach { cell ->
                    TableCell(text = cell, color = textColor, codeColor = codeColor, isHeader = true, modifier = Modifier.weight(1f))
                }
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            // Data Rows
            rows.forEach { row ->
                Row {
                    row.forEach { cell ->
                        TableCell(text = cell, color = textColor, codeColor = codeColor, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(text: String, color: Color, codeColor: Color, modifier: Modifier = Modifier, isHeader: Boolean = false) {
    Text(
        text = parseInlineMarkdown(text, codeColor),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = color
        ),
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
    )
}

// Robust inline parser for bold, italic, and code
private fun parseInlineMarkdown(text: String, codeColor: Color): AnnotatedString {
    val pattern = Pattern.compile("(\\*\\*(.*?)\\*\\*)|(\\*(.*?)\\*)|(`(.*?)`)")
    val matcher = pattern.matcher(text)

    return buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            val startIndex = matcher.start()
            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }

            val boldText = matcher.group(2)
            val italicText = matcher.group(4)
            val codeText = matcher.group(6)

            when {
                boldText != null -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(boldText) }
                italicText != null -> withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) { append(italicText) }
                codeText != null -> withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, background = codeColor, fontWeight = FontWeight.Medium)) { append(codeText) }
            }
            lastIndex = matcher.end()
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val infiniteTransition = rememberInfiniteTransition("typing_dots")
        val dotOffsets = (0..2).map {
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = it * 120, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "dot_offset_$it"
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dotOffsets.forEach {
                    Box(
                        modifier = Modifier
                            .offset(y = it.value.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Ask me anything...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (value.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ChatScreenPreviewLight() {
    AILegalAssistantTheme(darkTheme = false) {
        val context = LocalContext.current
        val repository = remember { ConversationRepository(context) }
        LegalAssistantChatScreen(repository)
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun ChatScreenPreviewDark() {
    AILegalAssistantTheme(darkTheme = true) {
        val context = LocalContext.current
        val repository = remember { ConversationRepository(context) }
        LegalAssistantChatScreen(repository)
    }
}


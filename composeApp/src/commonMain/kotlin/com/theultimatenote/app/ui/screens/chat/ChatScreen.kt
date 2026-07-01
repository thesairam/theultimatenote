package com.theultimatenote.app.ui.screens.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.theultimatenote.app.data.model.ChatAction
import com.theultimatenote.app.data.model.ChatMessage
import com.theultimatenote.app.ui.components.UpgradeDialog
import com.theultimatenote.app.ui.screens.subscription.SubscriptionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit = {},
) {
    val viewModel: ChatViewModel = koinViewModel()
    val subscriptionViewModel: SubscriptionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var enterSends by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear chat",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Box(modifier = Modifier.padding(top = 8.dp)) }
                items(uiState.messages, key = { it.id.ifBlank { it.timestamp.toString() } }) { message ->
                    ChatBubble(
                        message = message,
                        onExecuteAction = { index, action ->
                            viewModel.executeAction(message.id, index, action)
                        },
                    )
                }
                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
                item { Box(modifier = Modifier.padding(bottom = 8.dp)) }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = { enterSends = !enterSends },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = if (enterSends) "Enter sends message" else "Enter adds newline",
                        tint = if (enterSends) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (enterSends) "Enter to send..."
                            else "Shift+Enter to send...",
                        )
                    },
                    modifier = Modifier.weight(1f).onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            if (enterSends) {
                                if (inputText.isNotBlank() && !uiState.isLoading) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                    maxLines = 6,
                    shape = RoundedCornerShape(24.dp),
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !uiState.isLoading) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.isLoading,
                ) {
                    val sendTint by animateColorAsState(
                        targetValue = if (inputText.isNotBlank() && !uiState.isLoading)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(100),
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = sendTint,
                    )
                }
            }
        }
    }

    uiState.limitReached?.let { reason ->
        UpgradeDialog(
            reason = reason,
            onUpgrade = { subscriptionViewModel.launchUpgradeFlow(); viewModel.dismissLimit() },
            onDismiss = { viewModel.dismissLimit() },
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onExecuteAction: (Int, ChatAction) -> Unit = { _, _ -> },
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp,
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
            )
        }

        if (message.actions.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                message.actions.forEachIndexed { index, action ->
                    ActionCard(
                        action = action,
                        onExecute = { onExecuteAction(index, action) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    action: ChatAction,
    onExecute: () -> Unit,
) {
    val label = when (action.type) {
        "create_task" -> "Create task: ${action.title}"
        "create_project" -> "Create project: ${action.projectName}"
        else -> "Action: ${action.type}"
    }
    val subtitle = when (action.type) {
        "create_task" -> {
            val parts = mutableListOf("in ${action.projectName}")
            if (action.isRecurring) parts.add("recurring")
            if (action.scheduledTime != null) parts.add("at ${action.scheduledTime}")
            parts.joinToString(" · ")
        }
        else -> null
    }

    Card(
        modifier = Modifier.widthIn(max = 300.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (action.executed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (!action.executed) BorderStroke(0.75.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
            else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (action.executed) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (action.executed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (action.executed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onTertiaryContainer,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!action.executed) {
                TextButton(onClick = onExecute) {
                    Text("Create", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

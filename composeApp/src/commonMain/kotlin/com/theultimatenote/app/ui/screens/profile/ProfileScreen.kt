package com.theultimatenote.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profile saved!")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val user = uiState.user
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Personal Info", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = user.displayName,
                    onValueChange = { viewModel.updateUser(user.copy(displayName = it)) },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = user.email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = false,
                )

                OutlinedTextField(
                    value = user.bio,
                    onValueChange = { viewModel.updateUser(user.copy(bio = it)) },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Goals & Focus", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = user.currentLearningFocus,
                    onValueChange = { viewModel.updateUser(user.copy(currentLearningFocus = it)) },
                    label = { Text("Current Learning Focus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                ChipListEditor(
                    label = "Goals",
                    items = user.goals,
                    onUpdate = { viewModel.updateUser(user.copy(goals = it)) },
                )

                ChipListEditor(
                    label = "Areas of Focus",
                    items = user.areasOfFocus,
                    onUpdate = { viewModel.updateUser(user.copy(areasOfFocus = it)) },
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("About You", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                ChipListEditor(
                    label = "Hobbies",
                    items = user.hobbies,
                    onUpdate = { viewModel.updateUser(user.copy(hobbies = it)) },
                )

                ChipListEditor(
                    label = "Skills",
                    items = user.skills,
                    onUpdate = { viewModel.updateUser(user.copy(skills = it)) },
                )

                ChipListEditor(
                    label = "Inspirational People",
                    items = user.idols,
                    onUpdate = { viewModel.updateUser(user.copy(idols = it)) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Save Profile")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipListEditor(
    label: String,
    items: List<String>,
    onUpdate: (List<String>) -> Unit,
) {
    var newItem by remember { mutableStateOf("") }

    Column {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(item) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onUpdate(items - item) },
                            modifier = Modifier.size(18.dp),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                label = { Text("Add $label") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(
                onClick = {
                    if (newItem.isNotBlank()) {
                        onUpdate(items + newItem.trim())
                        newItem = ""
                    }
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

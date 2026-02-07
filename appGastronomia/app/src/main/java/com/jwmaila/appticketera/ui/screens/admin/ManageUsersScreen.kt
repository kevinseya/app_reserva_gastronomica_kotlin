package com.jwmaila.appticketera.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jwmaila.appticketera.data.model.CreateUserRequest
import com.jwmaila.appticketera.data.model.UpdateUserRequest
import com.jwmaila.appticketera.data.model.User
import com.jwmaila.appticketera.data.model.UserRole
import com.jwmaila.appticketera.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageUsersViewModel = hiltViewModel()
) {
    val usersState by viewModel.usersState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editUser by remember { mutableStateOf<User?>(null) }
    var deleteUser by remember { mutableStateOf<User?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Usuarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediumBlue,
                    titleContentColor = White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear usuario")
            }
        }
    ) { paddingValues ->
        when (val state = usersState) {
            is UsersState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediumBlue)
                }
            }
            
            is UsersState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PastelBlue)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = DarkBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Total de usuarios: ${state.users.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                            }
                        }
                    }
                    
                    items(state.users) { user ->
                        UserCard(
                            name = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            role = user.role.name,
                            onEdit = { editUser = user },
                            onDelete = { deleteUser = user }
                        )
                    }
                }
            }
            
            is UsersState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadUsers() },
                            colors = ButtonDefaults.buttonColors(containerColor = MediumBlue)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        UserDialog(
            title = "Crear usuario",
            initial = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { email, password, firstName, lastName, role ->
                viewModel.createUser(
                    CreateUserRequest(email, password, firstName, lastName, role)
                ) { showCreateDialog = false }
            }
        )
    }

    editUser?.let { user ->
        UserDialog(
            title = "Editar usuario",
            initial = user,
            onDismiss = { editUser = null },
            onConfirm = { email, password, firstName, lastName, role ->
                viewModel.updateUser(
                    user.id,
                    UpdateUserRequest(email, firstName, lastName, role)
                ) { editUser = null }
            },
            showPassword = false
        )
    }

    deleteUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteUser = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar a ${user.firstName} ${user.lastName}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteUser(user.id) { deleteUser = null }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteUser = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun UserCard(
    name: String,
    email: String,
    role: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (role == "ADMIN") PastelBlue else VeryLightBlue,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = DarkBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBlue
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray
                )
            }
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (role == "ADMIN") MediumBlue else VeryLightBlue
            ) {
                Text(
                    text = if (role == "ADMIN") "Admin" else "Cliente",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (role == "ADMIN") White else DarkBlue
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDialog(
    title: String,
    initial: User?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, UserRole) -> Unit,
    showPassword: Boolean = true
) {
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf(initial?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initial?.lastName ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: UserRole.CLIENT) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") })
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                if (showPassword) {
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
                }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        UserRole.values().forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(email, password, firstName, lastName, role) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

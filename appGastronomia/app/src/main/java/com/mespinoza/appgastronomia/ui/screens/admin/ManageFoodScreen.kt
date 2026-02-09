package com.mespinoza.appgastronomia.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mespinoza.appgastronomia.utils.ImageUtils
import com.mespinoza.appgastronomia.data.model.FoodCategory
import com.mespinoza.appgastronomia.data.model.FoodItem
import com.mespinoza.appgastronomia.ui.theme.*
import com.mespinoza.appgastronomia.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageFoodViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf<String?>(null) }
    var itemToEdit by remember { mutableStateOf<FoodItem?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Menús") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediumBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MediumBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva Categoría")
            }
        }
    ) { padding ->
        if (isLoading && categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MediumBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onAddItem = { showAddItemDialog = category.id },
                        onEditItem = { itemToEdit = it },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onConfirm = { name ->
                    viewModel.createCategory(name)
                    showAddCategoryDialog = false
                }
            )
        }

        showAddItemDialog?.let { catId ->
            AddItemDialog(
                onDismiss = { showAddItemDialog = null },
                onConfirm = { name, desc, price, uri ->
                    viewModel.createItem(catId, name, desc, price, uri)
                    showAddItemDialog = null
                }
            )
        }

        itemToEdit?.let { item ->
            AddItemDialog(
                initialItem = item,
                onDismiss = { itemToEdit = null },
                onConfirm = { name, desc, price, uri ->
                    viewModel.updateItem(item.id, null, name, desc, price, uri)
                    itemToEdit = null
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: FoodCategory,
    onAddItem: () -> Unit,
    onEditItem: (FoodItem) -> Unit,
    onDeleteItem: (FoodItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                IconButton(onClick = onAddItem) {
                    Icon(Icons.Default.AddCircle, "Agregar plato", tint = MediumBlue)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            if (category.items.isEmpty()) {
                Text(
                    "No hay platos en esta categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                category.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mostrar imagen si existe
                            item.imageUrl?.let { url ->
                                AsyncImage(
                                    model = ImageUtils.getFullImageUrl(url),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).padding(end = 8.dp)
                                )
                            }
                            Column {
                                Text(item.name, fontWeight = FontWeight.SemiBold, color = DarkBlue)
                                item.description?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    formatPrice(item.price),
                                    color = MediumBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { onEditItem(item) }) {
                                Icon(Icons.Default.Edit, "Editar", tint = MediumBlue)
                            }
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = ErrorRed)
                            }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej: Bebidas)") }
            )
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(name) }) {
                Text("Crear")
            }
        },        
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddItemDialog(
    initialItem: FoodItem? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Double, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var price by remember { mutableStateOf(initialItem?.price?.toString() ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Nuevo Plato" else "Editar Plato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri != null) "Imagen seleccionada" else "Seleccionar Imagen")
                }
                
                // Mostrar imagen existente o nueva seleccionada
                if (imageUri == null && initialItem?.imageUrl != null) {
                    AsyncImage(
                        model = ImageUtils.getFullImageUrl(initialItem.imageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val p = price.toDoubleOrNull()
                if(name.isNotBlank() && p != null) onConfirm(name, description, p, imageUri) 
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
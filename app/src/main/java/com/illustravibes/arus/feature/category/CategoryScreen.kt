package com.illustravibes.arus.feature.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor

@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var categoryToArchive by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = "Kelola Kategori",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = ArusBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (uiState.isLoading) {
            ArusLoadingState()
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Type Segmented Control (Pemasukan / Pengeluaran — sama seperti layar Catat Transaksi)
            ArusSegmentedControl(
                options = listOf("Pemasukan", "Pengeluaran"),
                selectedIndex = if (uiState.selectedTypeTab == "INCOME") 0 else 1,
                onOptionSelected = { index ->
                    viewModel.onTypeTabChange(if (index == 0) "INCOME" else "EXPENSE")
                }
            )

            ArusPrimaryButton(
                text = "+ Tambah Kategori ${if (uiState.selectedTypeTab == "EXPENSE") "Pengeluaran" else "Pemasukan"}",
                onClick = { viewModel.openCreateDialog() }
            )

            ArusSectionHeader(title = "Daftar Kategori (${uiState.categories.size})")

            if (uiState.categories.isEmpty()) {
                ArusEmptyState(
                    title = "Belum ada kategori ${if (uiState.selectedTypeTab == "EXPENSE") "pengeluaran" else "pemasukan"}",
                    description = "Tambah kategori pertama Anda dengan tombol di atas."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.categories.forEach { category ->
                    val defaultTint = if (category.type == "EXPENSE") NotionCoral else NotionMint
                    val iconTint = parseHexColor(category.colorHex, defaultTint)
                    val tileColor = iconTint.copy(alpha = 0.15f)

                    ArusCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ArusCategoryTile(
                                    iconResId = mapDrawableIcon(category.icon),
                                    tileColor = tileColor,
                                    iconTint = iconTint,
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category.name,
                                    style = ArusTypography.titleMedium,
                                    color = ArusTextPrimary
                                )
                            }

                            ArusActionPill(
                                text = "Arsipkan",
                                onClick = { categoryToArchive = category },
                                style = ArusActionPillStyle.DESTRUCTIVE
                            )
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Archive Confirmation Sheet
        if (categoryToArchive != null) {
            val targetCategory = categoryToArchive!!
            ArusConfirmationSheet(
                title = "Arsipkan Kategori Ini?",
                message = "Kategori \"${targetCategory.name}\" tidak akan muncul lagi di pemilih kategori transaksi baru.",
                confirmButtonText = "Ya, Arsipkan",
                onConfirm = {
                    viewModel.archiveCategory(targetCategory)
                    categoryToArchive = null
                },
                onDismiss = { categoryToArchive = null },
                isDestructive = true
            )
        }

        // Create Category Modal Sheet
        if (uiState.showDialog) {
            ArusFormSheet(
                title = "Tambah Kategori ${if (uiState.selectedTypeTab == "EXPENSE") "Pengeluaran" else "Pemasukan"}",
                onDismiss = { viewModel.closeDialog() }
            ) {

                    ArusTextField(
                        value = uiState.nameInput,
                        onValueChange = { name -> viewModel.onNameChange(name) },
                        label = "Nama Kategori",
                        placeholder = "mis. Kopi & Camilan, Tagihan Listrik",
                        errorMessage = uiState.errorMessage
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ArusPrimaryButton(
                        text = "Simpan Kategori",
                        onClick = { viewModel.saveCategory() }
                    )
            }
        }
    }
}

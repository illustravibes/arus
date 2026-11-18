package com.illustravibes.arus.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Type Segmented Control (Pengeluaran / Pemasukan)
            ArusSegmentedControl(
                options = listOf("Pengeluaran", "Pemasukan"),
                selectedIndex = if (uiState.selectedTypeTab == "EXPENSE") 0 else 1,
                onOptionSelected = { index ->
                    viewModel.onTypeTabChange(if (index == 0) "EXPENSE" else "INCOME")
                }
            )

            ArusPrimaryButton(
                text = "+ Tambah Kategori ${if (uiState.selectedTypeTab == "EXPENSE") "Pengeluaran" else "Pemasukan"}",
                onClick = { viewModel.openCreateDialog() }
            )

            ArusSectionHeader(title = "Daftar Kategori (${uiState.categories.size})")

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
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category.name,
                                    style = ArusTypography.titleMedium,
                                    color = ArusTextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(ArusExpenseSoft)
                                    .clickable { categoryToArchive = category }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Arsipkan",
                                    style = ArusTypography.labelSmall,
                                    color = ArusExpense
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
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
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeDialog() },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = ArusSurface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(PillShape)
                            .background(ArusBorder)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tambah Kategori ${if (uiState.selectedTypeTab == "EXPENSE") "Pengeluaran" else "Pemasukan"}",
                        style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ArusTextPrimary
                    )

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
}

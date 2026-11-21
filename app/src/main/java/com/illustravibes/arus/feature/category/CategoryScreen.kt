package com.illustravibes.arus.feature.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.R
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
    var menuCategory by remember { mutableStateOf<CategoryEntity?>(null) }

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
            // V2-P2-3: skeleton, bukan spinner fullscreen.
            ArusSkeletonLoading(showHero = false, modifier = Modifier.padding(paddingValues))
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
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
                    description = "Tambah kategori pertama Anda dengan tombol di atas.",
                    iconResId = R.drawable.fa_tags
                )
            } else {
                ArusCard {
                    Column {
                        uiState.categories.forEachIndexed { index, category ->
                            if (index > 0) {
                                HorizontalDivider(color = ArusBorder, thickness = 1.dp)
                            }
                            val defaultTint = if (category.type == "EXPENSE") NotionCoral else NotionMint
                            val iconTint = parseHexColor(category.colorHex, defaultTint)
                            val tileColor = iconTint.copy(alpha = 0.15f)

                            // Baris kategori: ketuk barisnya untuk buka menu
                            // Arsipkan/Pulihkan (tombol ••• dihapus, sama
                            // seperti Kelola Dompet — keputusan user
                            // 2026-11-21).
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuCategory = category }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
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
                                    style = ArusTypography.titleLarge,
                                    color = ArusTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                        }
                    }
                }
            }
            }

            // V2-P1-5: kategori terarsip bisa dipulihkan dari sini.
            if (uiState.archivedCategories.isNotEmpty()) {
                ArusSectionHeader(title = "Kategori Terarsip (${uiState.archivedCategories.size})")
                ArusCard {
                    Column {
                        uiState.archivedCategories.forEachIndexed { index, category ->
                            if (index > 0) {
                                HorizontalDivider(color = ArusBorder, thickness = 1.dp)
                            }
                            val defaultTint = if (category.type == "EXPENSE") NotionCoral else NotionMint
                            val iconTint = parseHexColor(category.colorHex, defaultTint)
                            val tileColor = iconTint.copy(alpha = 0.15f)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuCategory = category }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
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
                                    style = ArusTypography.titleLarge,
                                    color = ArusTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Action Sheet (bottom sheet ala Notion) untuk satu kategori.
        if (menuCategory != null) {
            val targetCategory = menuCategory!!
            val isArchived = uiState.archivedCategories.contains(targetCategory)
            ArusActionSheet(
                title = targetCategory.name,
                items = if (isArchived) {
                    listOf(
                        ArusActionSheetItem(
                            label = "Pulihkan",
                            iconResId = R.drawable.fa_arrow_rotate_left,
                            onClick = { viewModel.unarchiveCategory(targetCategory) }
                        )
                    )
                } else {
                    listOf(
                        ArusActionSheetItem(
                            label = "Arsipkan",
                            iconResId = R.drawable.fa_box_archive,
                            onClick = { categoryToArchive = targetCategory }
                        )
                    )
                },
                onDismissRequest = { menuCategory = null }
            )
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
                isDestructive = false
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

                    Text(
                        text = "Ikon",
                        style = ArusTypography.labelLarge,
                        color = ArusTextPrimary
                    )
                    ArusIconPicker(
                        selectedIcon = uiState.selectedIcon,
                        onIconChange = { icon -> viewModel.onIconChange(icon) },
                        selectedColorHex = uiState.selectedColorHex
                    )

                    Text(
                        text = "Warna",
                        style = ArusTypography.labelLarge,
                        color = ArusTextPrimary
                    )
                    ArusColorPicker(
                        selectedColorHex = uiState.selectedColorHex,
                        onColorChange = { hex -> viewModel.onColorChange(hex) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ArusPrimaryButton(
                        text = "Simpan Kategori",
                        onClick = { viewModel.saveCategory() },
                        // V2-P1-6: Simpan nonaktif sampai nama terisi (pola layar Catat).
                        enabled = uiState.nameInput.isNotBlank()
                    )
            }
        }
    }
}

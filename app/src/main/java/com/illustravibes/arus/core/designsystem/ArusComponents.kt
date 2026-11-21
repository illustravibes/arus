package com.illustravibes.arus.core.designsystem

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.illustravibes.arus.R
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ============================================================================
// 1. CURRENCY FORMATTER UTILITY
// ============================================================================

fun Long.formatRupiah(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    val formatted = formatter.format(this)
    // Converts "Rp1.000.000,00" or "IDR 1.000.000" to clean "Rp1.000.000" or "Rp 1.000.000"
    return formatted.replace("IDR", "Rp").replace(",00", "").trim()
}

fun formatInputRupiah(input: String): String {
    // P2-12: batasi 15 digit (maks ~Rp999 triliun) — di atas 19 digit
    // toLongOrNull() gagal dan tampilan tiba-tiba reset ke 0.
    val digits = input.filter { it.isDigit() }.take(15)
    if (digits.isEmpty()) return ""
    val amount = digits.toLongOrNull() ?: 0L
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return formatter.format(amount)
}

// ============================================================================
// 2. ARUS CARD (Bevel White Rounded Card)
// ============================================================================

@Composable
fun ArusCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = ArusSurface,
    borderColor: Color = ArusBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(12.dp)

    Column(
        modifier = cardModifier,
        content = content
    )
}

// ============================================================================
// 3. ARUS BUTTONS (Pill CTA & Secondary Buttons)
// ============================================================================

@Composable
fun ArusPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArusBrandPrimary,
            contentColor = Color.White,
            disabledContainerColor = ArusBorder,
            disabledContentColor = ArusTextSecondary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = ArusTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ArusSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconResId: Int? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        contentPadding = contentPadding,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD3D9E4))
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ArusSurface,
            contentColor = ArusTextPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = ArusTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = ArusTypography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ArusTextPrimary
                )
            )
        }
    }
}

@Composable
fun ArusDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconResId: Int? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArusExpense,
            contentColor = Color.White,
            disabledContainerColor = ArusBorder,
            disabledContentColor = ArusTextSecondary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = ArusTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

// ============================================================================
// 4. BEVEL FORM INPUTS (Hero Amount Input & Property Selector Row)
// ============================================================================

@Composable
fun ArusAmountHeroInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "NOMINAL TRANSAKSI",
    placeholder: String = "0",
    errorMessage: String? = null,
    amountColor: Color = ArusTextPrimary,
    readOnly: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
) {
    val formattedDisplayValue = androidx.compose.runtime.remember(value) {
        formatInputRupiah(value)
    }

    // P2-12: state tidak lagi dibuat ulang tiap `value` berubah (itu yang
    // selalu melempar kursor ke akhir). Perubahan dari luar (keypad) tetap
    // disinkronkan lewat LaunchedEffect di bawah, dengan kursor di akhir.
    var textFieldValueState by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            TextFieldValue(
                text = formattedDisplayValue,
                selection = TextRange(formattedDisplayValue.length)
            )
        )
    }
    androidx.compose.runtime.LaunchedEffect(formattedDisplayValue) {
        if (textFieldValueState.text != formattedDisplayValue) {
            textFieldValueState = TextFieldValue(
                text = formattedDisplayValue,
                selection = TextRange(formattedDisplayValue.length)
            )
        }
    }

    ArusCard(
        modifier = modifier,
        cornerRadius = 16.dp
    ) {
        Text(
            text = label.uppercase(),
            style = ArusTypography.labelMedium,
            color = ArusTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rp ",
                style = ArusTypography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )
            BasicTextField(
                value = textFieldValueState,
                onValueChange = { newTv ->
                    // P2-12: jaga posisi kursor relatif terhadap digit yang
                    // diedit — hitung digit sebelum kursor di teks mentah,
                    // lalu letakkan kursor setelah digit yang sama di teks
                    // berformat ribuan.
                    val cleanDigits = newTv.text.filter { it.isDigit() }.take(15)
                    val formatted = formatInputRupiah(cleanDigits)
                    val cursor = newTv.selection.start.coerceIn(0, newTv.text.length)
                    val digitsBeforeCursor = newTv.text.take(cursor).count { it.isDigit() }
                    var newCursor = formatted.length
                    if (digitsBeforeCursor == 0) {
                        newCursor = 0
                    } else {
                        var seen = 0
                        for (i in formatted.indices) {
                            if (formatted[i].isDigit()) {
                                seen++
                                if (seen == digitsBeforeCursor) {
                                    newCursor = i + 1
                                    break
                                }
                            }
                        }
                    }
                    textFieldValueState = TextFieldValue(text = formatted, selection = TextRange(newCursor))
                    onValueChange(cleanDigits)
                },
                modifier = Modifier.fillMaxWidth().onFocusChanged { onFocusChange(it.isFocused) },
                textStyle = ArusTypography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                readOnly = readOnly,
                decorationBox = { innerTextField ->
                    Box {
                        if (formattedDisplayValue.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = ArusTypography.displayLarge.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArusTextSecondary.copy(alpha = 0.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = ArusTypography.labelSmall,
                color = ArusExpense
            )
        }
    }
}

@Composable
fun ArusPropertySelectorRow(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    tileColor: Color = NotionSkySoft,
    iconTint: Color = NotionSky,
    placeholder: String = "Pilih",
    logoResId: Int? = null,
    inCard: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (inCard) Modifier
                else Modifier
                    .clip(PillShape)
                    .background(ArusSurface)
                    .border(1.dp, ArusBorder, PillShape)
            )
            .clickable { onClick() }
            .padding(horizontal = if (inCard) 0.dp else 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (iconResId != null || logoResId != null) {
                ArusWalletTile(
                    logoResId = logoResId,
                    iconResId = iconResId ?: R.drawable.fa_wallet,
                    tileColor = tileColor,
                    iconTint = iconTint,
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = label,
                style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ArusTextPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedValue.ifEmpty { placeholder },
                style = ArusTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (selectedValue.isNotEmpty()) ArusTextPrimary else ArusTextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.fa_chevron_right),
                contentDescription = null,
                tint = ArusTextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isCurrency: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    showClearButton: Boolean = false
) {
    val displayValue = if (isCurrency) {
        androidx.compose.runtime.remember(value) { formatInputRupiah(value) }
    } else value

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = ArusTypography.labelLarge,
            color = ArusTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
                if (isCurrency) {
                    val cleanDigits = newValue.filter { it.isDigit() }
                    onValueChange(cleanDigits)
                } else {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary
                )
            },
            prefix = if (isCurrency) {
                {
                    Text(
                        text = "Rp ",
                        style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ArusTextPrimary
                    )
                }
            } else null,
            textStyle = if (isCurrency) {
                ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ArusTextPrimary)
            } else {
                ArusTypography.bodyLarge.copy(color = ArusTextPrimary)
            },
            keyboardOptions = if (isCurrency) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else KeyboardOptions.Default,
            singleLine = singleLine,
            trailingIcon = if (showClearButton && value.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Hapus pencarian",
                            tint = ArusTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            isError = errorMessage != null,
            shape = PillShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ArusSurface,
                unfocusedContainerColor = ArusSurface,
                errorContainerColor = ArusSurface,
                focusedBorderColor = ArusBrandPrimary,
                unfocusedBorderColor = ArusBorder,
                errorBorderColor = ArusExpense
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = ArusTypography.labelSmall,
                color = ArusExpense,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// ============================================================================
// 5. ARUS CATEGORY TILE (Soft Tile with Notion Accent Colors)
// ============================================================================

@Composable
fun ArusCategoryTile(
    iconResId: Int,
    tileColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = ArusTextPrimary,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(SoftTileShape)
            .background(tileColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// Logo dompet (idn-finlogos, PNG @3x): bila ada logo, tampilkan gambarnya
// di tile putih berbingkai; bila tidak, fallback ke ikon fa + warna.
@Composable
fun ArusWalletTile(
    logoResId: Int?,
    iconResId: Int,
    tileColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = ArusTextPrimary,
    size: Dp = 44.dp
) {
    if (logoResId != null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(SoftTileShape)
                .background(ArusSurface)
                .border(1.dp, ArusBorder, SoftTileShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size * 0.72f)
            )
        }
    } else {
        ArusCategoryTile(
            iconResId = iconResId,
            tileColor = tileColor,
            modifier = modifier,
            iconTint = iconTint,
            size = size
        )
    }
}

// ============================================================================
// 6. ARUS CHIP (Filter & Selector Pill Chip)
// ============================================================================

@Composable
fun ArusChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ArusBrandPrimary
) {
    val backgroundColor = if (selected) accentColor else ArusSurface
    val textColor = if (selected) Color.White else ArusTextPrimary
    val borderColor = if (selected) accentColor else ArusBorder

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(PillShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, PillShape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ArusTypography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        )
    }
}

// ============================================================================
// 7. ARUS STAT CARD (Financial Summary Card)
// ============================================================================

@Composable
fun ArusStatCard(
    title: String,
    amount: Long,
    modifier: Modifier = Modifier,
    isIncome: Boolean? = null,
    subtitle: String? = null,
    showEyeButton: Boolean = false,
    isBalanceHidden: Boolean = false,
    onToggleHideBalance: (() -> Unit)? = null,
    // V2-P1-4: true untuk kartu setengah lebar (bulanan/laporan).
    compactAmount: Boolean = false
) {
    val accentColor = when (isIncome) {
        true -> ArusIncome
        false -> ArusExpense
        null -> ArusTextPrimary
    }

    // P2-7: angka bergerak halus ke nilai baru saat saldo berubah
    // (mis. setelah simpan/hapus); diam bila kurangi-animasi aktif.
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    // P2-7 (revisi): animasi lewat progres Float + interpolasi Long, bukan
    // animateLongAsState (tidak ada di Compose 1.12). Nilai akhir selalu
    // tepat sama dengan `amount`, berapa pun besarnya.
    var startAmount by remember { mutableStateOf(amount) }
    val progress = remember { Animatable(1f) }
    val animatedAmount = startAmount + ((amount - startAmount) * progress.value).toLong()
    LaunchedEffect(amount) {
        if (startAmount != amount) {
            val from = startAmount + ((amount - startAmount) * progress.value).toLong()
            startAmount = from
            progress.snapTo(0f)
            if (animationsEnabled) progress.animateTo(1f, tween(300)) else progress.snapTo(1f)
        }
    }
    val displayAmount = if (isBalanceHidden) "Rp ••••••••" else animatedAmount.formatRupiah()

    // V2-P1-4: nominal sangat panjang mengecil otomatis supaya tidak
    // terpotong "…" di kartu setengah lebar (compact). Kartu lebar penuh
    // (hero/total) tetap memakai displayMedium 24sp.
    val amountStyle = if (!compactAmount) {
        ArusTypography.displayMedium
    } else when {
        displayAmount.length > 18 -> ArusTypography.displayMedium.copy(fontSize = 13.sp, lineHeight = 17.sp)
        displayAmount.length > 15 -> ArusTypography.displayMedium.copy(fontSize = 15.sp, lineHeight = 20.sp)
        displayAmount.length > 12 -> ArusTypography.displayMedium.copy(fontSize = 18.sp, lineHeight = 23.sp)
        else -> ArusTypography.displayMedium
    }

    ArusCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = ArusTypography.labelMedium,
                color = ArusTextSecondary
            )
            if (showEyeButton && onToggleHideBalance != null) {
                // P1-1: area sentuh 48dp, lingkaran visual tetap 32dp.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onToggleHideBalance() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ArusBrandSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isBalanceHidden) R.drawable.fa_eye_slash else R.drawable.fa_eye),
                            contentDescription = if (isBalanceHidden) "Tampilkan Saldo" else "Sembunyikan Saldo",
                            tint = ArusBrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayAmount,
            style = amountStyle,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = ArusTypography.labelSmall,
                color = ArusTextSecondary
            )
        }
    }
}

// ============================================================================
// 8. ARUS WALLET CARD (Bevel Individual Wallet Card)
// ============================================================================

@Composable
fun ArusWalletCard(
    walletName: String,
    balance: Long,
    modifier: Modifier = Modifier,
    iconResId: Int = R.drawable.fa_wallet,
    tileColor: Color = NotionSkySoft,
    iconTint: Color = NotionSky,
    onClick: (() -> Unit)? = null,
    logoResId: Int? = null
) {
    // V2-P1-4: saldo sangat panjang mengecil otomatis (kartu dompet 180dp).
    val balanceText = balance.formatRupiah()
    val balanceStyle = when {
        balanceText.length > 18 -> ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
        balanceText.length > 15 -> ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp)
        balanceText.length > 12 -> ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp)
        else -> ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
    }
    ArusCard(
        modifier = modifier.width(180.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ArusWalletTile(
                logoResId = logoResId,
                iconResId = iconResId,
                tileColor = tileColor,
                iconTint = iconTint,
                size = 36.dp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = walletName,
            style = ArusTypography.labelMedium,
            color = ArusTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = balanceText,
            style = balanceStyle,
            color = ArusTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// 9. ARUS SEGMENTED CONTROL (Pill Switcher)
// ============================================================================

@Composable
fun ArusSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = PillShape,
        color = ArusSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArusBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                // P2-6: pill hitam bergeser/berubah halus, bukan instan.
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) ArusBrandPrimary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "seg-bg-$index"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else ArusTextSecondary,
                    animationSpec = tween(200),
                    label = "seg-text-$index"
                )
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(PillShape)
                        .background(backgroundColor)
                        .clickable { onOptionSelected(index) }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = ArusTypography.titleMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = fontWeight,
                            letterSpacing = 0.sp,
                            color = textColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============================================================================
// 10. ARUS TRANSACTION ROW (List Row with Category Soft Tile)
// ============================================================================

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

@Composable
fun ArusTransactionRow(
    title: String,
    subtitle: String,
    amount: Long,
    type: TransactionType,
    iconResId: Int,
    tileColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    inCard: Boolean = false
) {
    val amountPrefix = when (type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "− "
        TransactionType.TRANSFER -> ""
    }
    val amountColor = when (type) {
        TransactionType.INCOME -> ArusIncome
        TransactionType.EXPENSE -> ArusExpense
        TransactionType.TRANSFER -> ArusTextPrimary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (inCard) Modifier
                else Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ArusSurface)
                    .border(1.dp, ArusBorder, RoundedCornerShape(16.dp))
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(if (inCard) PaddingValues(vertical = 12.dp) else PaddingValues(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArusCategoryTile(
            iconResId = iconResId,
            tileColor = tileColor,
            iconTint = iconTint,
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = ArusTypography.labelSmall,
                color = ArusTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$amountPrefix${amount.formatRupiah()}",
            style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = amountColor
        )
    }
}

// ============================================================================
// 11. ARUS BAR CHART (Compose Canvas Dual Bar Chart)
// ============================================================================

data class BarChartEntry(
    val label: String,
    val incomeAmount: Long,
    val expenseAmount: Long
)

@Composable
fun ArusBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier
) {
    // P2-9: batang yang diketuk menampilkan nilainya; reset bila data berubah.
    var selectedEntry by remember(entries) { mutableStateOf<BarChartEntry?>(null) }
    ArusCard(modifier = modifier) {
        Text(
            text = "PERBANDINGAN PEMASUKAN VS PENGELUARAN",
            style = ArusTypography.labelMedium,
            color = ArusTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty() || entries.all { it.incomeAmount == 0L && it.expenseAmount == 0L }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Belum ada data laporan",
                    style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ArusTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catat transaksi pertamamu untuk melihat grafik di sini.",
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val maxVal = entries.maxOf { maxOf(it.incomeAmount, it.expenseAmount) }.coerceAtLeast(1L)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                entries.forEach { entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedEntry = if (selectedEntry == entry) null else entry }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            val incomeRatio = (entry.incomeAmount.toFloat() / maxVal).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(incomeRatio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(ArusIncome)
                            )
                            val expenseRatio = (entry.expenseAmount.toFloat() / maxVal).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(expenseRatio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(ArusExpense)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.label,
                            style = ArusTypography.labelSmall,
                            color = ArusTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ArusIncome)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Pemasukan", style = ArusTypography.labelSmall, color = ArusTextPrimary)

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ArusExpense)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Pengeluaran", style = ArusTypography.labelSmall, color = ArusTextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (selectedEntry != null) {
                val selected = selectedEntry!!
                Text(
                    text = "${selected.label} · Masuk ${selected.incomeAmount.formatRupiah()} · Keluar ${selected.expenseAmount.formatRupiah()}",
                    style = ArusTypography.labelMedium,
                    color = ArusTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Ketuk batang untuk melihat nilai",
                    style = ArusTypography.labelSmall,
                    color = ArusTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ============================================================================
// 12. ARUS TOP APP BAR (Clean Top Bar with Back Navigation)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary
            )
        },
        navigationIcon = {
            if (showBackButton && onBackClick != null) {
                // P1-1: area sentuh 48dp, tombol visual tetap 40dp.
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ArusSurface)
                            .border(1.dp, ArusBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Kembali",
                            tint = ArusTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ArusBackground
        ),
        windowInsets = TopAppBarDefaults.windowInsets,
        modifier = modifier
    )
}

// ============================================================================
// 13. ARUS BOTTOM NAVIGATION (Floating Pill 4 Tabs + Center FAB (+))
// ============================================================================

enum class ArusTab(val title: String, val iconRes: Int) {
    BERANDA("Beranda", R.drawable.fa_house),
    TRANSAKSI("Transaksi", R.drawable.fa_receipt),
    LAPORAN("Laporan", R.drawable.fa_chart_column),
    PENGATURAN("Pengaturan", R.drawable.fa_sliders)
}

@Composable
fun ArusBottomNavigation(
	selectedTab: ArusTab,
	onTabSelected: (ArusTab) -> Unit,
	onAddClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.fillMaxWidth(),
		contentAlignment = Alignment.BottomCenter
	) {
		// Ultra-smooth multi-stop gradient layer extending lower down behind navbar.
		// Di mode overlay, konten layar menggeser di bawah gradasi ini.
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(140.dp)
				.background(
					brush = androidx.compose.ui.graphics.Brush.verticalGradient(
						colors = listOf(
							ArusBackground.copy(alpha = 0f),
							ArusBackground.copy(alpha = 0.05f),
							ArusBackground.copy(alpha = 0.2f),
							ArusBackground.copy(alpha = 0.45f),
							ArusBackground.copy(alpha = 0.75f),
							ArusBackground.copy(alpha = 0.95f),
							ArusBackground,
							ArusBackground
						)
					)
				)
		)

		Surface(
			shape = PillShape,
			color = ArusSurface,
			tonalElevation = 6.dp,
			shadowElevation = 8.dp,
			border = androidx.compose.foundation.BorderStroke(1.dp, ArusBorder),
			modifier = Modifier
				.fillMaxWidth()
				.navigationBarsPadding()
				.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(64.dp)
					.padding(horizontal = 4.dp, vertical = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				ArusTab.entries.forEachIndexed { index, tab ->
					if (index == 2) {
						// Celah tengah untuk FAB (dock di tengah pill).
						Spacer(modifier = Modifier.width(64.dp))
					}
					val isSelected = tab == selectedTab
					val backgroundColor = if (isSelected) ArusBrandPrimary else Color.Transparent
					val contentColor = if (isSelected) Color.White else ArusTextSecondary

					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.clip(PillShape)
							.background(backgroundColor)
							.clickable { onTabSelected(tab) },
						contentAlignment = Alignment.Center
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.Center
						) {
							Icon(
								painter = painterResource(id = tab.iconRes),
								contentDescription = tab.title,
								tint = contentColor,
								modifier = Modifier.size(19.dp)
							)
							Spacer(modifier = Modifier.height(2.dp))
							Text(
								text = tab.title,
								style = ArusTypography.labelSmall.copy(
									fontSize = 11.sp,
									fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
									color = contentColor
								)
							)
						}
					}
				}
			}
		}

		// FAB "+" di tengah — docked ke pill (2 tab kiri, 2 tab kanan).
		// Hitam (primary) supaya menonjol dari pill putih; "+" putih.
		Surface(
			onClick = onAddClick,
			shape = CircleShape,
			color = ArusBrandPrimary,
			tonalElevation = 6.dp,
			shadowElevation = 10.dp,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding()
				.padding(bottom = 22.dp)
				.size(60.dp)
				.semantics {
					// V2-P2-4: TalkBack membacakan FAB sebagai "Catat Transaksi".
					contentDescription = "Catat Transaksi"
					role = Role.Button
				}
		) {
			// Ikon "+" digambar dari dua batang (tidak ada drawable fa_plus di proyek).
			Box(contentAlignment = Alignment.Center) {
				Box(
					modifier = Modifier
						.size(width = 22.dp, height = 3.dp)
						.clip(RoundedCornerShape(1.5.dp))
						.background(Color.White)
				)
				Box(
					modifier = Modifier
						.size(width = 3.dp, height = 22.dp)
						.clip(RoundedCornerShape(1.5.dp))
						.background(Color.White)
				)
			}
		}
	}
}

// ============================================================================
// 14. ARUS EMPTY STATE & LOADING STATE
// ============================================================================

@Composable
fun ArusEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    iconResId: Int = R.drawable.fa_money_bill,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ArusBrandSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = ArusBrandPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = ArusTypography.titleLarge,
            color = ArusTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = ArusTypography.bodyMedium,
            color = ArusTextSecondary,
            textAlign = TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            ArusPrimaryButton(
                text = actionButtonText,
                onClick = onActionClick,
                modifier = Modifier.widthIn(max = 220.dp)
            )
        }
    }
}

@Composable
fun ArusLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = ArusBrandPrimary,
            strokeWidth = 3.dp
        )
    }
}

// P2-4: skeleton placeholder (kartu abu-abu berdenyut) untuk Beranda
// (showHero = kartu saldo + 2 kartu bulanan) dan Riwayat (tanpa hero,
// kotak pertama menyerupai kolom pencarian). Diam (abu statis) bila
// kurangi-animasi aktif di perangkat.
@Composable
fun ArusSkeletonLoading(
    modifier: Modifier = Modifier,
    showHero: Boolean = true,
    rows: Int = 5
) {
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val pulseAlpha = if (animationsEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "skeleton-pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "skeleton-alpha"
        ).value
    } else {
        0.7f
    }
    val skeletonColor = ArusBrandSoft.copy(alpha = pulseAlpha)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        if (showHero) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(skeletonColor)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(skeletonColor)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(skeletonColor)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(skeletonColor)
            )
        }
        repeat(rows) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(skeletonColor)
            )
        }
    }
}

// ============================================================================
// 15. ARUS HELPER COMPONENTS (Section Header, Date Header, Confirmation Sheet)
// ============================================================================

@Composable
fun ArusSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ArusTextPrimary
        )
        if (actionText != null && onActionClick != null) {
            // P1-1: area sentuh 48dp untuk aksi header.
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(PillShape)
                    .clickable { onActionClick() }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // P2-13: chevron sebagai ikon, bukan karakter teks ›.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionText,
                        style = ArusTypography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ArusBrandPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.fa_chevron_right),
                        contentDescription = null,
                        tint = ArusBrandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArusDateHeaderRow(
    dateText: String,
    modifier: Modifier = Modifier,
    totalDailyText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText.uppercase(),
            style = ArusTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = ArusTextSecondary
        )
        if (totalDailyText != null) {
            Text(
                text = totalDailyText,
                style = ArusTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ArusTextSecondary
            )
        }
    }
}

// ============================================================================
// 13b. ARUS DAY CARD (satu kartu per hari: header tanggal + total di dalam
// kartu, baris-baris di bawahnya dipisah divider — pola Bevel/Notion,
// audit action button 2026-11-21)
// ============================================================================

@Composable
fun ArusDayCard(
    dateText: String,
    totalDailyText: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ArusCard(modifier = modifier) {
        ArusDateHeaderRow(
            dateText = dateText,
            totalDailyText = totalDailyText
        )
        HorizontalDivider(color = ArusBorder, thickness = 1.dp)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusConfirmationSheet(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = R.drawable.fa_money_bill,
    tileColor: Color = NotionCoralSoft,
    iconTint: Color = NotionCoral,
    isDestructive: Boolean = false
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (iconResId != null) {
                ArusCategoryTile(
                    iconResId = iconResId,
                    tileColor = tileColor,
                    iconTint = iconTint,
                    size = 52.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = ArusTypography.bodyMedium,
                color = ArusTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArusSecondaryButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                if (isDestructive) {
                    ArusDestructiveButton(
                        text = confirmButtonText,
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ArusPrimaryButton(
                        text = confirmButtonText,
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ============================================================================
// ARUS ACTION PILL & FORM SHEET (komponen bersama untuk aksi kecil + form sheet)
enum class ArusActionPillStyle { NEUTRAL, DESTRUCTIVE }

@Composable
fun ArusActionPill(
    text: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ArusActionPillStyle = ArusActionPillStyle.NEUTRAL,
    iconResId: Int? = null,
    enabled: Boolean = true
) {
    val backgroundColor = when (style) {
        ArusActionPillStyle.NEUTRAL -> ArusBrandSoft
        ArusActionPillStyle.DESTRUCTIVE -> ArusExpenseSoft
    }
    val contentColor = when (style) {
        ArusActionPillStyle.NEUTRAL -> ArusBrandPrimary
        ArusActionPillStyle.DESTRUCTIVE -> ArusExpense
    }
    // Border tegas supaya tepi tombol terbaca di atas kartu putih (gaya SwiftUI).
    val borderColor = when (style) {
        ArusActionPillStyle.NEUTRAL -> ArusBorder
        ArusActionPillStyle.DESTRUCTIVE -> Color(0xFFF6C9D4)
    }
    val shape = if (text == null) CircleShape else PillShape
    val buttonModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
        .clickable(enabled = enabled) { onClick() }
        .alpha(if (enabled) 1f else 0.4f)
    if (text == null) {
        // Ikon saja: cakram visual 40dp, area sentuh tetap 48dp.
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled) { onClick() }
                .alpha(if (enabled) 1f else 0.4f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = buttonModifier
                .heightIn(min = 48.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = ArusTypography.labelLarge,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusFormSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary
            )
            content()
        }
    }
}

// ============================================================================
// P1-5: PEMILIH IKON & WARNA (form tambah dompet/kategori)
// ============================================================================

val ArusIconOptions: List<Pair<String, Int>> = listOf(
    "fa_wallet" to R.drawable.fa_wallet,
    "fa_building_columns" to R.drawable.fa_building_columns,
    "fa_credit_card" to R.drawable.fa_credit_card,
    "fa_money_bill" to R.drawable.fa_money_bill,
    "fa_basket_shopping" to R.drawable.fa_basket_shopping,
    "fa_utensils" to R.drawable.fa_utensils,
    "fa_car" to R.drawable.fa_car,
    "fa_gift" to R.drawable.fa_gift,
    "fa_heart_pulse" to R.drawable.fa_heart_pulse,
    "fa_gamepad" to R.drawable.fa_gamepad,
    "fa_briefcase" to R.drawable.fa_briefcase,
    "fa_store" to R.drawable.fa_store,
    "fa_tags" to R.drawable.fa_tags,
    "fa_receipt" to R.drawable.fa_receipt
)

val ArusColorOptions: List<String> = listOf(
    "#FFB110", "#F64932", "#B18164", "#097FE8",
    "#62AEF0", "#02093A", "#00A86B", "#8B5CF6"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArusIconPicker(
    selectedIcon: String,
    onIconChange: (String) -> Unit,
    selectedColorHex: String? = null,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 6
    ) {
        ArusIconOptions.forEach { (iconName, iconResId) ->
            val isSelected = iconName == selectedIcon
            val selectedTileColor = selectedColorHex?.let { arusColorFromHex(it, ArusBrandPrimary) }
                ?: ArusBrandPrimary
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(SoftTileShape)
                    .background(if (isSelected) selectedTileColor else ArusBrandSoft)
                    .border(1.dp, if (isSelected) selectedTileColor else ArusBorder, SoftTileShape)
                    .clickable { onIconChange(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = iconName,
                    tint = if (isSelected) Color.White else ArusTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun arusColorFromHex(hex: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArusColorPicker(
    selectedColorHex: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 8
    ) {
        ArusColorOptions.forEach { hex ->
            val color = arusColorFromHex(hex, NotionSky)
            val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        2.dp,
                        if (isSelected) ArusBrandPrimary else Color.Transparent,
                        CircleShape
                    )
                    .clickable { onColorChange(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Pemilih merek dompet dari master data (logo + nama). Dipakai di form
// dompet saat jenisnya Bank / E-Wallet.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArusWalletBrandPicker(
    brands: List<WalletBrand>,
    selectedSlug: String?,
    onBrandSelected: (WalletBrand) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Cari..."
) {
    var query by remember { mutableStateOf("") }
    val filteredBrands = remember(brands, query) {
        if (query.isBlank()) brands
        else brands.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = searchPlaceholder,
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.fa_magnifying_glass),
                    contentDescription = null,
                    tint = ArusTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Hapus pencarian",
                            tint = ArusTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = PillShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ArusSurface,
                unfocusedContainerColor = ArusSurface,
                focusedBorderColor = ArusBrandPrimary,
                unfocusedBorderColor = ArusBorder
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (filteredBrands.isEmpty()) {
            Text(
                text = "Tidak ditemukan",
                style = ArusTypography.bodyMedium,
                color = ArusTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredBrands.forEach { brand ->
                    val isSelected = brand.slug == selectedSlug
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SoftTileShape)
                            .background(if (isSelected) ArusBrandSoft else ArusSurface)
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) ArusBrandPrimary else ArusBorder,
                                SoftTileShape
                            )
                            .clickable { onBrandSelected(brand) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArusSurface)
                                .border(1.dp, ArusBorder, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = brand.logoResId),
                                contentDescription = brand.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = brand.name,
                            style = ArusTypography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = ArusTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Terpilih",
                                tint = ArusBrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 13b. ARUS PASSWORD SHEET (Bottom Sheet dengan Field Kata Sandi)
// Dipakai dialog Backup/Restore: ArusConfirmationSheet tidak punya slot input,
// jadi kata sandi tidak pernah bisa dimasukkan (alur backup putus di UI).
// Tombol konfirmasi nonaktif sampai kata sandi >= 4 karakter.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusPasswordSheet(
    title: String,
    message: String,
    confirmButtonText: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isValid = password.length >= 4

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = ArusTypography.bodyMedium,
                color = ArusTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "Kata Sandi",
                        style = ArusTypography.labelLarge,
                        color = ArusTextPrimary
                    )
                },
                placeholder = {
                    Text(
                        text = "Minimal 4 karakter",
                        style = ArusTypography.bodyMedium,
                        color = ArusTextSecondary
                    )
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.fa_eye_slash else R.drawable.fa_eye
                            ),
                            contentDescription = if (isPasswordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                            tint = ArusTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                isError = password.isNotEmpty() && !isValid,
                supportingText = if (password.isNotEmpty() && !isValid) {
                    {
                        Text(
                            text = "Kata sandi minimal 4 karakter.",
                            style = ArusTypography.labelSmall,
                            color = ArusExpense
                        )
                    }
                } else null,
                textStyle = ArusTypography.bodyLarge.copy(color = ArusTextPrimary),
                shape = PillShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ArusSurface,
                    unfocusedContainerColor = ArusSurface,
                    errorContainerColor = ArusSurface,
                    focusedBorderColor = ArusBrandPrimary,
                    unfocusedBorderColor = ArusBorder,
                    errorBorderColor = ArusExpense
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArusSecondaryButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                if (isDestructive) {
                    Button(
                        onClick = onConfirm,
                        enabled = isValid,
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArusExpense,
                            contentColor = Color.White,
                            disabledContainerColor = ArusBorder,
                            disabledContentColor = ArusTextSecondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = ArusTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    ArusPrimaryButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = isValid
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class SelectionItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconResId: Int? = null,
    val tileColor: Color = NotionSkySoft,
    val iconTint: Color = NotionSky,
    val logoResId: Int? = null
)

// ============================================================================
// ARUS DATE PICKER SHEET (Bottom Sheet Kalender, locale id-ID)
// Menggantikan DatePickerDialog (modal tengah, headline bahasa Inggris):
// kalender yang sama dipindah ke ModalBottomSheet ala Bevel — sudut atas
// membulat, drag handle, header "Pilih Tanggal" + chip "Hari ini", tombol
// pill hitam "Pilih Tanggal" + teks "Batal". Locale id-ID dipaksa lewat
// DatePickerState(locale = ...) supaya bulan/hari/headline berbahasa
// Indonesia walau locale sistem HP bahasa Inggris.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusDatePickerSheet(
    initialDateMillis: Long,
    onDateSelected: (utcDateMillis: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tengah malam UTC hari ini (dipakai chip "Hari ini").
    val todayUtcMillis = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            val now = Calendar.getInstance()
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val datePickerState = remember(initialDateMillis) {
        DatePickerState(
            locale = Locale("id", "ID"),
            initialSelectedDateMillis = initialDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val endOfToday = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    return utcTimeMillis <= endOfToday
                }
            }
        )
    }
    val isTodaySelected = datePickerState.selectedDateMillis == todayUtcMillis
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Tanggal",
                    style = ArusTypography.titleLarge,
                    color = ArusTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                ArusChip(
                    text = "Hari ini",
                    selected = isTodaySelected,
                    onClick = {
                        datePickerState.selectedDateMillis = todayUtcMillis
                        datePickerState.displayedMonthMillis = todayUtcMillis
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            DatePicker(
                state = datePickerState,
                title = null,
                showModeToggle = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            ArusPrimaryButton(
                text = "Pilih Tanggal",
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                },
                enabled = datePickerState.selectedDateMillis != null
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Batal",
                    style = ArusTypography.labelLarge,
                    color = ArusTextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusSelectionSheet(
    title: String,
    items: List<SelectionItem>,
    selectedItemId: String?,
    onItemSelected: (SelectionItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
        ) {
            Text(
                text = title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // V2-P2-8: daftar pemilih bisa di-scroll (kategori bisa > 8).
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEach { item ->
                    val isSelected = item.id == selectedItemId
                    val rowBorderColor = if (isSelected) ArusBrandPrimary else ArusBorder
                    val rowBgColor = if (isSelected) ArusBrandSoft else ArusSurface

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(rowBgColor)
                            .border(1.dp, rowBorderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                onItemSelected(item)
                                onDismiss()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (item.iconResId != null || item.logoResId != null) {
                                ArusWalletTile(
                                    logoResId = item.logoResId,
                                    iconResId = item.iconResId ?: R.drawable.fa_wallet,
                                    tileColor = item.tileColor,
                                    iconTint = item.iconTint,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column {
                                Text(
                                    text = item.title,
                                    style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ArusTextPrimary
                                )
                                if (item.subtitle != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = ArusTypography.labelSmall,
                                        color = ArusTextSecondary
                                    )
                                }
                            }
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onItemSelected(item)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ArusBrandPrimary,
                                unselectedColor = ArusTextSecondary
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ============================================================================
// 15B. ARUS TRANSACTION DETAIL SHEET (Modal Bottom Sheet Preview)
// ============================================================================

data class TransactionDetailData(
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val dateText: String,
    val walletText: String,
    val targetWalletText: String? = null,
    val noteText: String? = null,
    val iconResId: Int = R.drawable.fa_tags,
    val tileColor: Color = NotionSkySoft,
    val iconTint: Color = NotionSky
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArusTransactionDetailSheet(
    data: TransactionDetailData,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (amountPrefix, amountColor) = when (data.type) {
        TransactionType.INCOME -> "+ " to ArusIncome
        TransactionType.EXPENSE -> "- " to ArusExpense
        TransactionType.TRANSFER -> "" to ArusTextPrimary
    }

    val typeBadgeText = when (data.type) {
        TransactionType.INCOME -> "PEMASUKAN"
        TransactionType.EXPENSE -> "PENGELUARAN"
        TransactionType.TRANSFER -> "TRANSFER"
    }

    val trendStyle = when (data.type) {
        TransactionType.INCOME -> TrendStyle.POSITIVE
        TransactionType.EXPENSE -> TrendStyle.NEGATIVE
        TransactionType.TRANSFER -> TrendStyle.NEUTRAL
    }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Notion Soft Tile Icon Badge
            ArusCategoryTile(
                iconResId = data.iconResId,
                tileColor = data.tileColor,
                iconTint = data.iconTint,
                size = 52.dp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.title,
                style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            ArusTrendBadge(text = typeBadgeText, style = trendStyle)

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Large Nominal Amount
            Text(
                text = "$amountPrefix${data.amount.formatRupiah()}",
                style = ArusTypography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Property Details List Card
            ArusCard(cornerRadius = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(label = "Tanggal & Waktu", value = data.dateText)
                    if (data.targetWalletText != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dompet",
                                style = ArusTypography.bodyMedium,
                                color = ArusTextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = data.walletText,
                                    style = ArusTypography.bodyMedium,
                                    color = ArusTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.fa_chevron_right),
                                    contentDescription = null,
                                    tint = ArusTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = data.targetWalletText,
                                    style = ArusTypography.bodyMedium,
                                    color = ArusTextPrimary
                                )
                            }
                        }
                    } else {
                        DetailRow(label = "Dompet", value = data.walletText)
                    }
                    if (!data.noteText.isNullOrBlank()) {
                        DetailRow(label = "Catatan", value = data.noteText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArusSecondaryButton(
                    text = "Tutup",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                )
                ArusSecondaryButton(
                    text = "Ubah",
                    onClick = {
                        onEditClick()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.fa_pen,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                )
                // P1-8: aksi destruktif memakai merah, bukan CTA primer hitam.
                ArusDestructiveButton(
                    text = "Hapus",
                    onClick = {
                        onDeleteClick()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.fa_trash_can,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ArusTypography.bodyMedium,
            color = ArusTextSecondary
        )
        Text(
            text = value,
            style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ArusTextPrimary
        )
    }
}

// ============================================================================
// 16. BEVEL ADVANCED COMPONENTS (Badge, Toggle Row, Progress Card, Keypad)
// ============================================================================

enum class TrendStyle { POSITIVE, NEGATIVE, NEUTRAL, FUTURE }

@Composable
fun ArusTrendBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: TrendStyle = TrendStyle.NEUTRAL
) {
    val (bgColor, textColor) = when (style) {
        TrendStyle.POSITIVE -> ArusIncomeSoft to ArusIncome
        TrendStyle.NEGATIVE -> ArusExpenseSoft to ArusExpense
        TrendStyle.NEUTRAL -> ArusBrandSoft to ArusTextPrimary
        TrendStyle.FUTURE -> NotionLavenderSoft to NotionLavender
    }

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ArusTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun ArusToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconResId: Int? = null,
    tileColor: Color = NotionLavenderSoft,
    iconTint: Color = NotionLavender
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ArusSurface)
            .border(1.dp, ArusBorder, RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (iconResId != null) {
                ArusCategoryTile(
                    iconResId = iconResId,
                    tileColor = tileColor,
                    iconTint = iconTint,
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = title,
                    style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ArusTextPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = ArusTypography.labelSmall,
                        color = ArusTextSecondary
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ArusBrandPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ArusBorder
            )
        )
    }
}

@Composable
fun ArusProgressCard(
    title: String,
    currentAmount: Long,
    totalAmount: Long,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progressColor: Color = ArusIncome
) {
    val progress = if (totalAmount > 0) {
        (currentAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()

    ArusCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = ArusTypography.labelMedium,
                color = ArusTextSecondary
            )
            ArusTrendBadge(
                text = "$percentage%",
                // V2-P2-7: badge merah hanya bila pengeluaran melebihi pemasukan
                // (>100%), selaras dengan subtitle "masih dalam batas aman".
                style = if (currentAmount <= totalAmount) TrendStyle.POSITIVE else TrendStyle.NEGATIVE
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = currentAmount.formatRupiah(),
                style = ArusTypography.displayMedium,
                color = ArusTextPrimary
            )
            Text(
                text = "dari ${totalAmount.formatRupiah()}",
                style = ArusTypography.labelSmall,
                color = ArusTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(PillShape),
            color = progressColor,
            trackColor = ArusBrandSoft
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = ArusTypography.labelSmall,
                color = ArusTextSecondary
            )
        }
    }
}

@Composable
fun ArusKeypadView(
    onKeyPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "⌫")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(PillShape)
                            .background(ArusSurface)
                            .border(1.dp, ArusBorder, PillShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (key == "⌫") onDeletePress()
                                else onKeyPress(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            style = ArusTypography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (key == "000") 16.sp else 22.sp
                            ),
                            color = ArusTextPrimary
                        )
                    }
                }
            }
        }
    }
}

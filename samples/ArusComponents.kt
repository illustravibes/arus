package com.illustravibes.arus.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Simple, reusable visual primitives. Prefer Material 3 for complex controls. */
@Composable
fun ArusCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = ArusColors.Card,
        border = BorderStroke(1.dp, ArusColors.Border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun ArusPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArusColors.Brand,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ArusSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        if (!eyebrow.isNullOrBlank()) {
            Text(
                text = eyebrow,
                color = ArusColors.Income,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = title,
            color = ArusColors.Ink,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun ArusTransactionRow(
    title: String,
    subtitle: String,
    amountLabel: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier,
) {
    // If this row becomes clickable, use a clickable Surface + semantics and 48dp target.
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isIncome) ArusColors.IncomeSoft else ArusColors.ExpenseSoft,
        ) {
            Text(
                text = if (isIncome) "+" else "−",
                color = if (isIncome) ArusColors.Income else ArusColors.Expense,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, color = ArusColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = amountLabel,
            color = if (isIncome) ArusColors.Income else ArusColors.Expense,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

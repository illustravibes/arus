package com.illustravibes.arus.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** UI-only preview. All amounts are DEMO data; no database/business logic. */
@Preview(name = "Arus — Homepage Style", showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ArusHomeStylePreview() {
    ArusTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ArusColors.Page)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(ArusColors.Brand, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(10.dp))
                Text("Arus", style = MaterialTheme.typography.titleLarge)
            }

            ArusSectionHeader(
                eyebrow = "Keuangan lebih teratur",
                title = "Uangmu, lebih jelas.",
            )

            ArusCard(Modifier.fillMaxWidth()) {
                Text("Total Saldo", color = ArusColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(6.dp))
                Text(
                    "Rp 8.450.000",
                    color = ArusColors.Ink,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(8.dp))
                Text("Saldo dari seluruh dompet", color = ArusColors.Income, style = MaterialTheme.typography.labelLarge)
            }

            ArusSectionHeader(title = "Aksi cepat")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("+ Masuk", "− Keluar", "⇄ Transfer").forEach { label ->
                    ArusCard(Modifier.weight(1f)) {
                        Text(label, color = ArusColors.Ink, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            ArusSectionHeader(eyebrow = "Bulan ini", title = "Ringkasan keuangan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArusCard(Modifier.weight(1f)) {
                    Text("Pemasukan", color = ArusColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.size(6.dp))
                    Text("Rp 5.200.000", color = ArusColors.Income, fontWeight = FontWeight.Bold)
                }
                ArusCard(Modifier.weight(1f)) {
                    Text("Pengeluaran", color = ArusColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.size(6.dp))
                    Text("Rp 2.150.000", color = ArusColors.Expense, fontWeight = FontWeight.Bold)
                }
            }

            ArusSectionHeader(title = "Aktivitas terbaru")
            ArusCard(Modifier.fillMaxWidth()) {
                ArusTransactionRow("Gaji", "Dompet Bank · Hari ini", "+Rp 5.200.000", true)
                ArusTransactionRow("Belanja", "Dompet Tunai · Kemarin", "−Rp 175.000", false)
            }
            ArusPrimaryButton(
                text = "+ Tambah transaksi",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

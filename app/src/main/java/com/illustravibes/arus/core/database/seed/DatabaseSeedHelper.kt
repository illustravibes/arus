package com.illustravibes.arus.core.database.seed

import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import kotlinx.coroutines.flow.first

object DatabaseSeedHelper {

    suspend fun seedDefaultDataIfEmpty(db: ArusDatabase) {
        val wallets = db.walletDao().getWalletsFlow().first()
        if (wallets.isEmpty()) {
            db.walletDao().insertWallet(
                WalletEntity(
                    name = "Dompet Tunai",
                    icon = "fa_money_bill",
                    colorHex = "#FFB110",
                    initialBalance = 1000000L
                )
            )
            db.walletDao().insertWallet(
                WalletEntity(
                    name = "Bank BCA",
                    icon = "fa_building_columns",
                    colorHex = "#62AEF0",
                    initialBalance = 2000000L
                )
            )
        }

        val categories = db.categoryDao().getActiveCategoriesFlow().first()
        if (categories.isEmpty()) {
            // Default Income Categories
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Gaji", icon = "fa_money_bill", colorHex = "#00A86B", type = "INCOME")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Bonus & Hadiah", icon = "fa_gift", colorHex = "#8B5CF6", type = "INCOME")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Pemasukan Lain", icon = "fa_tags", colorHex = "#62AEF0", type = "INCOME")
            )

            // Default Expense Categories
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Makanan & Minuman", icon = "fa_utensils", colorHex = "#F64932", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Belanja Harian", icon = "fa_basket_shopping", colorHex = "#FFB110", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Transportasi", icon = "fa_car", colorHex = "#62AEF0", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Rumah & Tagihan", icon = "fa_house", colorHex = "#8B5CF6", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Hiburan", icon = "fa_gamepad", colorHex = "#B18164", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Kesehatan", icon = "fa_heart_pulse", colorHex = "#E53935", type = "EXPENSE")
            )
            db.categoryDao().insertCategory(
                CategoryEntity(name = "Pengeluaran Lain", icon = "fa_tags", colorHex = "#60657A", type = "EXPENSE")
            )
        }
    }
}

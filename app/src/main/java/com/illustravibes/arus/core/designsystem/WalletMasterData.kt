package com.illustravibes.arus.core.designsystem

import com.illustravibes.arus.R
import com.illustravibes.arus.core.database.entity.WalletType

// Master data dompet: merek bank & e-wallet yang logonya dibundel di aplikasi.
// Logo dari idn-finlogos (hafidznoor/idn-finlogos), artwork CC BY-NC 4.0,
// merek milik pemegangnya. Hanya logo pilihan yang dibundel (PNG @3x,
// 240x240) supaya APK tetap ringan — tanpa dependensi baru.
data class WalletBrand(
    val slug: String,
    val name: String,
    val type: WalletType,
    val logoResId: Int
)

object WalletMasterData {
    val banks: List<WalletBrand> = listOf(
        WalletBrand("bca", "BCA", WalletType.BANK, R.drawable.logo_bca),
        WalletBrand("mandiri", "Mandiri", WalletType.BANK, R.drawable.logo_mandiri),
        WalletBrand("bri", "BRI", WalletType.BANK, R.drawable.logo_bri),
        WalletBrand("bni", "BNI", WalletType.BANK, R.drawable.logo_bni),
        WalletBrand("bsi", "BSI", WalletType.BANK, R.drawable.logo_bsi),
        WalletBrand("btn", "BTN", WalletType.BANK, R.drawable.logo_btn),
        WalletBrand("cimb-niaga", "CIMB Niaga", WalletType.BANK, R.drawable.logo_cimb_niaga),
        WalletBrand("danamon", "Danamon", WalletType.BANK, R.drawable.logo_danamon),
        WalletBrand("permata", "Permata", WalletType.BANK, R.drawable.logo_permata),
        WalletBrand("ocbc-nisp", "OCBC", WalletType.BANK, R.drawable.logo_ocbc_nisp),
        WalletBrand("maybank", "Maybank", WalletType.BANK, R.drawable.logo_maybank),
        WalletBrand("mega", "Mega", WalletType.BANK, R.drawable.logo_mega),
        WalletBrand("paninbank", "Panin", WalletType.BANK, R.drawable.logo_paninbank),
        WalletBrand("jago", "Jago", WalletType.BANK, R.drawable.logo_jago),
        WalletBrand("seabank", "SeaBank", WalletType.BANK, R.drawable.logo_seabank),
        WalletBrand("blu-bca", "Blu", WalletType.BANK, R.drawable.logo_blu_bca)
    )

    val ewallets: List<WalletBrand> = listOf(
        WalletBrand("gopay", "GoPay", WalletType.EWALLET, R.drawable.logo_gopay),
        WalletBrand("ovo", "OVO", WalletType.EWALLET, R.drawable.logo_ovo),
        WalletBrand("dana", "DANA", WalletType.EWALLET, R.drawable.logo_dana),
        WalletBrand("shopee-pay", "ShopeePay", WalletType.EWALLET, R.drawable.logo_shopee_pay),
        WalletBrand("linkaja", "LinkAja", WalletType.EWALLET, R.drawable.logo_linkaja)
    )

    val all: List<WalletBrand> = banks + ewallets

    fun brandsFor(type: WalletType): List<WalletBrand> = when (type) {
        WalletType.BANK -> banks
        WalletType.EWALLET -> ewallets
        WalletType.CASH -> emptyList()
    }

    fun brandForSlug(slug: String?): WalletBrand? =
        slug?.let { s -> all.find { it.slug == s } }

    fun logoResIdForSlug(slug: String?): Int? = brandForSlug(slug)?.logoResId
}

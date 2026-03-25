package de.miningsim.model

import org.bukkit.inventory.ItemStack

data class ShopItem(
    val item: ItemStack,
    var buyPrice: Double,   // -1 = nicht kaufbar
    var sellPrice: Double   // -1 = nicht verkaufbar
) {
    val isBuyable get() = buyPrice >= 0
    val isSellable get() = sellPrice >= 0
}
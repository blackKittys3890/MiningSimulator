package de.miningsim.model

import org.bukkit.inventory.ItemStack

data class ShopItem(
    val item: ItemStack,
    var buyPrice: Double,
    var sellPrice: Double
) {
    val isBuyable get() = buyPrice >= 0
    val isSellable get() = sellPrice >= 0
}
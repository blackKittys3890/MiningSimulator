package de.miningsim.model

import org.bukkit.inventory.ItemStack

data class TradeOffer(
    val ingredient1: ItemStack,
    val ingredient2: ItemStack?,
    val result: ItemStack
)
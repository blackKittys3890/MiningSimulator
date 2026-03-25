package de.miningsim.model

import org.bukkit.inventory.ItemStack

/**
 * Represents one trade row: up to two input items → one result item.
 * ingredient2 is optional (null = only one input needed).
 */
data class TradeOffer(
    val ingredient1: ItemStack,
    val ingredient2: ItemStack?,   // null = not required
    val result: ItemStack
)
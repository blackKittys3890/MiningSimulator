package de.miningsim.gui

import de.miningsim.MiningSimulator
import de.miningsim.model.TradeOffer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.MerchantRecipe
import java.util.UUID

class PlayerShopGUI(private val plugin: MiningSimulator) : Listener {

    companion object {
        val viewingVillager = mutableMapOf<UUID, UUID>()

        fun open(plugin: MiningSimulator, player: Player, villagerUUID: UUID) {
            val trades = plugin.shopManager.getTrades(villagerUUID)
            if (trades.isEmpty()) {
                player.sendMessage("§cDieser Shop hat noch keine Angebote.")
                return
            }

            val merchant = plugin.server.createMerchant("§6§l⚒ Shop")

            merchant.recipes = trades.map { offer ->
                val recipe = MerchantRecipe(
                    offer.result.clone(),
                    0,             // uses so far
                    Int.MAX_VALUE, // max uses — never runs out
                    false,         // no XP for player
                    0,             // villager XP
                    1.0f,          // price multiplier
                    0,             // demand
                    0              // specialPrice
                )
                recipe.addIngredient(offer.ingredient1.clone())
                offer.ingredient2?.let { recipe.addIngredient(it.clone()) }
                recipe
            }

            viewingVillager[player.uniqueId] = villagerUUID
            player.openMerchant(merchant, true)
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        viewingVillager.remove(player.uniqueId)
    }
}
package de.miningsim.events

import de.miningsim.MiningSimulator
import de.miningsim.gui.AdminTradeEditorGUI
import de.miningsim.gui.PlayerShopGUI
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class VillagerShopListener(private val plugin: MiningSimulator) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.rightClicked.type != EntityType.VILLAGER) return
        val villager = event.rightClicked as Villager
        if (!plugin.shopManager.isShopVillager(villager.uniqueId)) return

        event.isCancelled = true
        val player = event.player

        if (player.isSneaking && player.hasPermission("miningsim.admin")) {
            AdminTradeEditorGUI.open(plugin, player, villager.uniqueId)
        } else {
            PlayerShopGUI.open(plugin, player, villager.uniqueId)
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entityType != EntityType.VILLAGER) return
        if (plugin.shopManager.isShopVillager(event.entity.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        plugin.dataManager.unload(event.player.uniqueId)
    }
}
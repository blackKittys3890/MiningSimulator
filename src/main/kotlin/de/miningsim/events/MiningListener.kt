package de.miningsim.events

import de.miningsim.MiningSimulator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MiningListener(private val plugin: MiningSimulator) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val zone = plugin.mineManager.getZoneAt(event.block.location) ?: return
        val player = event.player

        event.isDropItems = false
        event.block.getDrops(player.inventory.itemInMainHand).forEach { drop ->
            player.inventory.addItem(drop).forEach { (_, leftover) ->
                player.world.dropItemNaturally(player.location, leftover)
            }
        }

        plugin.mineManager.onBlockBroken(zone, event.block.location)
    }
}
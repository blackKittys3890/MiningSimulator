package de.miningsim.gui

import de.miningsim.MiningSimulator
import de.miningsim.model.TradeOffer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class AdminTradeEditorGUI(private val plugin: MiningSimulator) : Listener {

    companion object {
        const val TITLE_PREFIX = "§c§l[EDITOR] Trades: "
        const val ROWS_PER_PAGE = 5
        const val INV_SIZE = 54

        val editingVillager = mutableMapOf<UUID, UUID>()
        val currentPage     = mutableMapOf<UUID, Int>()

        fun open(plugin: MiningSimulator, player: Player, villagerUUID: UUID, page: Int = 0) {
            val trades = plugin.shopManager.getTrades(villagerUUID)
            val maxPage = trades.size / ROWS_PER_PAGE
            val safePage = page.coerceIn(0, maxPage)

            val inv = Bukkit.createInventory(null, INV_SIZE,
                "$TITLE_PREFIX${villagerUUID.toString().take(8)} §7[${safePage + 1}]")

            val gray = grayGlass()
            for (i in 0 until INV_SIZE) inv.setItem(i, gray)

            val start = safePage * ROWS_PER_PAGE
            for (row in 0 until ROWS_PER_PAGE) {
                val tradeIdx = start + row
                val baseSlot = row * 9
                if (tradeIdx < trades.size) {
                    val t = trades[tradeIdx]
                    inv.setItem(baseSlot + 0, t.ingredient1.clone())
                    inv.setItem(baseSlot + 1, t.ingredient2?.clone() ?: emptySlotMarker("§7(leer)"))
                    inv.setItem(baseSlot + 2, arrowItem())
                    inv.setItem(baseSlot + 3, t.result.clone())
                    inv.setItem(baseSlot + 4, deleteButton(tradeIdx))
                } else {
                    inv.setItem(baseSlot + 0, emptySlotMarker("§a+ Zutat 1 hier ablegen"))
                    inv.setItem(baseSlot + 1, emptySlotMarker("§a+ Zutat 2 (optional)"))
                    inv.setItem(baseSlot + 2, arrowItem())
                    inv.setItem(baseSlot + 3, emptySlotMarker("§a+ Ergebnis hier ablegen"))
                    break
                }
            }

            inv.setItem(45, navButton("§e◀ Zurück", Material.ARROW, safePage > 0))
            inv.setItem(46, navButton("§e▶ Weiter",  Material.ARROW, safePage < maxPage))
            inv.setItem(47, addButton())

            editingVillager[player.uniqueId] = villagerUUID
            currentPage[player.uniqueId]     = safePage
            player.openInventory(inv)
        }

        private fun grayGlass(): ItemStack {
            val i = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            val m = i.itemMeta!!; m.setDisplayName(" "); i.itemMeta = m; return i
        }

        private fun arrowItem(): ItemStack {
            val i = ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
            val m = i.itemMeta!!; m.setDisplayName("§6→"); i.itemMeta = m; return i
        }

        private fun emptySlotMarker(label: String): ItemStack {
            val i = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            val m = i.itemMeta!!; m.setDisplayName(label); i.itemMeta = m; return i
        }

        private fun deleteButton(index: Int): ItemStack {
            val i = ItemStack(Material.RED_STAINED_GLASS_PANE)
            val m = i.itemMeta!!
            m.setDisplayName("§c✖ Trade #${index + 1} löschen")
            m.lore = listOf("§7Klick zum Löschen")
            i.itemMeta = m; return i
        }

        private fun navButton(label: String, mat: Material, active: Boolean): ItemStack {
            val mat2 = if (active) mat else Material.GRAY_STAINED_GLASS_PANE
            val i = ItemStack(mat2)
            val m = i.itemMeta!!; m.setDisplayName(if (active) label else "§8-"); i.itemMeta = m; return i
        }

        private fun addButton(): ItemStack {
            val i = ItemStack(Material.LIME_STAINED_GLASS_PANE)
            val m = i.itemMeta!!
            m.setDisplayName("§a✚ Neue Trade-Seite")
            m.lore = listOf("§7Springt zur nächsten freien Zeile")
            i.itemMeta = m; return i
        }

        fun isGlassOrMarker(item: ItemStack?): Boolean {
            if (item == null || item.type == Material.AIR) return true
            return item.type.name.endsWith("STAINED_GLASS_PANE") || item.type == Material.GLASS_PANE
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title
        if (!title.startsWith(TITLE_PREFIX)) return

        val villagerUUID = editingVillager[player.uniqueId] ?: return
        val page = currentPage[player.uniqueId] ?: 0
        val slot = event.rawSlot

        if (slot < 0 || slot >= INV_SIZE) return

        val row = slot / 9
        val col = slot % 9

        if (row == 5) {
            event.isCancelled = true
            when (slot) {
                45 -> if (page > 0) open(plugin, player, villagerUUID, page - 1)
                46 -> open(plugin, player, villagerUUID, page + 1)
                47 -> {
                    val trades = plugin.shopManager.getTrades(villagerUUID)
                    val newPage = trades.size / ROWS_PER_PAGE
                    open(plugin, player, villagerUUID, newPage)
                }
            }
            return
        }

        if (col == 2) { event.isCancelled = true; return }
        if (col > 4)  { event.isCancelled = true; return }

        if (col == 4) {
            event.isCancelled = true
            val current = event.currentItem ?: return
            if (current.type == Material.RED_STAINED_GLASS_PANE) {
                val tradeIdx = page * ROWS_PER_PAGE + row
                val trades = plugin.shopManager.getTrades(villagerUUID)
                if (tradeIdx in trades.indices) {
                    plugin.shopManager.removeTrade(villagerUUID, tradeIdx)
                    player.sendMessage("§c✖ Trade #${tradeIdx + 1} gelöscht.")
                    open(plugin, player, villagerUUID, page)
                }
            }
            return
        }

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            saveRowFromInventory(player, villagerUUID, page, row)
        }, 1L)
    }

    private fun saveRowFromInventory(player: Player, villagerUUID: UUID, page: Int, row: Int) {
        val openInv = player.openInventory.topInventory
        if (!player.openInventory.title.startsWith(TITLE_PREFIX)) return

        val baseSlot = row * 9
        val ing1Item = openInv.getItem(baseSlot + 0)
        val ing2Item = openInv.getItem(baseSlot + 1)
        val resItem  = openInv.getItem(baseSlot + 3)

        val ing1 = if (isGlassOrMarker(ing1Item)) null else ing1Item!!.clone()
        val ing2 = if (isGlassOrMarker(ing2Item)) null else ing2Item!!.clone()
        val res  = if (isGlassOrMarker(resItem))  null else resItem!!.clone()

        if (ing1 == null || res == null) return

        val trades = plugin.shopManager.getTrades(villagerUUID)
        val tradeIdx = page * ROWS_PER_PAGE + row
        val effectiveIdx = if (tradeIdx <= trades.size) tradeIdx else trades.size

        val offer = TradeOffer(ing1, ing2, res)
        plugin.shopManager.setTrade(villagerUUID, effectiveIdx, offer)
        player.sendMessage("§a✔ Trade #${effectiveIdx + 1} gespeichert.")
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        if (!event.view.title.startsWith(TITLE_PREFIX)) return
        editingVillager.remove(player.uniqueId)
        currentPage.remove(player.uniqueId)
    }
}
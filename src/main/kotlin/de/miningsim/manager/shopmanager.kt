package de.miningsim.manager

import de.miningsim.MiningSimulator
import de.miningsim.model.TradeOffer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class ShopManager(private val plugin: MiningSimulator) {

    private val shopVillagers = mutableSetOf<UUID>()
    private val tradeData = mutableMapOf<UUID, MutableList<TradeOffer>>()

    private val file = File(plugin.dataFolder, "shops.yml").also {
        plugin.dataFolder.mkdirs()
        if (!it.exists()) it.createNewFile()
    }
    private val cfg = YamlConfiguration.loadConfiguration(file)

    init { load() }

    fun isShopVillager(uuid: UUID) = uuid in shopVillagers
    fun getAllShopVillagers(): Set<UUID> = shopVillagers

    fun registerVillager(uuid: UUID) {
        shopVillagers += uuid
        tradeData.getOrPut(uuid) { mutableListOf() }
        save()
    }

    fun unregisterVillager(uuid: UUID) {
        shopVillagers -= uuid
        tradeData -= uuid
        save()
    }

    fun getTrades(uuid: UUID): List<TradeOffer> =
        tradeData.getOrDefault(uuid, emptyList())

    fun addTrade(uuid: UUID, offer: TradeOffer) {
        tradeData.getOrPut(uuid) { mutableListOf() }.add(offer)
        save()
    }

    fun setTrade(uuid: UUID, index: Int, offer: TradeOffer) {
        val list = tradeData.getOrPut(uuid) { mutableListOf() }
        if (index in list.indices) {
            list[index] = offer
        } else {
            // Append — handles new trades on any page correctly
            list.add(offer)
        }
        save()
    }

    fun removeTrade(uuid: UUID, index: Int) {
        tradeData[uuid]?.removeAt(index)
        save()
    }

    fun moveTrade(uuid: UUID, from: Int, to: Int) {
        val list = tradeData[uuid] ?: return
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        save()
    }

    fun save() {
        cfg.set("villagers", null)
        for (uuid in shopVillagers) {
            val vp = "villagers.$uuid"
            val trades = tradeData[uuid] ?: continue
            trades.forEachIndexed { i, offer ->
                val tp = "$vp.trades.$i"
                cfg.set("$tp.ingredient1", offer.ingredient1)
                cfg.set("$tp.ingredient2", offer.ingredient2)
                cfg.set("$tp.result", offer.result)
            }
        }
        cfg.save(file)
    }

    private fun load() {
        val section = cfg.getConfigurationSection("villagers") ?: return
        for (uuidStr in section.getKeys(false)) {
            val uuid = runCatching { UUID.fromString(uuidStr) }.getOrNull() ?: continue
            shopVillagers += uuid
            val trades = mutableListOf<TradeOffer>()
            val tradesSection = cfg.getConfigurationSection("villagers.$uuidStr.trades")
            tradesSection?.getKeys(false)?.sortedBy { it.toIntOrNull() ?: 0 }?.forEach { idxStr ->
                val tp = "villagers.$uuidStr.trades.$idxStr"
                val ing1 = cfg.getItemStack("$tp.ingredient1") ?: return@forEach
                val ing2 = cfg.getItemStack("$tp.ingredient2")
                val res  = cfg.getItemStack("$tp.result") ?: return@forEach
                trades += TradeOffer(ing1, ing2, res)
            }
            tradeData[uuid] = trades
        }
        plugin.logger.info("Loaded ${shopVillagers.size} shop villager(s).")
    }
}
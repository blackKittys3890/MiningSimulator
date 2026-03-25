package de.miningsim.manager

import de.miningsim.MiningSimulator
import de.miningsim.model.PlayerData
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class DataManager(private val plugin: MiningSimulator) {

    private val cache = mutableMapOf<UUID, PlayerData>()
    private val file = File(plugin.dataFolder, "playerdata.yml").also {
        plugin.dataFolder.mkdirs()
        if (!it.exists()) it.createNewFile()
    }
    private val cfg = YamlConfiguration.loadConfiguration(file)

    fun get(uuid: UUID, name: String): PlayerData {
        cache[uuid]?.let { it.name = name; return it }
        val coins = cfg.getDouble("players.$uuid.coins", 0.0)
        return PlayerData(uuid, name, coins).also { cache[uuid] = it }
    }

    fun save(data: PlayerData) {
        cfg.set("players.${data.uuid}.name", data.name)
        cfg.set("players.${data.uuid}.coins", data.coins)
        cfg.save(file)
    }

    fun unload(uuid: UUID) { cache.remove(uuid)?.let { save(it) } }

    fun saveAll() { cache.values.forEach { save(it) } }
}
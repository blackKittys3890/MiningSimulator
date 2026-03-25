package de.miningsim.manager

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import de.miningsim.MiningSimulator
import de.miningsim.model.MineZone
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

class MineManager(private val plugin: MiningSimulator) {

    private val zones = mutableMapOf<String, MineZone>()
    private val file = File(plugin.dataFolder, "mines.yml").also {
        plugin.dataFolder.mkdirs()
        if (!it.exists()) it.createNewFile()
    }
    private val cfg = YamlConfiguration.loadConfiguration(file)

    init { loadZones() }

    // ── WorldEdit ────────────────────────────────────────────────────────────

    fun createZoneFromSelection(player: Player, name: String): MineZone? {
        val we = Bukkit.getPluginManager().getPlugin("WorldEdit") as? WorldEditPlugin
            ?: run { player.sendMessage("§cWorldEdit nicht gefunden!"); return null }

        val region = try {
            we.getSession(player).getSelection(BukkitAdapter.adapt(player.world))
        } catch (e: Exception) {
            player.sendMessage("§cKeine WorldEdit-Auswahl! Wähle einen Bereich mit //wand.")
            return null
        } ?: run { player.sendMessage("§cKeine WorldEdit-Auswahl!"); return null }

        val world = player.world
        val min = Location(world, region.minimumPoint.x().toDouble(), region.minimumPoint.y().toDouble(), region.minimumPoint.z().toDouble())
        val max = Location(world, region.maximumPoint.x().toDouble(), region.maximumPoint.y().toDouble(), region.maximumPoint.z().toDouble())

        return MineZone(name, min, max).also {
            zones[name] = it
            saveZones()
        }
    }

    // ── Zone ops ─────────────────────────────────────────────────────────────

    fun getZone(name: String) = zones[name]
    fun getAllZones(): Collection<MineZone> = zones.values

    fun getZoneAt(loc: Location): MineZone? = zones.values.firstOrNull { it.contains(loc) }

    fun onBlockBroken(zone: MineZone, loc: Location) {
        zone.markBroken(loc)
        val delay = plugin.config.getLong("settings.respawn-delay-ticks", 60)
        object : BukkitRunnable() {
            override fun run() {
                if (loc.block.type == Material.AIR) {
                    loc.block.type = zone.getRandomBlock()
                }
                zone.clearBroken()
            }
        }.runTaskLater(plugin, delay)
    }

    fun resetZone(zone: MineZone) {
        zone.getAllLocations().forEach { it.block.type = zone.getRandomBlock() }
        zone.clearBroken()
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    fun saveZones() {
        cfg.set("mines", null)
        for (zone in zones.values) {
            val p = "mines.${zone.name}"
            cfg.set("$p.world", zone.min.world?.name)
            cfg.set("$p.min.x", zone.min.blockX)
            cfg.set("$p.min.y", zone.min.blockY)
            cfg.set("$p.min.z", zone.min.blockZ)
            cfg.set("$p.max.x", zone.max.blockX)
            cfg.set("$p.max.y", zone.max.blockY)
            cfg.set("$p.max.z", zone.max.blockZ)
            zone.blockComposition.forEach { (mat, weight) ->
                cfg.set("$p.blocks.${mat.name}", weight)
            }
        }
        cfg.save(file)
    }

    private fun loadZones() {
        val minesSection = cfg.getConfigurationSection("mines") ?: return
        for (name in minesSection.getKeys(false)) {
            val p = "mines.$name"
            val world = Bukkit.getWorld(cfg.getString("$p.world") ?: continue) ?: continue
            val min = Location(world, cfg.getInt("$p.min.x").toDouble(), cfg.getInt("$p.min.y").toDouble(), cfg.getInt("$p.min.z").toDouble())
            val max = Location(world, cfg.getInt("$p.max.x").toDouble(), cfg.getInt("$p.max.y").toDouble(), cfg.getInt("$p.max.z").toDouble())
            val zone = MineZone(name, min, max)
            cfg.getConfigurationSection("$p.blocks")?.getKeys(false)?.forEach { matName ->
                runCatching { Material.valueOf(matName) }.getOrNull()?.let { mat ->
                    zone.setBlockWeight(mat, cfg.getInt("$p.blocks.$matName"))
                }
            }
            zones[name] = zone
            plugin.logger.info("Loaded mine zone: $name")
        }
    }

    fun saveAll() = saveZones()
}
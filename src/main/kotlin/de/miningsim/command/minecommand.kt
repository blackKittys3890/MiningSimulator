package de.miningsim.commands

import de.miningsim.MiningSimulator
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MineCommand(private val plugin: MiningSimulator) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { sender.sendMessage("Nur für Spieler."); return true }
        if (!sender.hasPermission("miningsim.admin")) { sender.sendMessage("§cKeine Berechtigung."); return true }
        if (args.isEmpty()) { sendHelp(sender); return true }

        when (args[0].lowercase()) {

            "setzone" -> {
                if (args.size < 2) { sender.sendMessage("§cVerwendung: /mine setzone <name>"); return true }
                val zone = plugin.mineManager.createZoneFromSelection(sender, args[1]) ?: return true
                sender.sendMessage("§a✔ Zone §e${zone.name} §aerstellt! (${zone.getTotalBlocks()} Blöcke)")
                sender.sendMessage("§eBlöcke hinzufügen: §7/mine setblock ${zone.name} <MATERIAL> <Gewicht>")
            }

            "setblock" -> {
                if (args.size < 4) { sender.sendMessage("§cVerwendung: /mine setblock <zone> <MATERIAL> <Gewicht>"); return true }
                val zone = plugin.mineManager.getZone(args[1]) ?: run { sender.sendMessage("§cZone nicht gefunden."); return true }
                val mat = runCatching { Material.valueOf(args[2].uppercase()) }.getOrNull()
                    ?: run { sender.sendMessage("§cUnbekanntes Material: ${args[2]}"); return true }
                val weight = args[3].toIntOrNull()
                    ?: run { sender.sendMessage("§cUngültiges Gewicht."); return true }
                zone.setBlockWeight(mat, weight)
                plugin.mineManager.saveZones()
                sender.sendMessage("§a✔ ${mat.name} (Gewicht $weight) zu Zone §e${zone.name} §ahinzugefügt.")
            }

            "removeblock" -> {
                if (args.size < 3) { sender.sendMessage("§cVerwendung: /mine removeblock <zone> <MATERIAL>"); return true }
                val zone = plugin.mineManager.getZone(args[1]) ?: run { sender.sendMessage("§cZone nicht gefunden."); return true }
                val mat = runCatching { Material.valueOf(args[2].uppercase()) }.getOrNull()
                    ?: run { sender.sendMessage("§cUnbekanntes Material."); return true }
                zone.removeBlock(mat)
                plugin.mineManager.saveZones()
                sender.sendMessage("§a✔ ${mat.name} aus Zone entfernt.")
            }

            "reset" -> {
                if (args.size < 2) { sender.sendMessage("§cVerwendung: /mine reset <zone>"); return true }
                val zone = plugin.mineManager.getZone(args[1]) ?: run { sender.sendMessage("§cZone nicht gefunden."); return true }
                if (zone.blockComposition.isEmpty()) { sender.sendMessage("§cKeine Blöcke konfiguriert! Nutze /mine setblock."); return true }
                plugin.mineManager.resetZone(zone)
                sender.sendMessage("§a✔ Zone §e${zone.name} §azurückgesetzt.")
            }

            "info" -> {
                if (args.size < 2) {
                    sender.sendMessage("§6=== Mine-Zonen ===")
                    if (plugin.mineManager.getAllZones().isEmpty()) {
                        sender.sendMessage("§7Keine Zonen erstellt.")
                    } else {
                        plugin.mineManager.getAllZones().forEach {
                            sender.sendMessage("§e- ${it.name} §7(${it.getTotalBlocks()} Blöcke)")
                        }
                    }
                    return true
                }
                val zone = plugin.mineManager.getZone(args[1]) ?: run { sender.sendMessage("§cZone nicht gefunden."); return true }
                sender.sendMessage("§6=== Zone: ${zone.name} ===")
                sender.sendMessage("§eGesamt: ${zone.getTotalBlocks()} | Abgebaut: ${zone.getBrokenCount()}")
                sender.sendMessage("§eBlock-Zusammensetzung:")
                zone.blockComposition.forEach { (mat, weight) ->
                    sender.sendMessage("§7  ${mat.name} - Gewicht: §e$weight")
                }
            }

            else -> sendHelp(sender)
        }
        return true
    }

    private fun sendHelp(p: Player) {
        p.sendMessage("§6=== MiningSimulator ===")
        p.sendMessage("§e/mine setzone <name> §7- WorldEdit-Selection als Zone setzen")
        p.sendMessage("§e/mine setblock <zone> <MAT> <Gewicht> §7- Block hinzufügen")
        p.sendMessage("§e/mine removeblock <zone> <MAT> §7- Block entfernen")
        p.sendMessage("§e/mine reset <zone> §7- Zone zurücksetzen")
        p.sendMessage("§e/mine info [zone] §7- Zonen anzeigen")
    }
}
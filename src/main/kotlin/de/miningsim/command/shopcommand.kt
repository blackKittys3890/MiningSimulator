package de.miningsim.commands

import de.miningsim.MiningSimulator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager

class ShopCommand(private val plugin: MiningSimulator) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { sender.sendMessage("Nur für Spieler."); return true }
        if (!sender.hasPermission("miningsim.admin")) { sender.sendMessage("§cKeine Berechtigung."); return true }

        if (args.isEmpty()) {
            sender.sendMessage("§e/mshop spawn §7- Shop-Villager spawnen")
            sender.sendMessage("§e/mshop remove §7- Nächsten Shop-Villager entfernen")
            return true
        }

        when (args[0].lowercase()) {
            "spawn" -> {
                val villager = sender.world.spawnEntity(sender.location, EntityType.VILLAGER) as Villager
                villager.setAI(false)
                villager.isInvulnerable = true
                villager.customName = "§6§l⚒ Shop"
                villager.isCustomNameVisible = true
                villager.profession = Villager.Profession.TOOLSMITH
                plugin.shopManager.registerVillager(villager.uniqueId)
                sender.sendMessage("§a✔ Shop-Villager gespawnt!")
                sender.sendMessage("§eShift+Rechtsklick §7zum Bearbeiten.")
            }
            "remove" -> {
                val found = sender.getNearbyEntities(5.0, 5.0, 5.0)
                    .filterIsInstance<Villager>()
                    .firstOrNull { plugin.shopManager.isShopVillager(it.uniqueId) }

                if (found != null) {
                    plugin.shopManager.unregisterVillager(found.uniqueId)
                    found.remove()
                    sender.sendMessage("§a✔ Shop-Villager entfernt.")
                } else {
                    sender.sendMessage("§cKein Shop-Villager in der Nähe (5 Blöcke).")
                }
            }
            else -> sender.sendMessage("§cUnbekannter Unterbefehl.")
        }
        return true
    }
}
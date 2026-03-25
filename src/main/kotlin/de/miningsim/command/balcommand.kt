package de.miningsim.commands

import de.miningsim.MiningSimulator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalCommand(private val plugin: MiningSimulator) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { sender.sendMessage("Nur für Spieler."); return true }
        val data = plugin.dataManager.get(sender.uniqueId, sender.name)
        sender.sendMessage("§6💰 Dein Kontostand: §e${"%.2f".format(data.coins)} Coins")
        return true
    }
}
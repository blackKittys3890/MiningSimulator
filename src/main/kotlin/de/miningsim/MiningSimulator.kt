package de.miningsim

import de.miningsim.commands.BalCommand
import de.miningsim.commands.MineCommand
import de.miningsim.commands.ShopCommand
import de.miningsim.events.MiningListener
import de.miningsim.events.VillagerShopListener
import de.miningsim.gui.AdminTradeEditorGUI
import de.miningsim.gui.PlayerShopGUI
import de.miningsim.manager.DataManager
import de.miningsim.manager.MineManager
import de.miningsim.manager.ShopManager
import org.bukkit.plugin.java.JavaPlugin

class MiningSimulator : JavaPlugin() {

    lateinit var dataManager: DataManager
    lateinit var mineManager: MineManager
    lateinit var shopManager: ShopManager

    override fun onEnable() {
        saveDefaultConfig()

        dataManager = DataManager(this)
        mineManager = MineManager(this)
        shopManager = ShopManager(this)

        getCommand("mine")!!.setExecutor(MineCommand(this))
        getCommand("mshop")!!.setExecutor(ShopCommand(this))
        getCommand("bal")!!.setExecutor(BalCommand(this))

        server.pluginManager.registerEvents(MiningListener(this), this)
        server.pluginManager.registerEvents(VillagerShopListener(this), this)
        server.pluginManager.registerEvents(PlayerShopGUI(this), this)
        server.pluginManager.registerEvents(AdminTradeEditorGUI(this), this)

        logger.info("MiningSimulator enabled!")
    }

    override fun onDisable() {
        dataManager.saveAll()
        mineManager.saveAll()
        logger.info("MiningSimulator disabled.")
    }
}
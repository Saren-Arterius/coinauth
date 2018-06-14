package net.wtako.coinauth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sxtanna.database.Kedis
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.Jedis
import java.io.File
import java.sql.Time
import java.time.Instant

@Suppress("unused")
class Main : JavaPlugin(), Listener, CommandExecutor {

    companion object {
        val caConfigAdapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(CoinAuthConfig::class.java)
                .lenient()
                .indent("  ")!!
        lateinit var caConfig: CoinAuthConfig
        lateinit var jedis: Jedis
    }

    override fun onEnable() {
        logger.info { "Example plugin started!" }
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getScheduler().runTaskTimer(this, {  Bukkit.broadcastMessage("Runnable ran successfully at " + Time.from(Instant.now())) }, 20 * 30, 20 * 30)
        val configFile = File(dataFolder, "config.json")
        if (configFile.exists()) {
            caConfig = caConfigAdapter.fromJson(configFile.readText())!!
        } else {
            caConfig = CoinAuthConfig()
            logger.info("Saving default config to ${configFile.absolutePath}")
            configFile.parentFile.mkdirs()
        }
        configFile.writeText(caConfigAdapter.toJson(caConfig))

        val kedis = Kedis(caConfig.redisConfig)
        kedis.enable()
        jedis = kedis.resource()
    }

    @EventHandler
    fun exampleEvent(event: PlayerJoinEvent) {
        event.player.sendMessage("Hello from kotlin!")
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            //Kotlin casts automatically using smart-casts!
            val p: Player = sender
            //there is no switch in kotlin!!!
            when (command?.name) {
                "testcmd" -> {
                    p.sendMessage("Hello from kotlin " + p.name + "!")
                    p.sendMessage("Have a cake !")
                    p.inventory.addItem(ItemStack(Material.CAKE_BLOCK))
                }
                else -> {
                    p.sendMessage("Command not found!")
                }
            }
            return true
        }
        sender?.sendMessage("Your not a player! ")
        return true

    }

}
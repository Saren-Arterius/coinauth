package net.wtako.coinauth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sxtanna.database.Kedis
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*
import kotlin.collections.HashMap

@Suppress("unused")
class Main : JavaPlugin(), Listener, CommandExecutor {

    companion object {
        val caConfigAdapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(CoinAuthConfig::class.java)
                .lenient()
                .indent("  ")!!
        val playersPendingAuth = HashSet<Player>()
        val playerSessions = HashMap<Player, String>()
        lateinit var caConfig: CoinAuthConfig
        lateinit var kedis: Kedis
    }

    override fun onEnable() {
        val configFile = File(dataFolder, "config.json")
        if (configFile.exists()) {
            caConfig = caConfigAdapter.fromJson(configFile.readText())!!
        } else {
            caConfig = CoinAuthConfig()
            logger.info("Saving default config to ${configFile.absolutePath}")
            configFile.parentFile.mkdirs()
        }
        configFile.writeText(caConfigAdapter.toJson(caConfig))

        kedis = Kedis(caConfig.redisConfig)
        kedis.enable()

        server.pluginManager.registerEvents(this, this)
        server.scheduler.runTaskTimerAsynchronously(this, {
            val jedis = kedis.resource()
            playersPendingAuth
                    .filter { p -> p in playerSessions && jedis.exists("session:auth:${playerSessions[p]}") }
                    .forEach { p ->
                        playersPendingAuth.remove(p)
                        p.sendMessage("[CoinAuth] Successfully logged in.")
                    }
        }, 0, 20 * caConfig.authPollIntervalSeconds)

        server.scheduler.runTaskTimerAsynchronously(this, {
            val jedis = kedis.resource()
            playersPendingAuth
                    .filter { p -> p in playerSessions && !jedis.exists("session:auth:${playerSessions[p]}") }
                    .forEach { p -> sendLoginMessage(p, playerSessions[p]!!) }
        }, 0, 20 * caConfig.loginMessageIntervalSeconds)

        server.scheduler.runTaskTimerAsynchronously(this, {
            val jedis = kedis.resource()
            server.onlinePlayers
                    .filter { p -> p !in playerSessions || !jedis.exists("session:uuid:${playerSessions[p]}") }
                    .forEach { p ->
                        playerSessions.remove(p)
                        playersPendingAuth.remove(p)
                        server.scheduler.runTask(this) {
                            p.kickPlayer("Your session has expired.")
                        }
                    }
        }, 0, 20 * caConfig.kickPollIntervalSeconds)
    }

    private fun sendLoginMessage(player: Player, sessionID: String) {
        sendMessage(player, "Please login first. -> Click me! <-", "${caConfig.loginBaseURL}/login/$sessionID")
    }

    private fun sendMessage(player: Player, message: String, url: String) {
        val cmd = "tellraw ${player.name} {\"text\":\"[CoinAuth] $message\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"$url\"}}"
        server.dispatchCommand(Bukkit.getConsoleSender(), cmd)
    }

    @EventHandler
    fun loginEvent(event: PlayerJoinEvent) {
        val jedis = kedis.resource()
        val sessionID = UUID.randomUUID().toString()
        jedis.setex("session:uuid:$sessionID", caConfig.authTimeoutSeconds, event.player.uniqueId.toString())
        playerSessions.put(event.player, sessionID)
        playersPendingAuth.add(event.player)
        sendLoginMessage(event.player, sessionID)
    }


    @EventHandler
    fun logoutEvent(event: PlayerQuitEvent) {
        playersPendingAuth.remove(event.player)
        playerSessions.remove(event.player)
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerMoveEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerInteractEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerDropItemEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerInteractEntityEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }


    @EventHandler
    fun otherPlayerEvent(event: EntityPickupItemEvent) {
        if (event.entity is Player && event.entity as Player in playersPendingAuth) {
            event.isCancelled = true
        }
    }


    @EventHandler
    fun otherPlayerEvent(event: PlayerBucketEmptyEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerBucketFillEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: AsyncPlayerChatEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun otherPlayerEvent(event: PlayerCommandPreprocessEvent) {
        if (event.player in playersPendingAuth) {
            event.isCancelled = true
        }
    }

}
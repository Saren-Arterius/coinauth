package net.wtako.coinauth

import com.sxtanna.database.config.KedisConfig

data class CoinAuthConfig(
        val redisConfig: KedisConfig = KedisConfig(user = KedisConfig.UserOptions("password")),
        val kickPollIntervalSeconds: Long = 10,
        val loginMessageIntervalSeconds: Long = 5,
        val authPollIntervalSeconds: Long = 1,
        val authTimeoutSeconds: Int = 60,
        val loginBaseURL: String = "http://localhost:31300"
)
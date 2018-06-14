package net.wtako.coinauth

import com.sxtanna.database.config.KedisConfig

data class CoinAuthConfig(
        val redisConfig: KedisConfig = KedisConfig.DEFAULT,
        val pollIntervalSeconds: Int = 10,
        val authTimeoutSeconds: Int = 300

)
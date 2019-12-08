package ch.keepcalm.demo.gateway.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Component
class LoggingGatewayFilterFactory : AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config>(Config::class.java) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BASE_MSG = "baseMessage"
        const val PRE_LOGGER = "preLogger"
        const val POST_LOGGER = "postLogger"
    }

    override fun shortcutFieldOrder(): List<String> {
        return Arrays.asList(BASE_MSG, PRE_LOGGER, POST_LOGGER)
    }

    override fun apply(config: Config): GatewayFilter {
        return OrderedGatewayFilter(GatewayFilter { exchange: ServerWebExchange?, chain: GatewayFilterChain ->
            if (config.isPreLogger) logger.info("Pre GatewayFilter logging: ${config.baseMessage}")
            chain.filter(exchange)
                    .then(Mono.fromRunnable {
                        if (config.isPostLogger) logger.info("Post GatewayFilter logging: ${config.baseMessage}")
                    })
        }, 1)
    }

    class Config {
        var baseMessage: String? = null
        var isPreLogger = false
        var isPostLogger = false

        constructor() {}
        constructor(baseMessage: String?, preLogger: Boolean, postLogger: Boolean) : super() {
            this.baseMessage = baseMessage
            isPreLogger = preLogger
            isPostLogger = postLogger
        }
    }
}
package ch.keepcalm.demo.gateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class LoggingGlobalFiltersConfigurations {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun postGlobalFilter(): GlobalFilter {
        return GlobalFilter { exchange: ServerWebExchange?, chain: GatewayFilterChain ->
            chain.filter(exchange)
                    .then(Mono.fromRunnable { logger.info("Global Post Filter executed") })
        }
    }
}
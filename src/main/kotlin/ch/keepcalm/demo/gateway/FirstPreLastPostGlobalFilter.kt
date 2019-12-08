package ch.keepcalm.demo.gateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class FirstPreLastPostGlobalFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        logger.info("First Pre Global Filter")

        return chain.filter(exchange)
                .then(Mono.fromRunnable { logger.info("Last Post Global Filter") })
    }

    override fun getOrder(): Int {
        return -1
    }
}
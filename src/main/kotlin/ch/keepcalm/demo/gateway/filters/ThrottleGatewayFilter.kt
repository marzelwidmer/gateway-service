package ch.keepcalm.demo.gateway.filters


import org.apache.commons.logging.LogFactory
import org.isomorphism.util.TokenBucket
import org.isomorphism.util.TokenBuckets
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit


/**
 * Sample throttling filter. See https://github.com/bbeck/token-bucket
 */
class ThrottleGatewayFilter : GatewayFilter {
    var capacity = 0
    var refillTokens = 0
    var refillPeriod = 0
    var refillUnit: TimeUnit? = null

    fun setCapacity(capacity: Int): ThrottleGatewayFilter {
        this.capacity = capacity
        return this
    }

    fun setRefillTokens(refillTokens: Int): ThrottleGatewayFilter {
        this.refillTokens = refillTokens
        return this
    }

    fun setRefillPeriod(refillPeriod: Int): ThrottleGatewayFilter {
        this.refillPeriod = refillPeriod
        return this
    }

    fun setRefillUnit(refillUnit: TimeUnit?): ThrottleGatewayFilter {
        this.refillUnit = refillUnit
        return this
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val tokenBucket: TokenBucket = TokenBuckets.builder().withCapacity(capacity.toLong())
                .withFixedIntervalRefillStrategy(refillTokens.toLong(), refillPeriod.toLong(), refillUnit)
                .build()
        // TODO: get a token bucket for a key
        log.debug("TokenBucket capacity: " + tokenBucket.getCapacity())
        val consumed: Boolean = tokenBucket.tryConsume()
        if (consumed) {
            return chain.filter(exchange)
        }
        exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        return exchange.response.setComplete()
    }

    companion object {
        private val log = LogFactory.getLog(ThrottleGatewayFilter::class.java)
    }
}
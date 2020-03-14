package ch.keepcalm.demo.gateway

import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import ch.sbb.esta.openshift.gracefullshutdown.GracefulshutdownSpringApplication
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.jaegertracing.internal.samplers.ConstSampler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.support.beans
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtSecurityProperties::class)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
class GatewayApplication {

    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"))
        System.out.println("Date in Europe/Zurich: ${Date().toString()}")
    }


    @Bean
    fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
        return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
            factory.configureDefault { id: String? ->
                Resilience4JConfigBuilder(id)
                        .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                .slidingWindowSize(5)
                                .permittedNumberOfCallsInHalfOpenState(5)
                                .failureRateThreshold(50.0f)
                                .waitDurationInOpenState(Duration.ofMillis(30))
                        .slowCallRateThreshold(50.0F)
                                .build())
                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build())
                        .build()
            }
        }
    }
}

fun main(args: Array<String>) {
//    GracefulshutdownSpringApplication.run(GatewayApplication::class.java, *args)
    runApplication<GatewayApplication>(*args) {
        val context = beans {
            bean{
                RedisRateLimiter(5, 7)
            }
            bean {
                KeyResolver { Mono.just("1") }
            }
        }
        addInitializers(context)
    }
}

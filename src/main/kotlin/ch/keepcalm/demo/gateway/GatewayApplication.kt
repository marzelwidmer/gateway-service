package ch.keepcalm.demo.gateway

import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.jaegertracing.internal.samplers.ConstSampler
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
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Consumer


@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtSecurityProperties::class)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
class GatewayApplication {
    // This definition is used to get Intellij happy
    @Bean
    fun reactiveResilience4JCircuitBreakerFactory() = ReactiveResilience4JCircuitBreakerFactory()
}


fun main(args: Array<String>) {
//    GracefulshutdownSpringApplication.run(GatewayApplication::class.java, *args)
    runApplication<GatewayApplication>(*args) {
        val context = beans {
            // RedisRateLimiter
            bean {
                RedisRateLimiter(5, 7)
            }
            bean {
                KeyResolver { Mono.just("1") }
            }
            // CircuitBreaker
//            bean {
//                Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//                    factory.configure(Consumer { builder: Resilience4JConfigBuilder ->
//                        builder
//                                .timeLimiterConfig(TimeLimiterConfig.custom()
//                                        .timeoutDuration(Duration.ofSeconds(5)).build())
//                                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                    }, "greet")
//                }
//            }

            // CircuitBreaker
            bean {
                Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
                    factory.configureDefault { id: String? ->
                        Resilience4JConfigBuilder(id)
                                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                        .slidingWindowSize(5)
                                        .permittedNumberOfCallsInHalfOpenState(5)
                                        .failureRateThreshold(50.0f)
                                        .waitDurationInOpenState(Duration.ofMillis(30))
                                        .slowCallRateThreshold(50.0F)
                                        .build())
                                .timeLimiterConfig(TimeLimiterConfig.custom()
                                        .timeoutDuration(Duration.ofSeconds(5)).build())
                                .build()
                    }
                }
            }
            // Jaeger Tracing
            bean {
                io.jaegertracing.Configuration("gateway-service")
                        .withSampler(io.jaegertracing.Configuration.SamplerConfiguration
                                .fromEnv()
                                .withType(ConstSampler.TYPE)
                                .withParam(1))
                        .withReporter(io.jaegertracing.Configuration.ReporterConfiguration
                                .fromEnv()
                                .withLogSpans(true))
            }

        }
        addInitializers(context)
    }
}

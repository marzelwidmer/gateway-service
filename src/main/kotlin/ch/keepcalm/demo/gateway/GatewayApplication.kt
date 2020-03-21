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
//    @Bean
//    fun reactiveResilience4JCircuitBreakerFactory ( )= ReactiveResilience4JCircuitBreakerFactory()
}

//{
//To enable circuit breaker built on top of Resilience4J we need to declare Customizer bean that is
// passed a ReactiveResilience4JCircuitBreakerFactory. The very simple configuration contains default circuit breaker
// settings and and defines timeout duration using TimeLimiterConfig.
// For the first test I decided to set 200 milliseconds timeout.
//    @Bean
//    fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory>? {
//        return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//            factory.configureDefault { id: String? ->
//                Resilience4JConfigBuilder(id)
//                        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build())
//                        .build()
//            }
//        }
//    }

// The property slidingWindowSize defines how many outcome calls has to be recorded when a circuit breaker is closed.
// Assuming we have the same test endpoints what will happen if we change this value to 10 as shown below?
//    @Bean
//    fun defaultCustomizerWithSlidingWindowSize(): Customizer<ReactiveResilience4JCircuitBreakerFactory>? {
//        return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//            factory.configureDefault { id: String? ->
//                Resilience4JConfigBuilder(id)
//                        .circuitBreakerConfig(CircuitBreakerConfig.custom()
//                                .slidingWindowSize(10)
//                                .build())
//                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build()).build()
//            }
//        }
//    }


//    @Bean
//    fun defaultCustomizerFailureRateThreshold(): Customizer<ReactiveResilience4JCircuitBreakerFactory>? {
//        return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//            factory.configureDefault { id: String? ->
//                Resilience4JConfigBuilder(id)
//                        .circuitBreakerConfig(CircuitBreakerConfig.custom()
//                                .slidingWindowSize(10)
//                                .failureRateThreshold(66.6f)
//                                .build())
//                        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build()).build()
//            }
//        }
//    }

//@Bean
//fun slowCusomtizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory>? {
//    return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//        factory.configure(Consumer { builder: Resilience4JConfigBuilder ->
//            builder
//                    .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build())
//                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//        }, "slow", "slowflux")
//        factory.addCircuitBreakerCustomizer(Customizer { circuitBreaker: CircuitBreaker ->
//            circuitBreaker.eventPublisher
//                    .onError(normalFluxErrorConsumer).onSuccess(normalFluxSuccessConsumer)
//        }, "normalflux")
//    }
//}
//    @Bean
//    fun circuitBreakerConfig() = CircuitBreakerConfig.custom()
//            .minimumNumberOfCalls(2)
//            .failureRateThreshold(50F)
//            .waitDurationInOpenState(Duration.ofMillis(1000))
//            .permittedNumberOfCallsInHalfOpenState(2)
//            .slidingWindowSize(2)
//            .build()
//}


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

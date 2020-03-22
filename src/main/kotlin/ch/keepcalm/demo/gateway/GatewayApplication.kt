package ch.keepcalm.demo.gateway

import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
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
import org.zalando.problem.ProblemModule
import org.zalando.problem.violations.ConstraintViolationProblemModule
import reactor.core.publisher.Mono
import java.time.Duration


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
            // Zanlando - https://github.com/zalando/problem-spring-web/tree/master/problem-spring-webflux
            bean{
                ProblemModule()
            }
            // Zanlando - https://github.com/zalando/problem-spring-web/tree/master/problem-spring-webflux
            bean {
                ConstraintViolationProblemModule()
            }
            // CircuitBreaker - https://resilience4j.readme.io/docs/circuitbreaker
            bean {
                Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
                    factory.configureDefault { id: String? ->
                        Resilience4JConfigBuilder(id)
                                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                        // Configures the size of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.
                                        .slidingWindowSize(5)
                                        // Configures the number of permitted calls when the CircuitBreaker is half open.
                                        .permittedNumberOfCallsInHalfOpenState(5)
                                        // Configures the failure rate threshold in percentage
                                        // If the failure rate is equal or greater than the threshold the CircuitBreaker transitions to open and starts short-circuiting calls.
                                        .failureRateThreshold(50.0f)
                                        // Configures the wait duration which specifies how long the CircuitBreaker should stay open, before it switches to half open.
                                        // Default value is 60 seconds.
                                        .waitDurationInOpenState(Duration.ofMillis(30))
                                        // Configures a threshold in percentage. The CircuitBreaker considers a call as slow when the call duration is greater than
                                        // slowCallDurationThreshold(Duration)
                                        //  When the percentage of slow calls is equal or greater the threshold, the CircuitBreaker transitions to open and starts short-circuiting calls.
                                        .slowCallRateThreshold(50.0F)
                                        .build())
                                // Configure the time limiter
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

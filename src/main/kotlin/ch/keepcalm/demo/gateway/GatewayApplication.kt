package ch.keepcalm.demo.gateway

import ch.sbb.esta.openshift.gracefullshutdown.GracefulshutdownSpringApplication
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.annotation.PostConstruct
import io.jaegertracing.internal.samplers.ConstSampler
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

@SpringBootApplication
@EnableDiscoveryClient
class GatewayApplication {
    @Bean
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange: ServerWebExchange? -> Mono.just("1") }
    }

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
    GracefulshutdownSpringApplication.run(GatewayApplication::class.java, *args)
}


@RestController
class LivenessProbe {
    @GetMapping(value = ["/alive"])
    fun alive() = "ok"
}


@Component
class TracerConfiguration {
    @Bean
    fun jaegerTracer(): io.jaegertracing.Configuration = io.jaegertracing.Configuration("gateway-service")
            .withSampler(io.jaegertracing.Configuration.SamplerConfiguration
                    .fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1))
            .withReporter(io.jaegertracing.Configuration.ReporterConfiguration
                    .fromEnv()
                    .withLogSpans(true))
}



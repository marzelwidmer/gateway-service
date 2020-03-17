package ch.keepcalm.demo.gateway

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.time.Duration


@RestController
class FailingRestController(private val failingService: FailingService, private val reactiveCircuitBreakerFactory: ReactiveCircuitBreakerFactory<*, *>) {

    @GetMapping("/greet")
    fun greet(@RequestParam name: String?): Mono<String> {
        val results = failingService.greet(name) // cold
        val circuitBreaker = reactiveCircuitBreakerFactory.create("greet")
        return circuitBreaker.run(results) { Mono.just("fallback") }
    }
}

@Service
class FailingService() {

    fun greet(name: String?): Mono<String> {
        val seconds  = Math.random() * 10
        name?.map {
            return Mono.just("Hello ${name} ! this call took $seconds" ).delayElement(Duration.ofSeconds(seconds.toLong()))
        }.isNullOrEmpty().apply {
            return Mono.error(NullPointerException())
        }
    }
}


@RestController
class ClientController(private val webClient: WebClient.Builder,
                       private val reactiveCircuitBreakerFactory: ReactiveCircuitBreakerFactory<*, *>) {
    @GetMapping("/foo")
    fun hello(): Mono<String> {
        return webClient.build()
                .get().uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .scheme("http")
                            .host("slow-service").path("/slow")
                            .build()
                }
                .retrieve().bodyToMono(String::class.java)
                .transform { it: Mono<String>? ->
                    val rcb = reactiveCircuitBreakerFactory.create("slow")
                    rcb.run(it) { throwable: Throwable? -> Mono.just("fallback") }
                }
    }

}
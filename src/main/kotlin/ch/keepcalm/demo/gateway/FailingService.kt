package ch.keepcalm.demo.gateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Duration


@RestController
class FailingRestController(private val failingService: FailingService, private val reactiveCircuitBreakerFactory: ReactiveCircuitBreakerFactory<*, *>) {

    @GetMapping("/greet")
    fun greet(@RequestParam name: String?): Mono<String> {
        val results = failingService.greet(name) // cold
        val circuitBreaker = reactiveCircuitBreakerFactory.create("greet")

        return circuitBreaker.run(results) {
            Mono.just("fallback")
        }
    }
}

@Service
class FailingService() {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun greet(name: String?): Mono<String> {
        val seconds = (0..10).random()

        name?.map {
            val msg = "Hello ${name} ! this call took $seconds"
            logger.info(msg)
            return Mono.just(msg)
                    .delayElement(Duration.ofSeconds(seconds.toLong()))
        }.isNullOrEmpty().apply {
            return Mono.error(NullPointerException())
        }
    }
}


//@RestController
//class ClientController(private val webClient: WebClient.Builder,
//                       private val reactiveCircuitBreakerFactory: ReactiveCircuitBreakerFactory<*, *>) {
//    @GetMapping("/foo")
//    fun hello(): Mono<String> {
//        return webClient.build()
//                .get().uri { uriBuilder: UriBuilder ->
//                    uriBuilder
//                            .scheme("http")
//                            .host("slow-service").path("/slow")
//                            .build()
//                }
//                .retrieve().bodyToMono(String::class.java)
//                .transform { it: Mono<String>? ->
//                    val rcb = reactiveCircuitBreakerFactory.create("slow")
//                    rcb.run(it) { throwable: Throwable? -> Mono.just("fallback") }
//                }
//    }
//
//}
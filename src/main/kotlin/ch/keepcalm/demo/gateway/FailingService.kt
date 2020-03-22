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
    fun greet(@RequestParam name: String?): Mono<FailingServiceResponse> {
        return reactiveCircuitBreakerFactory.create("greet")
                .run(failingService.greet(name)) {
                    Mono.just(FailingServiceResponse(msg = "Fallback: call took too long."))
                }
    }
}

@Service
class FailingService() {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun greet(name: String?): Mono<FailingServiceResponse> {

        name?.map {
            val seconds = (0..10).random()
            val msg = "Hello ${name} ! this call took $seconds"
            return Mono.just(FailingServiceResponse(msg = msg))
                    .delayElement(Duration.ofSeconds(seconds.toLong()))
                    .doOnNext { logger.info(it.msg) }
        }.isNullOrEmpty().apply {
            return Mono.error(NullPointerException())
        }
    }
}

data class FailingServiceResponse(val msg: String)
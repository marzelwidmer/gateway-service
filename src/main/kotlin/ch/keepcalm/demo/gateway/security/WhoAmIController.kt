package ch.keepcalm.demo.gateway.security

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
class WhoAmIController {
    @GetMapping("/whoami")
    fun me(principal: Mono<Principal>): Mono<Principal> {
        return principal
    }
}

package ch.keepcalm.demo.gateway

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal


@RestController
class Foo {
    @GetMapping(value = ["/api/foo"])
    fun foo() = "Welcome to ROLE_MYHELSANA_MEMBER and ROLE_MYHELSANA_FAVOR"

    @GetMapping(value = ["/light"])
    fun bar() = "Welcome to ROLE_MYHELSANA_LIGHT"

}


@RestController
class GreetController {
    @GetMapping("/me")
    fun greet(principal: Mono<Principal>): Mono<Principal> {
        return principal
    }
}





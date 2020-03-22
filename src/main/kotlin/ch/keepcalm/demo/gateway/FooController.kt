package ch.keepcalm.demo.gateway

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class FooController {
    @GetMapping(value = ["/strong"])
    fun foo() = "Welcome to ROLE_KEEPCALM_MEMBER and ROLE_KEEPCALM_FAVOR"

    @GetMapping(value = ["/light"])
    fun bar() = "Welcome to ROLE_KEEPCALM_LIGHT"

}

package ch.keepcalm.demo.gateway

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
class Api {
    @GetMapping("/api/public")
    fun public() = "Public"

    @GetMapping("/api/member")
    fun member() = "Member"

    @GetMapping("/api/admin")
    fun admin() = "Admin"
}


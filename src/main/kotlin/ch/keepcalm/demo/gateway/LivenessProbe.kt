package ch.keepcalm.demo.gateway

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class LivenessProbe {
    @GetMapping(value = ["/alive"])
    fun alive(): String {
        System.out.println("Date in Europe/Zurich: ${Date().toString()}")
        return "ok"
    }
}

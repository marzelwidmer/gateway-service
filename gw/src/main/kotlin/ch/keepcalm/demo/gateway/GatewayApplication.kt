package ch.keepcalm.demo.gateway

import ch.keepcalm.demo.gateway.security.faketoken.FaketokenProperties
import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity

@EnableConfigurationProperties(JwtSecurityProperties::class, FaketokenProperties::class)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
@SpringBootApplication
@EnableHypermediaSupport(stacks = [WebStack.WEBFLUX], type = [EnableHypermediaSupport.HypermediaType.HAL])
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args) {
        addInitializers(
            beans {

            }
        )
    }
}

package ch.keepcalm.demo.gateway.security.faketoken

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "faketoken")
data class FaketokenProperties(var firstName: String, var lastName: String, var email: String, var subject: String, var roles: String, val language: String)


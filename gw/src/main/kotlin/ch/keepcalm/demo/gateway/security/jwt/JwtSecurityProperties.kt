package ch.keepcalm.demo.gateway.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "security.jwt")
data class JwtSecurityProperties(var issuer: String,var audience: String, var secret: String)

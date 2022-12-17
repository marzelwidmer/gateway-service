package ch.keepcalm.demo.gateway.security.faketoken

import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

data class AccessToken(
        val memberAccessToken: String,
        val adminAccessToken: String
)

@RestController
class Token(private val faketokenProperties: FaketokenProperties, private val jwtSecurityProperties: JwtSecurityProperties) {

    @GetMapping(value = ["/faketoken"])
    fun token() = AccessToken(
        memberAccessToken = generateToken(faketokenProperties.roles),
        adminAccessToken = generateToken("keepcalm.admin")
    )

    private fun generateToken(roles: String) = "Bearer ${
        Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setSubject(faketokenProperties.subject)
            .setIssuedAt(Date())
            .setExpiration(
                Date.from(3600.toLong().let {
                    LocalDateTime.now().plusSeconds(it).atZone(ZoneId.systemDefault()).toInstant()
                })
            )
            .setIssuer(jwtSecurityProperties.issuer)
            .setAudience(jwtSecurityProperties.audience)
            .addClaims(
                mapOf(
                    Pair("firstName", faketokenProperties.firstName),
                    Pair("lastName", faketokenProperties.lastName),
                    Pair("email", faketokenProperties.email),
                    Pair("roles", roles),
                    Pair("language", faketokenProperties.language)
                )
            )
            .signWith(
                SignatureAlgorithm.HS256,
                Base64.getEncoder().encodeToString(jwtSecurityProperties.secret.toByteArray(StandardCharsets.UTF_8))
            ).compact()
    }"
}

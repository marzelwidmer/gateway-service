package ch.keepcalm.demo.gateway.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import java.nio.charset.StandardCharsets
import java.util.*

class JwtTokenVerifier(val audience: String, val issuer: String, secret: String) {

    private val signingKey = Base64.getEncoder().encodeToString(secret.toByteArray(StandardCharsets.UTF_8))

    fun verify(token: String): Jws<Claims> = Jwts.parser()
            .setSigningKey(signingKey)
            .requireAudience(audience)
            .requireIssuer(issuer)
            .parseClaimsJws(token)

}


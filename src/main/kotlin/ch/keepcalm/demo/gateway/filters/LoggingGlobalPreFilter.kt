package ch.keepcalm.demo.gateway.filters

import ch.keepcalm.demo.gateway.security.faketoken.FaketokenProperties
import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class LoggingGlobalPreFilter (private val faketokenProperties: FaketokenProperties, private val jwtSecurityProperties: JwtSecurityProperties) : GlobalFilter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {

        val portalAccount = exchange.request.cookies.getFirst("Navajo")
        println(generateToken(portalAccount?.value.toString()))


        logger.info("Global Pre Filter executed")
        return chain.filter(exchange)
    }


    private fun generateToken(subject: String) = "Bearer ${
        Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setSubject(subject)
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
                    Pair("roles", faketokenProperties.roles),
                    Pair("language", faketokenProperties.language)
                )
            )
            .signWith(
                SignatureAlgorithm.HS256,
                Base64.getEncoder().encodeToString(jwtSecurityProperties.secret.toByteArray(StandardCharsets.UTF_8))
            ).compact()
    }"
}


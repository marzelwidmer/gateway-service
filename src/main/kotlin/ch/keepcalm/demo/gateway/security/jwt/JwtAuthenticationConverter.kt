package ch.keepcalm.demo.gateway.security.jwt

import ch.keepcalm.demo.gateway.security.CustomUserDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class JwtAuthenticationConverter(private val jwtTokenVerifier: JwtTokenVerifier) : ServerAuthenticationConverter {

    companion object {
        private const val BEARER = "Bearer "
        private const val DEFAULT_LANGUAGE = "de"
        private const val AUTHORITIES = "roles"
        private const val PARTNER_NR = "partnernr"
        private const val PARTNER_ID = "partnerid"
        private const val IS_EMPLOYEE = "isemployee"
        private const val ALLOWED_KEYS = "allowedkeys"
        private const val LANGUAGE = "language"
    }

    private val logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)
    private val hasBearerToken = { authValue: String -> authValue.length > BEARER.length && authValue.startsWith(BEARER, true) }
    private val isolateBearerValue = { authValue: String -> authValue.substring(BEARER.length, authValue.length) }

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange)
                .flatMap { webExchange -> extractToken(webExchange) }
                .map { token -> Pair(token, jwtTokenVerifier.verify(token)) }
                .doOnNext { logger.debug("JWT token: {}", it) }
                .map { pairTokenAndClaims -> getAuthentication(pairTokenAndClaims.first) }
                .doOnNext { logger.debug("Authentication created: $it") }
                .doOnError { error -> throw BadCredentialsException("Invalid JWT", error) }
    }


    fun getAuthentication(token: String): Authentication {
        val claims = extractClaims(token)
        val userDetails : UserDetails =  CustomUserDetails(subject = claims.subject,
                password = "",
                partnernr = "${claims[PARTNER_NR]}",
                partnerid = "${claims[PARTNER_ID]}",
                allowedkeys = "${claims[ALLOWED_KEYS]}",
                authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                        claims.get(AUTHORITIES,
                                String::class.java)),
                isemployee = "${claims[IS_EMPLOYEE]}".toBoolean(),
                language = claims.get(LANGUAGE, String::class.java) ?: DEFAULT_LANGUAGE)

        return PreAuthenticatedAuthenticationToken(userDetails, token, userDetails.authorities)

    }

    private fun extractClaims(token: String) = jwtTokenVerifier.verify(token).body

    private fun extractToken(webExchange: ServerWebExchange): Mono<String> {
        val authValue = webExchange.request
                .headers
                .getFirst(HttpHeaders.AUTHORIZATION)

        if (authValue.isNullOrEmpty()) {
            return Mono.empty()
        }

        if (hasBearerToken(authValue)) {
            val token = isolateBearerValue(authValue)
            return Mono.justOrEmpty(token)
        }

        logger.warn("Can't handle Authorization header: $authValue")
        return Mono.empty()
    }
}

package ch.keepcalm.demo.gateway.security.jwt

import ch.keepcalm.demo.gateway.security.CustomUserDetails
import io.jsonwebtoken.Claims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class JwtAuthenticationConverter(private val jwtTokenVerifier: JwtTokenVerifier) : ServerAuthenticationConverter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

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

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange)
                .flatMap { serverWebExchange -> extractToken(serverWebExchange) }
                .map { token -> Pair(token, jwtTokenVerifier.verify(token)) }
                .doOnNext { logger.debug("JWT token: {}", it) }
                .map { pairTokenAndClaims -> getAuthentication(pairTokenAndClaims.first) }
                .doOnNext { logger.debug("Authentication created: $it") }
                .doOnError { error -> throw BadCredentialsException("Invalid JWT", error) }
    }

    private fun extractToken(serverWebExchange: ServerWebExchange): Mono<String> {
        serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION).takeIf {
            StringUtils.hasText(it)
        }?.let { authValue ->
            return Mono.justOrEmpty(extractBearerTokenFromHeader(authValue))
        }
        return Mono.empty()
    }

    private fun extractBearerTokenFromHeader(header: String): String? = header.takeIf {
        header.startsWith("Bearer ")
    }?.apply {
        return header.substring(BEARER.length, header.length)
    }

    private fun getAuthentication(token: String): Authentication {
        val customUserDetails: UserDetails = customUserDetails(claims = extractClaimsFromToken(token= token))
        return PreAuthenticatedAuthenticationToken(customUserDetails, token, customUserDetails.authorities)
    }

    private fun customUserDetails(claims: Claims): UserDetails = CustomUserDetails(subject = claims.subject,
            password = "",
            partnernr = "${claims[PARTNER_NR]}",
            partnerid = "${claims[PARTNER_ID]}",
            allowedkeys = "${claims[ALLOWED_KEYS]}",
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    claims.get(AUTHORITIES,
                            String::class.java)),
            isemployee = "${claims[IS_EMPLOYEE]}".toBoolean(),
            language = claims.get(LANGUAGE, String::class.java) ?: DEFAULT_LANGUAGE)


    private fun extractClaimsFromToken(token: String) = jwtTokenVerifier.verify(token).body
}


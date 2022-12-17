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
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


class JwtAuthenticationConverter(private val jwtTokenVerifier: JwtTokenVerifier) : ServerAuthenticationConverter {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)
        private val AUTHENTICATION_SCHEMA = "Bearer "
        private val DEFAULT_LANGUAGE = "de"
        private val AUTHORITIES = "roles"
        private val LANGUAGE = "language"
    }

    override fun convert(swe: ServerWebExchange): Mono<Authentication> = Mono.justOrEmpty(swe)
            .flatMap { serverWebExchange -> extractBearerTokenfromAuthorizationHeader(serverWebExchange) }
            .doOnNext { LOGGER.debug("--- JWT token: {}", it) }
            .map { token -> Pair(token, jwtTokenVerifier.verify(token)) }
            .doOnNext { LOGGER.debug("--- JWT token verifyed: {}", it) }
            .map { pairTokenAndClaims -> createPreAuthenticatedAuthenticationToken(pairTokenAndClaims.first) }
            .doOnNext { LOGGER.debug("--- Authentication created: $it") }
            .doOnError { error -> throw BadCredentialsException("Invalid JWT", error) }


    /**
     * Extract Bearer token form Authorization Header (Authorization: Bearer eyJhbGciOiJIU.eyJpc3MiOiJIZWxzc.GciOiJIUSGciO)
     *
     * @param serverWebExchange ServerWebExchange
     * @return Mono<String>
     */
    private fun extractBearerTokenfromAuthorizationHeader(serverWebExchange: ServerWebExchange): Mono<String> {
        fun  extractTokenFromBearer(bearerToken: String?) =
                if (bearerToken != null && bearerToken.startsWith(AUTHENTICATION_SCHEMA)) {
                    Mono.justOrEmpty(bearerToken.substring(AUTHENTICATION_SCHEMA.length, bearerToken.length).trim())
                } else Mono.empty()

        return extractTokenFromBearer(serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION).toString()
                .also { LOGGER.trace("--- Found Authorization Header: $it") })
    }


    /**
     * Setup Security Context
     *
     * @param token String
     * @return Authentication
     */
    private fun createPreAuthenticatedAuthenticationToken(token: String): Authentication {
        val customUserDetails: UserDetails = createCustomUserDetailsFromJwtClaims(claims = jwtTokenVerifier.getClamsFromJwt(token = token))
        return PreAuthenticatedAuthenticationToken(customUserDetails, token, customUserDetails.authorities)
    }

    /**
     * Create CustomerUserDetails from JWT Claims
     *
     * @param claims Claims
     * @return UserDetails
     */
    private fun createCustomUserDetailsFromJwtClaims(claims: Claims): UserDetails = CustomUserDetails(subject = claims.subject,
            password = "",
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    claims.get(AUTHORITIES,
                            String::class.java)),
            language = claims.get(LANGUAGE, String::class.java) ?: DEFAULT_LANGUAGE)
}


package ch.keepcalm.demo.gateway.security.jwt

import ch.keepcalm.demo.gateway.security.error.ErrorHandler
import ch.keepcalm.demo.gateway.security.error.HttpErrorHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class JwtAuthenticationFailureHandler(objectMapper: ObjectMapper) : HttpErrorHandler(objectMapper), ServerAuthenticationEntryPoint {

    override fun commence(serverWebExchange: ServerWebExchange, e: AuthenticationException): Mono<Void> {
        return handle(serverWebExchange, ErrorHandler.UNAUTHORIZED_TYPE, HttpStatus.UNAUTHORIZED)
    }
}
package ch.keepcalm.demo.gateway.security.error

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class WebExceptionHandler(private val errorHandler: ErrorHandler, objectMapper: ObjectMapper) :
        HttpErrorHandler(objectMapper), ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        return errorHandler.handle(ex).flatMap { errorResponse -> handle(exchange, errorResponse) }
    }
}
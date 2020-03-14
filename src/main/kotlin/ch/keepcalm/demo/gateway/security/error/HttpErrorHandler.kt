package ch.keepcalm.demo.gateway.security.error

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.Charset

abstract class HttpErrorHandler(private val objectMapper: ObjectMapper) {

    fun handle(serverWebExchange: ServerWebExchange, errorCode: String, httpStatus: HttpStatus): Mono<Void> {
        return handle(serverWebExchange, ErrorResponse(httpStatus, errorCode, httpStatus.reasonPhrase))
    }

    fun handle(exchange: ServerWebExchange, errorResponse: ErrorResponse): Mono<Void> {
        val response = exchange.response
        response.headers.set(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_UTF8_VALUE)
        response.statusCode = errorResponse.httpStatus

        val errorJson = objectMapper.writeValueAsString(errorResponse)
        val dataBufferFactory = response.bufferFactory()
        val buffer = dataBufferFactory.wrap(errorJson.toByteArray(Charset.defaultCharset()))

        return response.writeWith(Mono.just(buffer)).doOnError { DataBufferUtils.release(buffer) }
    }
}
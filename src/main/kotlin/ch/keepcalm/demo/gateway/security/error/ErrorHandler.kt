package ch.keepcalm.demo.gateway.security.error

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just

class ErrorHandler(private val objectMapper: ObjectMapper) {

    fun handle(error: Throwable): Mono<ErrorResponse> {
        when (error) {
            is AccessDeniedException -> return forbidden()
            is BadCredentialsException -> return unauthorized()
            is WebClientResponseException -> return downstreamServiceFailed(error)
            is WebClientException -> return downstreamServiceUnavailable(error)
        }

        return internalError(error)
    }

    fun handleAndRespond(error: Throwable): Mono<ServerResponse> {
        when (error) {
            is AccessDeniedException -> return toResponse(HttpStatus.FORBIDDEN, forbidden())
            is BadCredentialsException -> return toResponse(HttpStatus.UNAUTHORIZED, unauthorized())
            is WebClientResponseException -> return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, downstreamServiceFailed(error))
            is WebClientException -> return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, downstreamServiceUnavailable(error))
        }

        return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, internalError(error))
    }

    fun notFound(): Mono<ErrorResponse> {
        return just(ErrorResponse(HttpStatus.NOT_FOUND, NOT_FOUND_TYPE, HttpStatus.NOT_FOUND.reasonPhrase))
    }

    fun forbidden(): Mono<ErrorResponse> {
        return just(ErrorResponse(HttpStatus.FORBIDDEN, FORBIDDEN_TYPE, "Access Denied"))
    }

    fun unauthorized(): Mono<ErrorResponse> {
        return just(ErrorResponse(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_TYPE, HttpStatus.UNAUTHORIZED.reasonPhrase))
    }

    private fun downstreamServiceFailed(error: WebClientResponseException): Mono<ErrorResponse> {
        LOG.error("<<< ${error.statusCode}: ${error.message}")
        var details: Any = error.responseBodyAsString
        if (MediaType.APPLICATION_JSON.isCompatibleWith(error.headers.contentType)) {
            val json = objectMapper.readTree(error.responseBodyAsString)
            details = json
        }
        LOG.error("<<< ${error.message}")
        LOG.error("<<< $details")
        return just(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_TYPE, error.message!!, details))
    }

    private fun downstreamServiceUnavailable(error: WebClientException): Mono<ErrorResponse> {
        LOG.error(">>> ${error.message}")
        return just(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_TYPE, error.message!!))
    }

    private fun internalError(error: Throwable): Mono<ErrorResponse> {
        LOG.error(error.message, error)
        return just(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_TYPE, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase))
    }

    private fun toResponse(httpStatus: HttpStatus, monoError: Mono<ErrorResponse>): Mono<ServerResponse> {
        return monoError
                .flatMap({ errorResponse ->
                    ServerResponse
                            .status(httpStatus)
                            .body(just(errorResponse), ErrorResponse::class.java)
                })
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ErrorHandler::class.java)

        const val INTERNAL_ERROR_TYPE = "https://keepcalm.ch/problem/internal-error"
        const val NOT_FOUND_TYPE = "https://keepcalm.ch/problem/not-found"
        const val UNAUTHORIZED_TYPE = "https://keepcalm.ch/problem/unauthorized"
        const val FORBIDDEN_TYPE = "https://keepcalm.ch/problem/forbidden"
    }
}
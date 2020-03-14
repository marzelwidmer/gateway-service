package ch.keepcalm.demo.gateway.security.error

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

class ErrorResponse @JsonCreator constructor(@JsonIgnore val httpStatus: HttpStatus,
                                             @param:JsonProperty("type") val type: String,
                                             @param:JsonProperty("title") val title: String,
                                             @param:JsonProperty("detail") val detail: Any? = null) {

    val status = httpStatus.value()
}

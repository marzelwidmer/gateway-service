package ch.keepcalm.demo.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.path
import org.springframework.web.reactive.function.server.RouterFunctions.route
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
class GatewayApplication {
	@PostConstruct
	fun init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"))
		System.out.println("Date in Europe/Zurich: ${Date().toString()}")
	}
}

fun main(args: Array<String>) {
	runApplication<GatewayApplication>(*args)
}



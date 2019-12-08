package ch.keepcalm.demo.gateway.routes

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class KotlinRoutes {
    @Bean
    fun kotlinBasedRoutes(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator =
            routeLocatorBuilder.routes {
                route {
                    path("/kotlin/**")
                    filters { stripPrefix(1) }
                    uri("http://httpbin.org")
                }
            }
}
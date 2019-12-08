package ch.keepcalm.demo.gateway.routes

import ch.keepcalm.demo.gateway.filters.LoggingGatewayFilterFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.*
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

    @Bean
    fun orderRoutes(
            builder: RouteLocatorBuilder,
            loggingFactory: LoggingGatewayFilterFactory): RouteLocator? {
        return builder.routes()
                .route("service_route_kotlin_config") { r: PredicateSpec ->
                    r.path("/order/**")
                            .filters { filter: GatewayFilterSpec ->
                                filter.rewritePath("order(?<segment>/?.*)", "$\\{segment}")
                                        .filter(loggingFactory.apply(
                                                LoggingGatewayFilterFactory.Config("My Custom Message", true, true)))
                            }
                            .uri("http://localhost:8082")
                }
                .build()
    }
}


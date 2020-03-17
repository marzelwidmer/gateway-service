package ch.keepcalm.demo.gateway.routes

import ch.keepcalm.demo.gateway.filters.LoggingGatewayFilterFactory
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.*
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class AdditionalRoutes {

    @Bean
    fun kotlinBasedRoutes(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator =
            routeLocatorBuilder.routes {
                route("kotlin") {
                    path("/kotlin/**")
                    filters { stripPrefix(1) }
                    uri("http://httpbin.org")
                }
            }

    @Bean
    fun additionalRouteLocator(builder: RouteLocatorBuilder, ctx: ApplicationContext, redisRateLimiter: RedisRateLimiter) = builder.routes {
        route(id = "test-kotlin") {
            path("/test-kotlin")
            filters {
                stripPrefix(1)
                addResponseHeader("X-TestHeader", "foobar")
                requestRateLimiter {
                    it.rateLimiter = redisRateLimiter

                }
            }
            uri("http://httpbin.org")
        }
    }


    @Bean
    fun catalogRoutesK8s(
            builder: RouteLocatorBuilder,
            loggingFactory: LoggingGatewayFilterFactory): RouteLocator? {
        return builder.routes()
                .route("service_route_kotlin_config") { r: PredicateSpec ->
                    r.path("/catalog/**")
                            .filters { filter: GatewayFilterSpec ->
                                filter.rewritePath("catalog(?<segment>/?.*)", "$\\{segment}")
                                        .filter(loggingFactory.apply(
                                                LoggingGatewayFilterFactory.Config("My Custom Message", true, true)))
                            }
                            .uri("http://catalog-service.dev.svc:8080")
                }
                .build()
    }

    @Bean
    fun customerRoutesK8s(
            builder: RouteLocatorBuilder,
            loggingFactory: LoggingGatewayFilterFactory): RouteLocator? {
        return builder.routes()
                .route("service_route_kotlin_config") { r: PredicateSpec ->
                    r.path("/customer/**")
                            .filters { filter: GatewayFilterSpec ->
                                filter.rewritePath("customer(?<segment>/?.*)", "$\\{segment}")
                                        .filter(loggingFactory.apply(
                                                LoggingGatewayFilterFactory.Config("My Custom Message", true, true)))
                            }
                            .uri("http://customer-service.dev.svc:8080")
                }
                .build()
    }

    @Bean
    fun orderRoutesK8s(
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
                            .uri("http://order-service.dev.svc:8080")
                }
                .build()
    }
}



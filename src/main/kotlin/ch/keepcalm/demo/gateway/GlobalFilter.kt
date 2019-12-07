//package ch.keepcalm.demo.gateway
//
//import org.springframework.cloud.gateway.filter.GatewayFilter
//import org.springframework.cloud.gateway.filter.GatewayFilterChain
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import java.util.*
//
//
//@Component
//class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilterConfig>() {
//
//    override fun apply(config: GlobalFilterConfig?): GatewayFilter {
//        println("inside GlobalFilter.apply method")
//        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
//            val request: ServerHttpRequest = exchange.request.mutate().header("scgw-global-header", UUID.randomUUID().toString()).build()
//            chain.filter(exchange.mutate().request(request).build())
//        }
//    }
//}
//
//data class GlobalFilterConfig(var name: String? = null)
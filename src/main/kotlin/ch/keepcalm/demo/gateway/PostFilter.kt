//package ch.keepcalm.demo.gateway
//
//import org.springframework.cloud.gateway.filter.GatewayFilter
//import org.springframework.cloud.gateway.filter.GatewayFilterChain
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import reactor.core.publisher.Mono
//import java.util.*
//
//
//@Component
//class PostFilter : AbstractGatewayFilterFactory<PostFilterConfig>() {
//
//    override fun apply(config: PostFilterConfig): GatewayFilter {
//        println("inside PostFilter.apply method...")
//        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
//            chain.filter(exchange).then(Mono.fromRunnable {
//                val response = exchange.response
//                val headers = response.headers
//                headers.forEach { k: String, v: List<String?> -> println("$k : $v") }
//            })
//        }
//    }
//}
//
//data class PostFilterConfig(var name: String? = null)

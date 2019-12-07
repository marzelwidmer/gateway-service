//package ch.keepcalm.demo.gateway
//
//import org.springframework.cloud.gateway.filter.GatewayFilter
//import org.springframework.cloud.gateway.filter.GatewayFilterChain
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import reactor.core.publisher.Mono
//import java.util.*
//
//
//@Component
//class PreFilter : AbstractGatewayFilterFactory<PreFilterConfig>() {
//
//    override fun apply(config: PreFilterConfig): GatewayFilter {
//        println("inside SCGWPreFilter.apply method")
//        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
//            val request = exchange.request.mutate().header("scgw-pre-header",  UUID.randomUUID().toString()).build()
//            chain.filter(exchange.mutate().request(request).build())
//        }
//    }
//}
//
//data class PreFilterConfig(var name: String? = null)

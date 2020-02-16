package ch.keepcalm.demo.gateway

import ch.sbb.esta.openshift.gracefullshutdown.GracefulshutdownSpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.annotation.PostConstruct
import io.jaegertracing.internal.samplers.ConstSampler

@SpringBootApplication
@EnableDiscoveryClient
class GatewayApplication {
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"))
        System.out.println("Date in Europe/Zurich: ${Date().toString()}")
    }
}

fun main(args: Array<String>) {
    GracefulshutdownSpringApplication.run(GatewayApplication::class.java, *args)
}


@RestController
class LivenessProbe {
    @GetMapping(value = ["/alive"])
    fun alive() = "ok"
}


@Component
class TracerConfiguration {

    @Bean
    fun jaegerTracer(): io.jaegertracing.Configuration = io.jaegertracing.Configuration("catalog-service")
            .withSampler(io.jaegertracing.Configuration.SamplerConfiguration
                    .fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1))
            .withReporter(io.jaegertracing.Configuration.ReporterConfiguration
                    .fromEnv()
                    .withLogSpans(true))
}
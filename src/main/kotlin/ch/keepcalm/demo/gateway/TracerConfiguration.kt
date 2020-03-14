package ch.keepcalm.demo.gateway

import io.jaegertracing.internal.samplers.ConstSampler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


@Component
class TracerConfiguration {
    @Bean
    fun jaegerTracer(): io.jaegertracing.Configuration = io.jaegertracing.Configuration("gateway-service")
            .withSampler(io.jaegertracing.Configuration.SamplerConfiguration
                    .fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1))
            .withReporter(io.jaegertracing.Configuration.ReporterConfiguration
                    .fromEnv()
                    .withLogSpans(true))
}
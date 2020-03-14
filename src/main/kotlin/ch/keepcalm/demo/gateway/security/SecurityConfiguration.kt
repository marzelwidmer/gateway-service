package ch.keepcalm.demo.gateway.security

import ch.keepcalm.demo.gateway.security.error.ErrorHandler
import ch.keepcalm.demo.gateway.security.error.WebExceptionHandler
import ch.keepcalm.demo.gateway.security.jwt.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository


const val ROLE_KEEPCALM_FAVOR = "keepcalm.user" // FAVOR
const val ROLE_KEEPCALM_MEMBER = "keepcalm.mitglied" // Mitglied
const val ROLE_KEEPCALM_LIGHT = "keepcalm.light" // Light
const val ROLE_ACTUATOR = "ACTUATOR"


@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtSecurityProperties::class)
class SecurityConfiguration {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity, apiAuthenticationWebFilter: AuthenticationWebFilter,
                                  jwtAuthenticationFailureHandler: JwtAuthenticationFailureHandler): SecurityWebFilterChain {
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .addFilterAt(apiAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(Customizer { exchanges: AuthorizeExchangeSpec ->
                    exchanges
                            .pathMatchers("/alive").permitAll()
                            .pathMatchers("/me").permitAll()
                            .pathMatchers("/actuator/health").permitAll()
                            .pathMatchers("/light/**").hasAnyAuthority(ROLE_KEEPCALM_LIGHT)
                            .pathMatchers("/api/**").hasAnyAuthority(ROLE_KEEPCALM_FAVOR, ROLE_KEEPCALM_MEMBER)
                }
                )
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationFailureHandler)
                .and()
        return http.build()
    }

    @Bean
    fun jwtTokenVerifier(jwtSecurityProperties: JwtSecurityProperties): JwtTokenVerifier = JwtTokenVerifier(jwtSecurityProperties.audience, jwtSecurityProperties.issuer, jwtSecurityProperties.secret)

    @Bean
    fun securityContextRepository(): NoOpServerSecurityContextRepository = NoOpServerSecurityContextRepository.getInstance()

    @Bean
    fun jwtAuthenticationFailureHandler(objectMapper: ObjectMapper): JwtAuthenticationFailureHandler = JwtAuthenticationFailureHandler(objectMapper)

    @Bean
    fun jwtAuthenticationConverter(jwtTokenVerifier: JwtTokenVerifier): JwtAuthenticationConverter = JwtAuthenticationConverter(jwtTokenVerifier)

    @Bean
    fun apiAuthenticationWebFilter(
            jwtAuthenticationFailureHandler: JwtAuthenticationFailureHandler,
            jwtAuthenticationConverter: JwtAuthenticationConverter): AuthenticationWebFilter {

        val apiAuthenticationWebFilter = AuthenticationWebFilter(JwtAuthenticationManager())

        apiAuthenticationWebFilter.setAuthenticationFailureHandler(ServerAuthenticationEntryPointFailureHandler(jwtAuthenticationFailureHandler))
        apiAuthenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)
        apiAuthenticationWebFilter.setSecurityContextRepository(securityContextRepository())
        return apiAuthenticationWebFilter
    }

    @Bean
    fun errorHandler(objectMapper: ObjectMapper): ErrorHandler = ErrorHandler(objectMapper)

    @Bean
    fun webExceptionHandler(errorHandler: ErrorHandler, objectMapper: ObjectMapper): ErrorWebExceptionHandler = WebExceptionHandler(errorHandler, objectMapper)
}

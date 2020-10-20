package ch.keepcalm.demo.gateway.security

import ch.keepcalm.demo.gateway.security.jwt.JwtAuthenticationConverter
import ch.keepcalm.demo.gateway.security.jwt.JwtAuthenticationManager
import ch.keepcalm.demo.gateway.security.jwt.JwtSecurityProperties
import ch.keepcalm.demo.gateway.security.jwt.JwtTokenVerifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport

@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtSecurityProperties::class)
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(private val problemSupport: SecurityProblemSupport) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity, apiAuthenticationWebFilter: AuthenticationWebFilter): SecurityWebFilterChain {
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .addFilterAt(apiAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange()
                    .pathMatchers("/faketoken").permitAll()
                    .pathMatchers("/alive").permitAll()
                    .pathMatchers("/whoami").permitAll()
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/api/public/**").permitAll()
                    .pathMatchers("/api/member/**").hasAnyAuthority(ROLE_MEMBER)
                    .pathMatchers("/api/admin/**").hasAnyAuthority(ROLE_ADMIN)
                .anyExchange().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        return http.build()
    }

    @Bean
    fun jwtTokenVerifier(jwtSecurityProperties: JwtSecurityProperties): JwtTokenVerifier = JwtTokenVerifier(jwtSecurityProperties.audience, jwtSecurityProperties.issuer, jwtSecurityProperties.secret)

    @Bean
    fun securityContextRepository(): NoOpServerSecurityContextRepository = NoOpServerSecurityContextRepository.getInstance()

    @Bean
    fun jwtAuthenticationConverter(jwtTokenVerifier: JwtTokenVerifier): JwtAuthenticationConverter = JwtAuthenticationConverter(jwtTokenVerifier)

    @Bean
    fun apiAuthenticationWebFilter(
            jwtAuthenticationConverter: JwtAuthenticationConverter): AuthenticationWebFilter {
        val apiAuthenticationWebFilter = AuthenticationWebFilter(JwtAuthenticationManager())
        apiAuthenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)
        apiAuthenticationWebFilter.setSecurityContextRepository(securityContextRepository())
        return apiAuthenticationWebFilter
    }
}

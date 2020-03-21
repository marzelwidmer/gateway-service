package ch.keepcalm.demo.gateway

import org.springframework.web.bind.annotation.ControllerAdvice
import org.zalando.problem.spring.webflux.advice.ProblemHandling
import org.zalando.problem.spring.webflux.advice.security.SecurityAdviceTrait

@ControllerAdvice
class ExceptionHandler : SecurityAdviceTrait, ProblemHandling
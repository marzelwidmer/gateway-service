package ch.keepcalm.demo.gateway

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GatewayCircuitBreakerTest {
    @Autowired
    var template: TestRestTemplate? = null
    var i = 0

    @Disabled
    @Test
    @RepeatedTest(1)
    fun testFailService() {
        val gen = 1 + i++ % 2
//        val r: ResponseEntity<*> = template!!.exchange("/greet?name=foo", HttpMethod.GET, null, String::class.java, gen)
        val r: ResponseEntity<*> = template!!.exchange("/greet", HttpMethod.GET, null, String::class.java, gen)
        logger.info("{}. Received: status->{}, payload->{}, call->{}", i, r.statusCodeValue, r.body, gen)
    }


    private val logger = LoggerFactory.getLogger(GatewayCircuitBreakerTest::class.java)
}
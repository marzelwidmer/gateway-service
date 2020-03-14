package ch.keepcalm.demo.gateway.util


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun main(args: Array<String>) {
             
    println("Welcome to JWT token generator....")
    println("subject : [A1002593]")
    val subject = readLine()?.ifBlank { Token().subject }

    println("vorname : [John]")
    val firstName = readLine()?.ifBlank { Token().firstName }

    println("name : [Keepcalm AG]")
    val name = readLine()?.ifBlank { Token().name }

    println("roles : [keepcalm.user, keepcalm.mitglied, keepcalm.light]")
    val roles = readLine()?.ifBlank { Token().roles }

    println("issuer : [Keepcalm Auth Portal]")
    val issuer = readLine()?.ifBlank { Token().issuer }

    println("audience : [Keepcalm]")
    val audience = readLine()?.ifBlank { Token().audience }

    println("secret : [willbereplacedinalaterversiononceRSAcanbeused]")
    val secret = readLine()?.ifBlank { Token().secret }.toString()

    println("partnerNr : [M60011484]")
    val partnerNr = readLine()?.ifBlank { Token().partnerNr }

    println("partnerId : [60011484]")
    val partnerId = readLine()?.ifBlank { Token().partnerId }

    println("userEmail : [keepcalm@c3smonkey.ch]")
    val userEmail = readLine()?.ifBlank { Token().userEmail }

    println("allowedkeys : [60011484]")
    val allowedkeys = readLine()?.ifBlank { Token().allowedkeys }

    println("isEmployee : [false]")
    val isEmployee = readLine()?.toBoolean() ?: Token().isEmployee

    println("language : [de]")
    val language = readLine()?.ifBlank { Token().language }

    println("expiration : [3600]")
    val expiration = readLine()?.toIntOrNull() ?: Token().expiration

    val token = Token(subject = subject,
            firstName = firstName,
            isEmployee = isEmployee,
            language = language,
            name = name,
            roles = roles,
            issuer = issuer,
            audience = audience,
            secret = secret,
            partnerNr = partnerNr,
            partnerId = partnerId,
            userEmail = userEmail,
            allowedkeys = allowedkeys,
            expiration = expiration)

    val generatedToken = generateToken(token)
    println("###############################")
    println(" \n \"${generatedToken}\" \n")
    println("###############################")

}




fun generateToken(token: Token) =
        Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(token.subject)
                .setIssuedAt(Date())
                .setExpiration(
                        Date.from(token.expiration.toLong().let {
                            LocalDateTime.now().plusSeconds(it).atZone(ZoneId.systemDefault()).toInstant()
                        })
                )
                .setIssuer(token.issuer)
                .setAudience(token.audience)
                .addClaims(
                        mapOf(
                                Pair("language", token.language),
                                Pair("name", token.name),
                                Pair("firstName", token.firstName),
                                Pair("isemployee", token.isEmployee),
                                Pair("email", token.userEmail),
                                Pair("roles", token.roles),
                                Pair("partnerid", token.partnerId),
                                Pair("partnernr", token.partnerNr),
                                Pair("allowedkeys", token.allowedkeys)
                        )
                )
                .signWith(
                        SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(token.secret.toByteArray(StandardCharsets.UTF_8))
                ).compact()


data class Token(
        var language: String? = "de",
        var firstName: String? = "John",
        var isEmployee: Boolean? = false,
        var name: String? = "Keepcalm AG",
        var subject: String? = "A1002593",
        var roles: String? = "keepcalm.user",
        var issuer: String? = "Keepcalm Auth Portal",
        var audience: String? = "Keepcalm",
        var secret: String = "willbereplacedinalaterversiononceRSAcanbeused",
        var partnerNr: String? = "M60011484",
        var partnerId: String? = "60011484",
        var userEmail: String? = "keepcalm@c3smonkey.ch",
        var allowedkeys: String? = "60011484",
        var expiration: Int = 3600
)



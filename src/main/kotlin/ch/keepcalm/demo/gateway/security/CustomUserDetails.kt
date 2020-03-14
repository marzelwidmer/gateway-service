package ch.keepcalm.demo.gateway.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUserDetails(val subject: String?,
                        password: String?,
                        authorities: List<GrantedAuthority>,
                        val partnernr: String,
                        val partnerid: String,
                        val isemployee: Boolean = false,
                        val allowedkeys: String,
                        val language: String = "de") : User(subject, password, authorities) {

    override fun toString(): String {
        return "oId:${this.subject} pId:${partnerid} aId:${allowedkeys}"
    }
}
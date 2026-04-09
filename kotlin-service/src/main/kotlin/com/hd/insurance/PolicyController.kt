package com.hd.insurance

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Policy(
    val id: String,
    val holder: String,
    val type: String,
    val premium: Double,
    val status: String
)

@RestController
@RequestMapping("/policies")
class PolicyController {

    private val log = LoggerFactory.getLogger(PolicyController::class.java)

    private val policies = listOf(
        Policy("POL-001", "Nikos Papadopoulos", "Motor", 450.00, "active"),
        Policy("POL-002", "Maria Georgiou",     "Home",  820.00, "active"),
        Policy("POL-003", "Yannis Stavros",     "Life",  1200.00, "active"),
        Policy("POL-004", "Elena Kostas",       "Motor", 390.00, "expired"),
        Policy("POL-005", "Dimitris Alexiou",   "Home",  760.00, "active"),
    )

    @GetMapping
    fun listPolicies(): List<Policy> {
        log.info("Fetching all policies, count=${policies.size}")
        Thread.sleep((50..150).random().toLong()) // simulate DB lookup
        return policies
    }

    @GetMapping("/{id}")
    fun getPolicy(@PathVariable id: String): ResponseEntity<Policy> {
        log.info("Fetching policy id=$id")
        Thread.sleep((30..100).random().toLong()) // simulate DB lookup
        val policy = policies.find { it.id == id }
        return if (policy != null) {
            ResponseEntity.ok(policy)
        } else {
            log.warn("Policy not found id=$id")
            ResponseEntity.notFound().build()
        }
    }
}

package com.hd.insurance

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import kotlin.random.Random

data class ClaimRequest(
    val policyId: String,
    val description: String = "",
    val estimatedAmount: Double
)

data class ClaimResponse(
    val claimId: String,
    val policyId: String,
    val status: String,
    val message: String
)

data class ErrorResponse(val detail: String)

private val POLICY_STATUS = mapOf(
    "POL-001" to "active",
    "POL-002" to "active",
    "POL-003" to "active",
    "POL-004" to "expired",
    "POL-005" to "active",
)

@RestController
@RequestMapping("/claims")
class ClaimsController {

    private val log = LoggerFactory.getLogger(ClaimsController::class.java)

    @PostMapping
    fun submitClaim(@RequestBody request: ClaimRequest): ResponseEntity<Any> {
        // Validate amount
        if (request.estimatedAmount > 10000) {
            log.error("Claim rejected: amount exceeds policy limit policyId=${request.policyId} amount=${request.estimatedAmount}")
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse("Estimated amount exceeds policy limit of €10,000"))
        }

        // Check policy exists
        val status = POLICY_STATUS[request.policyId]
        if (status == null) {
            log.error("Claim rejected: policy not found policyId=${request.policyId}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse("Policy ${request.policyId} not found"))
        }

        // Check policy is active
        if (status == "expired") {
            log.error("Claim rejected: policy expired policyId=${request.policyId}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse("Policy ${request.policyId} is expired and cannot be used for new claims"))
        }

        // Simulate intermittent payment gateway failure (20%)
        if (Random.nextDouble() < 0.20) {
            log.error("Payment gateway unavailable policyId=${request.policyId} amount=${request.estimatedAmount}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("Payment gateway unavailable, please try again later"))
        }

        val claimId = "CLM-${UUID.randomUUID().toString().take(8).uppercase()}"
        log.info("Submitting claim claimId=$claimId policyId=${request.policyId} amount=${request.estimatedAmount}")
        Thread.sleep((100..300).random().toLong())
        log.info("Claim accepted claimId=$claimId")

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ClaimResponse(
                claimId = claimId,
                policyId = request.policyId,
                status = "pending_review",
                message = "Your claim has been received and is under review."
            )
        )
    }
}

package com.hellas.insurance

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class ClaimRequest(
    val policyId: String,
    val description: String,
    val estimatedAmount: Double
)

data class ClaimResponse(
    val claimId: String,
    val policyId: String,
    val status: String,
    val message: String
)

@RestController
@RequestMapping("/claims")
class ClaimsController {

    private val log = LoggerFactory.getLogger(ClaimsController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun submitClaim(@RequestBody request: ClaimRequest): ClaimResponse {
        val claimId = "CLM-${UUID.randomUUID().toString().take(8).uppercase()}"
        log.info("Submitting claim claimId=$claimId policyId=${request.policyId} amount=${request.estimatedAmount}")

        Thread.sleep((100..300).random().toLong()) // simulate processing

        log.info("Claim accepted claimId=$claimId")
        return ClaimResponse(
            claimId = claimId,
            policyId = request.policyId,
            status = "pending_review",
            message = "Your claim has been received and is under review."
        )
    }
}

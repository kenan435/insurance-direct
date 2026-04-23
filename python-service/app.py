import logging
import random
import time
import uuid
from flask import Flask, jsonify, request

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

app = Flask(__name__)

POLICIES = [
    {"id": "POL-001", "holder": "Nikos Papadopoulos", "type": "Motor",  "premium": 450.00,  "status": "active"},
    {"id": "POL-002", "holder": "Maria Georgiou",     "type": "Home",   "premium": 820.00,  "status": "active"},
    {"id": "POL-003", "holder": "Yannis Stavros",     "type": "Life",   "premium": 1200.00, "status": "active"},
    {"id": "POL-004", "holder": "Elena Kostas",       "type": "Motor",  "premium": 390.00,  "status": "expired"},
    {"id": "POL-005", "holder": "Dimitris Alexiou",   "type": "Home",   "premium": 760.00,  "status": "active"},
]


@app.get("/policies")
def list_policies():
    log.info(f"Fetching all policies count={len(POLICIES)}")
    time.sleep(random.uniform(0.05, 0.15))
    return jsonify(POLICIES)


@app.get("/policies/<policy_id>")
def get_policy(policy_id):
    log.info(f"Fetching policy id={policy_id}")
    time.sleep(random.uniform(0.03, 0.10))
    policy = next((p for p in POLICIES if p["id"] == policy_id), None)
    if not policy:
        log.warning(f"Policy not found id={policy_id}")
        return jsonify({"detail": "Policy not found"}), 404
    return jsonify(policy)


@app.post("/claims")
def submit_claim():
    body = request.get_json()
    policy_id = body.get("policyId")
    amount = body.get("estimatedAmount", 0)

    # Validate amount
    if amount > 10000:
        log.error(f"Claim rejected: amount exceeds policy limit policy_id={policy_id} amount={amount}")
        return jsonify({"detail": "Estimated amount exceeds policy limit of €10,000"}), 422

    # Check policy exists
    policy = next((p for p in POLICIES if p["id"] == policy_id), None)
    if not policy:
        log.error(f"Claim rejected: policy not found policy_id={policy_id}")
        return jsonify({"detail": f"Policy {policy_id} not found"}), 404

    # Check policy is active
    if policy["status"] == "expired":
        log.error(f"Claim rejected: policy expired policy_id={policy_id}")
        return jsonify({"detail": f"Policy {policy_id} is expired and cannot be used for new claims"}), 400

    # Simulate intermittent payment gateway failure (20%)
    if random.random() < 0.20:
        log.error(f"Payment gateway unavailable policy_id={policy_id} amount={amount}")
        return jsonify({"detail": "Payment gateway unavailable, please try again later"}), 500

    claim_id = f"CLM-{str(uuid.uuid4())[:8].upper()}"
    log.info(f"Submitting claim claim_id={claim_id} policy_id={policy_id} amount={amount}")
    time.sleep(random.uniform(0.10, 0.30))
    log.info(f"Claim accepted claim_id={claim_id}")
    return jsonify({
        "claimId": claim_id,
        "policyId": policy_id,
        "status": "pending_review",
        "message": "Your claim has been received and is under review.",
    }), 201

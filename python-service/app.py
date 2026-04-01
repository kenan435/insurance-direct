import logging
import random
import time
import uuid
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

app = FastAPI()

POLICIES = [
    {"id": "POL-001", "holder": "Nikos Papadopoulos", "type": "Motor",  "premium": 450.00, "status": "active"},
    {"id": "POL-002", "holder": "Maria Georgiou",     "type": "Home",   "premium": 820.00, "status": "active"},
    {"id": "POL-003", "holder": "Yannis Stavros",     "type": "Life",   "premium": 1200.00,"status": "active"},
    {"id": "POL-004", "holder": "Elena Kostas",       "type": "Motor",  "premium": 390.00, "status": "expired"},
    {"id": "POL-005", "holder": "Dimitris Alexiou",   "type": "Home",   "premium": 760.00, "status": "active"},
]


class ClaimRequest(BaseModel):
    policyId: str
    description: str
    estimatedAmount: float


@app.get("/policies")
def list_policies():
    log.info(f"Fetching all policies count={len(POLICIES)}")
    time.sleep(random.uniform(0.05, 0.15))
    return POLICIES


@app.get("/policies/{policy_id}")
def get_policy(policy_id: str):
    log.info(f"Fetching policy id={policy_id}")
    time.sleep(random.uniform(0.03, 0.10))
    policy = next((p for p in POLICIES if p["id"] == policy_id), None)
    if not policy:
        log.warning(f"Policy not found id={policy_id}")
        raise HTTPException(status_code=404, detail="Policy not found")
    return policy


@app.post("/claims", status_code=201)
def submit_claim(request: ClaimRequest):
    claim_id = f"CLM-{str(uuid.uuid4())[:8].upper()}"
    log.info(f"Submitting claim claim_id={claim_id} policy_id={request.policyId} amount={request.estimatedAmount}")
    time.sleep(random.uniform(0.10, 0.30))
    log.info(f"Claim accepted claim_id={claim_id}")
    return {
        "claimId": claim_id,
        "policyId": request.policyId,
        "status": "pending_review",
        "message": "Your claim has been received and is under review.",
    }

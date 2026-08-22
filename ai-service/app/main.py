from fastapi import FastAPI

app = FastAPI(
    title="PayPilot AI Service",
    description="AI and agent service for PayPilot",
    version="1.0.0",
)


@app.get("/api/v1/health")
def health():
    return {
        "service": "paypilot-ai",
        "status": "UP",
        "version": "v1",
    }
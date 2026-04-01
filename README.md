# Hellas Direct — Coralogix OTel POC

Reference implementation for instrumenting Kubernetes services (Kotlin/JVM + Python) with OpenTelemetry and shipping all three signals — traces, metrics, and logs — to Coralogix.

## How it works

```
App Pod  →  OTel Collector DaemonSet (same node, port 4317)  →  Tail Sampling Gateway  →  Coralogix
```

No code changes required. Instrumentation is done entirely via the Java agent (Kotlin) and `opentelemetry-instrument` wrapper (Python), configured through environment variables.

## Quick start

| Step | What |
|---|---|
| 1 | [Deploy the collector](docs/instrumentation-guide.md#part-1-deploy-the-coralogix-collector) — Helm chart, 3 commands |
| 2 | [Instrument your app](docs/instrumentation-guide.md#part-2-instrument-your-applications) — env vars only, no code changes |
| 3 | [Query your data](docs/dataprime-queries.md) — copy-paste DataPrime queries for logs + traces |

## Docs

- **[instrumentation-guide.md](docs/instrumentation-guide.md)** — full collector setup, Kotlin and Python instrumentation, collector architecture and features
- **[dataprime-queries.md](docs/dataprime-queries.md)** — sample DataPrime queries validated against live data, field reference

## Reference services

| Directory | Language | Framework |
|---|---|---|
| [`kotlin-service/`](./kotlin-service/) | Kotlin (JVM) | Spring Boot |
| [`python-service/`](./python-service/) | Python | FastAPI |
| [`collector/values.yaml`](./collector/values.yaml) | Helm values | Coralogix otel-integration |

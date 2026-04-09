# Hellas Direct — OpenTelemetry POC

This repo contains reference instrumentation examples for sending traces, metrics, and logs from your Kubernetes services to Coralogix via OpenTelemetry.

## Overview

This repo will be walked through during the integration call. It contains reference service implementations showing how traces, metrics, and logs flow from your Kubernetes applications to Coralogix via OpenTelemetry.

The collector setup, instrumentation, and querying will all be covered live during the call.

---

## How it works

```
Your App Pod  →  OTel Agent (DaemonSet, same node)  →  Gateway (tail sampling, 3 replicas)  →  Coralogix
```

The app uses the node's IP (injected via the Kubernetes Downward API) to reach the collector:

```yaml
- name: OTEL_IP
  valueFrom:
    fieldRef:
      fieldPath: status.hostIP
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: http://$(OTEL_IP):4317
```

---

## Kotlin / Java (JVM)

Instrumentation is done via the [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest) — no code changes required.

### 1. Add the agent to your Docker image

```dockerfile
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
```

### 2. Add env vars to your K8s deployment

```yaml
env:
  - name: OTEL_IP
    valueFrom:
      fieldRef:
        fieldPath: status.hostIP
  - name: OTEL_EXPORTER_OTLP_ENDPOINT
    value: http://$(OTEL_IP):4317
  - name: OTEL_SERVICE_NAME
    value: your-service-name
  - name: OTEL_SERVICE_VERSION
    value: "1.0.0"
  - name: OTEL_RESOURCE_ATTRIBUTES
    value: cx.application.name=hellas,cx.subsystem.name=your-service-name
  - name: OTEL_EXPORTER_OTLP_PROTOCOL
    value: grpc
```

> `cx.application.name` and `cx.subsystem.name` are how your service appears in Coralogix. Set them to something meaningful.

See the full example in [`kotlin-service/k8s/deployment.yaml`](./kotlin-service/k8s/deployment.yaml).

---

## Python

Python uses the `opentelemetry-distro` package for zero-code auto-instrumentation.

### 1. Add dependencies to your image

```dockerfile
RUN pip install opentelemetry-distro opentelemetry-exporter-otlp
RUN opentelemetry-bootstrap -a install
```

### 2. Run your app via `opentelemetry-instrument`

```dockerfile
CMD ["opentelemetry-instrument", "gunicorn", "app:app", "--bind", "0.0.0.0:8000", "--preload"]
```

### 3. Add env vars to your K8s deployment

```yaml
env:
  - name: OTEL_IP
    valueFrom:
      fieldRef:
        fieldPath: status.hostIP
  - name: OTEL_EXPORTER_OTLP_ENDPOINT
    value: http://$(OTEL_IP):4317
  - name: OTEL_SERVICE_NAME
    value: your-service-name
  - name: OTEL_SERVICE_VERSION
    value: "1.0.0"
  - name: OTEL_RESOURCE_ATTRIBUTES
    value: cx.application.name=hellas,cx.subsystem.name=your-service-name
  - name: OTEL_EXPORTER_OTLP_PROTOCOL
    value: grpc
```

See the full example in [`python-service/k8s/deployment.yaml`](./python-service/k8s/deployment.yaml).

---

## Reference services

| Directory | Language | Framework |
|---|---|---|
| [`kotlin-service/`](./kotlin-service/) | Kotlin (JVM) | Spring Boot |
| [`python-service/`](./python-service/) | Python | Flask + Gunicorn |

---

## Collector setup & architecture

The collector is deployed via the Coralogix `otel-integration` Helm chart with the following features enabled:

- **Logs** — stdout/stderr collection from all pods
- **Host & kubelet metrics** — CPU, memory, disk, network per node and pod
- **Span metrics** — auto-generated RED metrics (rate, errors, duration) from traces
- **Tail sampling** — 3-replica gateway; always keeps error traces, 10% probabilistic sample of the rest
- **Kubernetes events & cluster metrics** — pod scheduling, restarts, replica counts, API server metrics

---

## Querying your data in Coralogix

Copy-paste DataPrime queries for logs, traces, slow requests, error rates, and log-trace correlation are in [docs/dataprime-queries.md](docs/dataprime-queries.md). Includes a full field reference.

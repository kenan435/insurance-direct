# Hellas Direct — OpenTelemetry Instrumentation Guide

This repo contains reference instrumentation examples for sending traces, metrics, and logs from your Kubernetes services to Coralogix via OpenTelemetry.

## Prerequisites

The Coralogix OpenTelemetry collector must be deployed to your EKS cluster before instrumenting your apps. Follow the [Kubernetes Observability setup guide](https://coralogix.com/docs/opentelemetry/kubernetes-observability/kubernetes-observability-using-opentelemetry/) to deploy it via the Coralogix Helm chart.

Once deployed, a collector pod runs on every node (DaemonSet) and listens on port `4317`. Your apps send telemetry to the collector on the same node — not directly to Coralogix.

---

## How it works

```
Your App Pod  →  OTel Collector (DaemonSet, same node)  →  Coralogix
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

Instrumentation is done via the [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar) — no code changes required.

### 1. Add the agent to your Docker image

```dockerfile
COPY opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
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
  - name: OTEL_TRACES_EXPORTER
    value: otlp
  - name: OTEL_METRICS_EXPORTER
    value: none
  - name: OTEL_LOGS_EXPORTER
    value: none
  - name: OTEL_EXPORTER_OTLP_TRACES_PROTOCOL
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
CMD ["opentelemetry-instrument", "python", "app.py"]
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
  - name: OTEL_TRACES_EXPORTER
    value: otlp
  - name: OTEL_METRICS_EXPORTER
    value: otlp
  - name: OTEL_LOGS_EXPORTER
    value: otlp
  - name: OTEL_EXPORTER_OTLP_PROTOCOL
    value: grpc
```

See the full example in [`python-service/k8s/deployment.yaml`](./python-service/k8s/deployment.yaml).

---

## Reference services

| Directory | Language | Framework |
|---|---|---|
| [`kotlin-service/`](./kotlin-service/) | Kotlin (JVM) | Spring Boot |
| [`python-service/`](./python-service/) | Python | FastAPI |

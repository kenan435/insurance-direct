# Coralogix OpenTelemetry Setup Guide

This guide covers deploying the Coralogix OpenTelemetry collector to your EKS cluster and instrumenting your Kotlin and Python applications to send traces, metrics, and logs to Coralogix.

---

## Prerequisites

- `kubectl` configured against your EKS cluster
- `helm` v3.9+ installed
- A Coralogix **Send-Your-Data API key** — available under *Settings → API Keys*

---

## Part 1: Deploy the Coralogix Collector

The collector runs as three components on your cluster:
- **DaemonSet agent** — one pod per node, collects logs, metrics, and receives telemetry from your apps
- **Cluster collector** — single pod, collects Kubernetes cluster metrics and events
- **Tail sampling gateways** — 3 replicas, apply sampling policies to traces before shipping to Coralogix

### Step 1: Add the Helm repository

```bash
helm repo add coralogix https://cgx.jfrog.io/artifactory/coralogix-charts-virtual
helm repo update
```

### Step 2: Create the namespace and API key secret

```bash
kubectl create namespace coralogix

kubectl create secret generic coralogix-keys \
  --namespace coralogix \
  --from-literal=PRIVATE_KEY="<your-send-your-data-api-key>"
```

### Step 3: Deploy the collector

Download the `values.yaml` from this repository (`collector/values.yaml`), update the `clusterName` field, then run:

```bash
helm upgrade --install otel-coralogix-integration coralogix/otel-integration \
  --version=0.0.291 \
  --namespace coralogix \
  --render-subchart-notes \
  -f values.yaml
```

### Step 4: Verify

```bash
kubectl get pods -n coralogix
```

You should see the following pods running:

```
coralogix-opentelemetry-agent-<id>          1/1   Running   # DaemonSet — one per node
coralogix-opentelemetry-collector-<id>      1/1   Running   # Cluster collector
coralogix-opentelemetry-gateway-<id>        1/1   Running   # Tail sampling gateway (x3)
```

---

## Part 2: Instrument Your Applications

Once the collector is running, your applications send telemetry to the DaemonSet agent on the same node. No authentication is required — the agent handles that.

The node IP is injected into the pod at runtime using the Kubernetes Downward API:

```yaml
- name: OTEL_IP
  valueFrom:
    fieldRef:
      fieldPath: status.hostIP
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: http://$(OTEL_IP):4317
```

> Port `4317` is used for gRPC. The collector also listens on `4318` for HTTP/protobuf. Both Kotlin and Python are configured for gRPC in the examples below.

---

### Kotlin / Java (JVM)

Instrumentation is done via the [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar). No code changes are required.

#### 1. Add the agent to your Docker image

```dockerfile
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar \
    /app/opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
```

#### 2. Add environment variables to your Kubernetes deployment

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

> `cx.application.name` and `cx.subsystem.name` control how your service appears in the Coralogix UI. Set them to something meaningful for your team. You can also add any custom tags here, for example:
> ```
> cx.application.name=hellas,cx.subsystem.name=your-service-name,environment=production,team=backend
> ```

Log correlation is automatic — the Java agent injects `trace_id` and `span_id` into your log output (Logback and Log4j2 supported), allowing you to jump from a log line directly to the corresponding trace in Coralogix.

---

### Python

Python uses `opentelemetry-distro` for zero-code auto-instrumentation.

#### 1. Add dependencies to your Docker image

```dockerfile
RUN pip install opentelemetry-distro opentelemetry-exporter-otlp
RUN opentelemetry-bootstrap -a install
```

#### 2. Start your app via `opentelemetry-instrument`

```dockerfile
CMD ["opentelemetry-instrument", "python", "app.py"]
# or for uvicorn:
CMD ["opentelemetry-instrument", "uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

#### 3. Add environment variables to your Kubernetes deployment

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

> Same as Kotlin — `cx.application.name`, `cx.subsystem.name`, and any custom tags can be added to `OTEL_RESOURCE_ATTRIBUTES`.

---

## Reference

Full working examples for both services are in this repository:

| Directory | Language | Framework |
|---|---|---|
| [`kotlin-service/`](../kotlin-service/) | Kotlin (JVM) | Spring Boot |
| [`python-service/`](../python-service/) | Python | FastAPI |

The `collector/values.yaml` in this repository contains the full Helm chart configuration used to deploy the collector.

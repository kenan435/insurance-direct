# Sample DataPrime Queries — Hellas Direct

These queries are validated against real data flowing from the Hellas POC environment.
Open them in **Coralogix → Explore → Logs** or **Explore → Tracing**.

---

## Logs

### All application logs (excludes K8s infra events)

```
source logs
| filter $l.applicationname == 'hellas'
| filter $l.subsystemname != 'kube-events'
```

### Logs from a specific service

```
source logs
| filter $l.applicationname == 'hellas'
| filter $l.subsystemname == 'kotlin-service'
```

```
source logs
| filter $l.applicationname == 'hellas'
| filter $l.subsystemname == 'python-service'
```

### Error and warning logs

```
source logs
| filter $l.applicationname == 'hellas'
| filter $m.severity == ERROR || $m.severity == WARNING
| select $m.timestamp, $l.subsystemname, $d.logRecord.body
```

### Search log body for a keyword

```
source logs
| filter $l.applicationname == 'hellas'
| filter $d.logRecord.body ~ 'Exception'
```

### Logs from a specific pod

```
source logs
| filter $l.applicationname == 'hellas'
| filter $d.logRecord.resource.attributes.'k8s.pod.name' == 'kotlin-service-868569bdc-cl2dq'
```

### Log volume by service over time

```
source logs
| filter $l.applicationname == 'hellas'
| groupby $l.subsystemname, $m.severity count() as log_count
| orderby log_count desc
```

### K8s events for the hellas namespace (pod starts, image pulls, restarts)

```
source logs
| filter $l.applicationname == 'hellas'
| filter $l.subsystemname == 'kube-events'
| select $m.timestamp, $d.logRecord.body.object.reason, $d.logRecord.body.object.note
```

---

## Traces

### All spans for hellas

```
source spans
| filter $l.applicationName == 'hellas'
```

### Traces for a specific service

```
source spans
| filter $l.applicationName == 'hellas'
| filter $l.serviceName == 'kotlin-service'
```

### Slow requests (> 500ms)

```
source spans
| filter $l.applicationName == 'hellas'
| filter $d.duration > 500000
| select $m.timestamp, $l.serviceName, $d.operationName, $d.duration, $d.traceID
| orderby $d.duration desc
```

> `duration` is in microseconds — 500000 µs = 500ms

### Failed requests (HTTP 4xx / 5xx)

```
source spans
| filter $l.applicationName == 'hellas'
| filter $d.tags.'http.response.status_code' != '200' && $d.tags.'http.response.status_code' != '201'
| select $m.timestamp, $l.serviceName, $d.operationName, $d.tags.'http.response.status_code', $d.tags.'url.path', $d.traceID
```

### P95 latency by operation

```
source spans
| filter $l.applicationName == 'hellas'
| groupby $l.serviceName, $d.operationName percentile($d.duration, 95) as p95_us, avg($d.duration) as avg_us, count() as req_count
| orderby p95_us desc
```

### Request rate by endpoint

```
source spans
| filter $l.applicationName == 'hellas'
| filter $d.tags.'span.kind' == 'server'
| groupby $l.serviceName, $d.operationName count() as requests
| orderby requests desc
```

### Look up all spans for a specific trace ID

```
source spans
| filter $d.traceID == '<your-trace-id>'
| orderby $m.timestamp asc
```

### Traces with errors (OTel ERROR status)

```
source spans
| filter $l.applicationName == 'hellas'
| filter $d.tags.'otel.status_code' == 'ERROR'
| select $m.timestamp, $l.serviceName, $d.operationName, $d.tags.'otel.status_description', $d.traceID
```

---

## Correlating Logs and Traces

Coralogix automatically links logs to traces when `trace_id` is present in the log.
To find logs for a specific trace:

```
source logs
| filter $l.applicationname == 'hellas'
| filter $d.logRecord.attributes.'trace_id' == '<your-trace-id>'
```

---

## Field Reference

| Signal | Field | Example Value |
|--------|-------|---------------|
| Logs | `$l.applicationname` | `hellas` |
| Logs | `$l.subsystemname` | `kotlin-service`, `python-service`, `kube-events` |
| Logs | `$m.severity` | `INFO`, `ERROR`, `WARNING` |
| Logs | `$d.logRecord.body` | `"Started Application in 3.2 seconds"` |
| Logs | `$d.logRecord.resource.attributes.'service.name'` | `kotlin-service` |
| Logs | `$d.logRecord.resource.attributes.'k8s.pod.name'` | `kotlin-service-868569bdc-cl2dq` |
| Traces | `$l.applicationName` | `hellas` |
| Traces | `$l.serviceName` | `kotlin-service`, `python-service` |
| Traces | `$d.operationName` | `GET /policies`, `POST /claims` |
| Traces | `$d.duration` | microseconds (500000 = 500ms) |
| Traces | `$d.tags.'http.response.status_code'` | `"200"`, `"404"`, `"500"` |
| Traces | `$d.tags.'http.route'` | `/policies/{id}` |
| Traces | `$d.traceID` | `28b6763fc77b3014d72711afed6c1c98` |
| Traces | `$d.process.tags.'k8s.pod.name'` | `kotlin-service-868569bdc-cl2dq` |

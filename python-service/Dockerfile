FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Install OTel auto-instrumentation and bootstrap instrumented libraries
RUN pip install opentelemetry-distro opentelemetry-exporter-otlp
RUN opentelemetry-bootstrap -a install

COPY . .

EXPOSE 8000

CMD ["opentelemetry-instrument", "gunicorn", "app:app", "--bind", "0.0.0.0:8000", "--preload"]

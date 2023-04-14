# streaming-pipeline-with-redpanda-and-apache-beam
Building streaming ETL pipelines with Apache Beam and Redpanda

## Start Redpanda Container

docker run -d --pull=always --name=redpanda-1 --rm -p 8081:8081 -p 8082:8082 -p 9092:9092 -p 9644:9644 docker.redpanda.com/redpandadata/redpanda:latest redpanda start --overprovisioned --smp 1  --memory 1G --reserve-memory 0M --node-id 0 --check=false
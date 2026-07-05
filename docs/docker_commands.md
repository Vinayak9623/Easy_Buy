## Command to start postgres
```bash
docker run -d --name postgres-db -e POSTGRES_DB=productdb -e POSTGRES_USER=user -e POSTGRES_PASSWORD=user -p 5432:5432 -v pgdata:/var/lib/postgresql postgres:latest
```

## Command to start redis
```bash
docker run -d --name redis-server -p 6379:6379 redis:latest
```

## Command to start kafka
```bash
docker run -d --name kafka -p 9092:9092 apache/kafka:latest
```

## Command to start kafka ui
```bash
docker run -d --name kafka-ui -p 8080:8080 -e KAFKA_CLUSTERS_0_NAME=local-kafka -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=host.docker.internal:9092 -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui:latest
```

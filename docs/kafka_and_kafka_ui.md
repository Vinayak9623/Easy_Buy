# Kafka and Kafka UI

This setup uses Kafka in KRaft mode plus Kafka UI for browser-based inspection and admin tasks.

## Services

- `kafka`: the broker exposed on `localhost:9092`
- `kafka-ui`: the web UI exposed on `localhost:8080`

Kafka UI connects to the broker using the Docker service name `kafka:9092`, so both services must run in the same Compose network.

## Start the stack

Run the Compose file from the directory that contains `docker-compose.yml`:

```bash
docker compose up -d
```

Check the containers:

```bash
docker compose ps
```

Watch broker logs if startup fails:

```bash
docker compose logs -f kafka
```

## Open Kafka UI

Open:

- http://localhost:8080

The configured cluster name is `local-kafka`.

If the UI cannot connect, verify:

- Kafka is running
- the broker is reachable as `kafka:9092` from inside the Docker network
- port `8080` is not already in use

## Kafka basics in this setup

This Compose file runs Kafka in KRaft mode, so there is no ZooKeeper container.

Important ports:

- `9092`: Kafka client port inside the Docker network and on the host via port mapping
- `29092`: internal host-mapped listener used by the broker container
- `9093`: internal controller port used by the broker container
- `8080`: Kafka UI web port

The broker advertises two listener addresses:

- `kafka:9092` for Docker-internal clients such as Kafka UI
- `localhost:9092` for clients running on your machine

## Create a topic

You can create topics from Kafka UI or from the broker container.

Using the container shell:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh \
  --create \
  --topic demo-topic \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1
```

List topics:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server kafka:9092
```

Describe a topic:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh \
  --describe \
  --topic demo-topic \
  --bootstrap-server kafka:9092
```

## Produce and consume messages

Start a producer from inside the container network:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --topic demo-topic \
  --bootstrap-server kafka:9092
```

Type messages and press Enter to publish each line.

Start a consumer in another terminal:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --topic demo-topic \
  --from-beginning \
  --bootstrap-server kafka:9092
```

Each produced line should appear in the consumer.

## Using Kafka UI

Common UI actions:

- browse the cluster and verify broker status
- create and delete topics
- inspect partitions and replicas
- view messages in a topic
- produce test messages from the browser
- review consumer groups and offsets

Typical local workflow:

1. Start the stack with `docker compose up -d`
2. Open Kafka UI at `http://localhost:8080`
3. Create a topic such as `demo-topic`
4. Publish a test message
5. Consume the message from a terminal or from the UI

## Stop the stack

```bash
docker compose down
```

To remove volumes too:

```bash
docker compose down -v
```

## Troubleshooting

- If Kafka UI cannot connect, confirm the broker is exposed on `kafka:9092` inside the Docker network.
- If local tools cannot connect, verify that `9092` is free on your machine.
- If the broker fails to start, check the Kafka logs first; KRaft startup errors usually point to a listener or quorum mismatch.
- If you change the broker port or service name, update `KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS` in the UI service.

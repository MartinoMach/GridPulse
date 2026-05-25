# GridPulse

Predictive Failure Analysis Command Center for telecom-style network failure forecasting.

## Stack

- Java 21, Spring Boot 3.x
- Kafka 3.x
- PostgreSQL 15+
- Angular 17+

## Project Layout

- `src/main/java/com/gridpulse/ingestion` - KDD parser, Kafka replay engine, Kafka consumer, fallback simulator
- `src/main/java/com/gridpulse/service` - sliding-window prediction and KDD label accuracy scoring
- `src/main/resources/db/schema.sql` - optimized PostgreSQL DDL and indexes
- `frontend/src/app/grid-pulse` - Angular command center component
- `scripts/download_kdd.py` - Kaggle dataset downloader using `kagglehub`

## Dataset

Download and extract the KDD Cup 1999 10 percent dataset:

```bash
pip install kagglehub
python scripts/download_kdd.py
```

The script downloads `galaxyh/kdd-cup-1999-data`, extracts `kddcup.data_10_percent.gz`, and writes:

```text
data/kddcup.data_10_percent.csv
```

## Data Mapping

- `duration` -> `metadata.duration`
- `protocol_type` -> `eventType`
- `flag` -> `severity`
- `src_bytes + dst_bytes` -> `metadata.trafficVolume`
- `label` -> `kddLabel` for ground-truth accuracy reporting

Severity mapping:

- `SF` -> `LOW`
- `REJ` -> `MEDIUM`
- `S0`, `S1`, `S2`, `S3` -> `HIGH`
- `ERROR` -> `CRITICAL`

## Backend

Create the PostgreSQL database/user, then start Kafka and run:

```bash
mvn spring-boot:run
```

Enable KDD replay:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--gridpulse.replay.enabled=true"
```

Enable fallback simulator when the dataset is unavailable:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--gridpulse.simulator.enabled=true"
```

## API

- `POST /api/events`
- `GET /api/assets/{assetId}/prediction`
- `GET /api/alerts`
- `GET /api/accuracy`

## Frontend

```bash
cd frontend
npm install
npm start
```

The Angular dev server proxies `/api` to `http://localhost:8080`.

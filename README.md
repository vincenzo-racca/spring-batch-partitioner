# Spring Batch Partitioner

This project runs a batch job using **Spring Batch** with support for **parallel partitioning** and leverages:

- **DynamoDB (via LocalStack)** as the data source
- **PostgreSQL** to store Spring Batch metadata
- **Spring Scheduler** for periodic execution
- **Micrometer Tracing + Spring Boot Actuator** for observability and traceability
- **DynamoDB pre-populated on startup** with `PaperDelivery` and `DeliveryDriverCapacity` sample data

---

## ✅ Prerequisites

Make sure you have the following installed:

- **JDK 21** or higher
- **Podman** or **Docker**

---

## 🧱 Architecture

- A scheduled **JobRunner** triggers the job periodically
- It fetches all `deliveryDriverId##province` pairs via REST (mocked)
- Each pair is processed by a `Slave Step` in parallel
- Each `Slave Step`:
    - reads from DynamoDB the PaperDelivery with the deliveryDriverId##province partition key
    - processes in chunks
    - writes/logs/removes the data

At startup, the `main` application class clears and repopulates DynamoDB tables using `DynamoDbTemplate` and a mock data generator.

![Block Image](/spring-batch-01.webp)

![Block Image](/spring-batch-02.webp)

---

## 🐳 Local Execution with Podman / Docker Compose

The project includes a `compose.yml` with:

### 🔹 LocalStack
- Simulates AWS services (including DynamoDB)
- Exposes services on `localhost:4566`

### 🔹 PostgreSQL
- Stores Spring Batch metadata
- Auto-initialized via `config/init.sql`

### 🔹 pgAdmin
- Web UI available at [http://localhost:15432](http://localhost:15432)
- Credentials: `admin@pgadmin.com` / `password`

---

## ▶️ Run

Make sure you have [Podman](https://podman.io) or Docker installed. Then run:

```bash
podman compose -f compose.yml up
```

Or with Docker:

```bash
docker compose -f compose.yml up
```

Run the Spring Boot app:

```bash
./mvnw clean spring-boot:run
```

The Spring Boot app will:
- Wipe existing `PaperDelivery` and `DeliveryDriverCapacity` items
- Populate sample data based on mock `deliveryDriverId##province` combinations

---

## 📦 Job Execution

The job is automatically triggered via a `@Scheduled(cron = ...)` method.
You can modify the schedule in `application.application`:

```yaml
paper-delivery-cron: "0 */1 * * * *" # every minute
```

---

## 📈 Tracing and Actuator

- Project includes **Micrometer Tracing** to propagate `traceId`
- **Spring Boot Actuator** enabled at `/actuator/**`
- Distributed tracing supported via `JobParameters`

---

## 📁 Project Structure

```
.
├── HELP.md
├── README.md
├── compose.yml
├── config
│   ├── init-aws.sh
│   └── init.sql
├── mvnw
├── mvnw.cmd
├── pom.xml
├── prova.md
├── spring-batch-01.webp
├── spring-batch-02.webp
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── vincenzoracca
│   │   │           └── springbatchpartitioner
│   │   │               ├── SpringBatchAwsApplication.java
│   │   │               ├── job
│   │   │               │   ├── config
│   │   │               │   │   └── PaperDeliveryJobConfig.java
│   │   │               │   ├── reader
│   │   │               │   │   ├── PaperDeliveryReader.java
│   │   │               │   │   └── partitioner
│   │   │               │   │       └── PaperDeliveryPartitioner.java
│   │   │               │   ├── runner
│   │   │               │   │   └── PaperDeliveryJobRunner.java
│   │   │               │   └── writer
│   │   │               │       └── LogWriter.java
│   │   │               └── middleware
│   │   │                   ├── db
│   │   │                   │   └── entity
│   │   │                   │       ├── DeliveryDriverCapacity.java
│   │   │                   │       └── PaperDelivery.java
│   │   │                   └── mock
│   │   │                       └── ExternalService.java
│   │   └── resources
│   │       └── application.properties
```

---
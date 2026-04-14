# Rentaroost — Event-Driven Real Estate Ecosystem

An event-driven microservices platform for rental property management, featuring Kafka-choreographed booking flows, Apache Flink dynamic pricing, Stripe payment integration, and a Netflix DGS GraphQL API layer.

---

## System Architecture

The Rentaroost ecosystem consists of 9 specialized services communicating via gRPC for synchronous retrieval and Kafka for asynchronous choreography.

```mermaid
graph LR
    Client["Client (Next.js)"] --> GW["Gateway :6500"]
    Client --> GQL["GraphQL (DGS) :8185"]
    
    GW --> LS["Listings :8085"]
    GW --> BS["Booking :8380"]
    GW --> DPS["Dynamic Pricing :8281"]
    
    GQL -->|gRPC| LS
    GQL -->|gRPC| BS
    GQL -->|gRPC| PS["Payments :4242"]
    
    BS -->|gRPC| PS
    
    BS -->|"user.payment.initiated"| Kafka
    PS -->|"user.payment.confirmed / failed"| Kafka
    LS -->|"user.listings.view"| Kafka
    BS -->|"user.booking.confirmed"| Kafka
    
    DPS -->|"dynamicpricing.update"| Kafka
    Kafka -->|"dynamicpricing.update"| Flink["Flink Job"]
    Flink -->|"dynamicpricing.output"| Kafka
    Kafka -->|"dynamicpricing.output"| DPS
    
    DPS --> Redis["Redis (Price Cache)"]
    LS --> MongoDB[(MongoDB)]
    BS --> MongoDB
    
    PS --> Stripe["Stripe API"]
    PNS["Push Notif :6700"] --> FCM["Firebase (FCM)"]
    
    subgraph Discovery
      Eureka["Eureka :8761"]
    end
```

---

## Service Catalog

| Service | Port | Tech Stack | Responsibility |
|---|---|---|---|
| `gateway` | 6500 | Spring Cloud Gateway | Edge routing, Admin UI (Thymeleaf), Pricing Proxy |
| `graphql` | 8185 | Netflix DGS 9.0, gRPC | Federated API layer, Query aggregation |
| `listings-service` | 8085 | Spring Boot, MongoDB | Property management, Reactive data access |
| `booking-service` | 8380 | Spring Boot, Kafka, gRPC | Transactional lifecycle, Saga participant |
| `payments-service` | 4242 | Stripe SDK, Kafka | Payment intent lifecycle, Webhook orchestration |
| `dynamic-pricing-service` | 8281 | Spring Boot, Redis, Kafka | Flink bridge, Real-time pricing cache |
| `dynamic-pricing-flink-job` | — | Apache Flink 1.19 | Stream processing, Windowed demand sensing |
| `push-notification-service`| 6700 | Firebase Admin SDK | Topic-based messaging, FCM delivery |
| `discovery-service` | 8761 | Netflix Eureka | Service registry and health monitoring |

---

## Technical Deep Dives

### Kafka Event Choreography
Rentaroost uses a choreography-based Saga pattern for distributed consistency across booking and payment boundaries.

**Key Topics:**
- `user.payment.initiated`: Triggers Stripe payment intent creation.
- `user.payment.confirmed`: Signals successful payment to the booking service.
- `user.payment.failed`: Triggers compensation logic/rejection.
- `dynamicpricing.update`: Feeds user interactions (views/bookings) to Flink.

```mermaid
sequenceDiagram
    participant C as Client
    participant GQL as GraphQL (DGS)
    participant BS as Booking Service
    participant K as Kafka
    participant PS as Payments Service
    participant S as Stripe API
    
    C->>GQL: createBooking mutation
    GQL->>BS: gRPC CreateBooking
    BS->>K: Produce "user.payment.initiated"
    K->>PS: Consume "user.payment.initiated"
    PS->>S: PaymentIntent.create() + confirm()
    alt Success
        S-->>PS: PaymentIntent confirmed
        PS->>K: Produce "user.payment.confirmed"
    else Failure
        S-->>PS: StripeException
        PS->>K: Produce "user.payment.failed"
    end
```

### Flink Dynamic Pricing Engine
The system uses **Apache Flink** to compute real-time price adjustments based on demand signals.

- **Windowing Strategy**: Sliding Event Time Windows (30m duration, 1m slide).
- **Deduplication**: Per-window deduping by `userID + eventType` to prevent price manipulation.
- **Algorithm**: `BASE_PRICE × ImpactFactors`.
    - `Booking_Impact`: +10% per unique booking event.
    - `View_Impact`: +5% per unique view event.
- **Sink**: Results are pushed to `dynamicpricing.output` and cached in **Redis** with a 30-minute TTL.

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local builds)
- Maven (for Flink jobs)

### Quick Start
1. **Clone the repository**:
   ```bash
   git clone https://github.com/bellerophon95/rentaroost.git
   cd rentaroost
   ```

2. **Launch Infrastructure & Services**:
   ```bash
   docker-compose up -d --build
   ```

3. **Seed Initial Data**:
   ```bash
   ./seed_data.sh
   ```

### Access Points
- **Admin Dashboard**: `http://localhost:6500`
- **GraphiQL IDE**: `http://localhost:8185/graphiql`
- **Service Registry (Eureka)**: `http://localhost:8761`

---

## Infrastructure Note
The production infrastructure for this ecosystem requires managed Kafka and Flink clusters. For evaluation, the included `docker-compose.yaml` provides a fully functional local environment including Zookeeper, Kafka, Redis, and a Flink JobManager.

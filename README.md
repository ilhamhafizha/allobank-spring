# Allo Bank Split Bill API

Spring Boot REST API for creating bill groups, recording shared expenses, and calculating settlement transfers. Uses Java 17, Maven, PostgreSQL, Flyway migrations, and BigDecimal for monetary calculations.

## Build and run

With Docker and Docker Compose installed:

```sh
cp .env.example .env
# Edit .env to set your local configuration.
docker compose up --build -d
docker compose logs -f app
curl http://localhost:4110/health
```

Compose builds the application with Maven in a multi-stage Dockerfile, starts PostgreSQL, waits for database health, and starts the API on port **4110**. Flyway applies the schema automatically. Stop services with `docker compose down`; the named database volume preserves data.

For local development, install JDK 17+ and use the Maven wrapper:

```sh
docker compose up -d postgres
./mvnw test
./mvnw package
java -jar target/allobank-split-bill-0.0.1-SNAPSHOT.jar
```

The context test requires the configured PostgreSQL database. Calculator and request-validation unit tests run independently of the database:

```sh
./mvnw -Dtest=SettlementCalculatorTest,RequestValidationTest,SettlementResponseTest,SettlementServiceTest test
```

Copy `.env.example` to `.env` once, then edit its values. Spring Boot reads `.env` from the working directory; shell environment variables override the file. Docker Compose also reads `.env` and supplies the container's internal database URL. Database credentials and the GitHub username are required. Compose exposes PostgreSQL on host port **5433** by default; its internal app connection uses port **5432**. If you change `DB_PORT` or `DB_NAME`, update the local `DB_URL` to match. This private repository includes `.env` at the owner’s request so it is available after cloning. Review its settings for the destination PC.

## API examples

Create a group (HTTP 201):

```sh
curl -i -X POST http://localhost:4110/api/bill-groups \
  -H 'Content-Type: application/json' \
  -d '{"name":"Weekend Trip","participants":[{"name":"Ilham"},{"name":"Budi"},{"name":"Andi"}]}'
```

Use the returned group and participant IDs in subsequent requests. The following example assumes group ID 1 and participant IDs 1, 2, and 3 from a fresh database.

Add an expense (HTTP 201):

```sh
curl -i -X POST http://localhost:4110/api/bill-groups/1/expenses \
  -H 'Content-Type: application/json' \
  -d '{"description":"Dinner","amount":150000.00,"paidBy":1,"participantIds":[1,2,3]}'
```

Get settlement (HTTP 200):

```sh
curl http://localhost:4110/api/bill-groups/1/settlement
```

For the single expense above:

```json
{
  "groupId": 1,
  "totalExpense": 150000.00,
  "service_charge_pct": 4,
  "service_charge_amount": 6000.00,
  "transactions": [
    {"fromId": 2, "from": "Budi", "toId": 1, "to": "Ilham", "amount": 52000.00},
    {"fromId": 3, "from": "Andi", "toId": 1, "to": "Ilham", "amount": 52000.00}
  ]
}
```

Health endpoint (HTTP 200, body `OK`):

```sh
curl -i http://localhost:4110/health
```

Errors use a ProblemDetail response with status and detail; validation errors also include an errors array with field names and messages. Invalid inputs return HTTP 400. Missing groups or payers return HTTP 404. Payers and beneficiaries must belong to the group, and beneficiary IDs must be unique. Expense amounts must be positive, have at most 17 integer digits and 2 decimal digits. Participants may share names; settlement includes IDs to distinguish them.

## Personalization

GitHub username: **ilhamhafizha**. Lowercase character sum: **1254**. Service charge: **1254 % 10 = 4%**. The code calculates the percentage from the configured username; the percentage is not hardcoded.

## Settlement rules and scope

- Each expense is split equally among its selected beneficiaries; the payer need not be a beneficiary.
- All amounts use BigDecimal and two decimal places. Service charge is calculated on total group expenses with HALF_UP rounding.
- Charge is allocated to expenses using differences between rounded cumulative charges, ordered by expense ID. This preserves the group charge exactly without independently rounding each expense's charge.
- Assumption: each expense's allocated charge is reimbursed to its payer together with the principal and shared by its beneficiaries. There is no separate platform payment recipient.
- Beneficiaries are ordered by participant ID. Shares round down and the final participant receives the remainder, preserving the exact total. This deterministic rule can place multiple remainder cents on one participant.
- Net balance is total paid (including allocated charge) minus total owed. A deterministic greedy algorithm matches debtors with creditors. It produces at most D + C - 1 transfers for D debtors and C creditors, but does not guarantee the global minimum number of transfers.
- Settlement is a computed summary, not a payment record. There is one implicit currency per group; currency conversion, authentication, alternative split strategies, and payment recording are outside this submission's scope.
- Controller, service, repository, and calculator responsibilities are separated. Writes are transactional; settlement runs in a read-only transaction. Flyway manages the schema, Hibernate validates it, and settlement fetches participant associations with an entity graph.

## Hardest design decision

The hardest design decision was balancing exact monetary allocation against a simple, explainable settlement algorithm. I chose equal splits with deterministic remainder allocation and cumulative service-charge rounding so that every allocated amount reconciles with the group total. I accepted that the last beneficiary can absorb multiple remainder cents, and that greedy debtor-creditor matching does not guarantee the globally smallest number of transfers. These trade-offs keep the implementation straightforward to test and extend while making its financial rules explicit.

## Frontend

Open **http://localhost:4110/** to create groups, record expenses, and view settlement transfers. HTML, CSS, and JavaScript are served by Spring Boot without a separate frontend build.

The browser stores group and expense history in localStorage because the API does not provide list endpoints. History is specific to the browser; clearing it does not delete database records. Settlement calculations always come from the backend. Amounts are displayed in rupiah and payments are not recorded.

## Project structure

```text
src/main/java/com/allobanksplitbill/
  calculator/    Monetary allocation and settlement algorithm
  controller/    HTTP endpoints
  dto/request/   Validated request models
  dto/response/  API response models
  entity/        Persistence models
  exception/     API error handling
  repository/    Database access
  service/       Application use cases and transactions
src/main/resources/
  db/migration/  Versioned database schema
  static/        Browser interface
src/test/java/   Unit, controller, and application context tests
```

`.env.example` documents the configuration fields. At the owner’s request, this private repository also versions `.env`, build output (`target/`), IDE settings, and local supporting files so they are available after cloning. Existing ignore rules still apply to new, untracked files. Supporting documentation is versioned in `.local/docs/`; this README contains the project explanation and setup instructions.

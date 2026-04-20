# SmartLoad Optimization API

A stateless Spring Boot service that finds the optimal truck load combination from a list of
candidate orders — maximising revenue while respecting weight, volume, hazmat, and route
constraints.

---

## How to Run

```bash
git clone git@github.com:kmukesh27/smartload-optimizer.git
cd smartload-optimizer
docker compose up --build
# → Service available at http://localhost:8080
```

---

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

---

## Example Request

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

### Expected Response

```json
{
  "truck_id": "truck-123",
  "selected_order_ids": ["ord-001", "ord-002"],
  "total_payout_cents": 430000,
  "total_weight_lbs": 30000,
  "total_volume_cuft": 2100,
  "utilization_weight_percent": 68.18,
  "utilization_volume_percent": 70.0
}
```


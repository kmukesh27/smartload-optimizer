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

Following are the Various Test Cases

## Example Request -- Default

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
  "utilization_volume_percent": 70.0,
  "algorithm_used":"BITMASK_DP"
}
```

## Example Request -- MAX_REVENUE mode

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-max-revenue.json
```

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "PREMIUM1",
        "BULK1"
    ],
    "total_payout_cents": 52000,
    "total_weight_lbs": 600,
    "total_volume_cuft": 300,
    "utilization_weight_percent": 60.0,
    "utilization_volume_percent": 60.0,
    "algorithm_used": "BITMASK_DP"
}
```

## Example Request -- MAX_UTILIZATION mode

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-max-utilization.json
```

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "BULK1",
        "BULK2"
    ],
    "total_payout_cents": 4000,
    "total_weight_lbs": 900,
    "total_volume_cuft": 450,
    "utilization_weight_percent": 90.0,
    "utilization_volume_percent": 90.0,
    "algorithm_used": "BITMASK_DP"
}
```

## Example Request -- BALANCED mode  

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-balanced.json
```

### Expected Response

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "O1",
        "O2",
        "O3"
    ],
    "total_payout_cents": 1900,
    "total_weight_lbs": 900,
    "total_volume_cuft": 450,
    "utilization_weight_percent": 90.0,
    "utilization_volume_percent": 90.0,
    "algorithm_used": "BITMASK_DP"
}
```

## Example Request -- With Pareto solutions included

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-pareto-soln.json
```

### Expected Response

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "HIGH_VALUE",
        "MID_VALUE1",
        "MID_VALUE2"
    ],
    "total_payout_cents": 155000,
    "total_weight_lbs": 850,
    "total_volume_cuft": 425,
    "utilization_weight_percent": 85.0,
    "utilization_volume_percent": 85.0,
    "algorithm_used": "BITMASK_DP",
    "pareto_solutions": [
        {
            "order_ids": [
                "HIGH_VALUE",
                "MID_VALUE1",
                "MID_VALUE2"
            ],
            "total_payout_cents": 155000,
            "utilization_weight_percent": 85.0,
            "utilization_volume_percent": 85.0
        },
        {
            "order_ids": [
                "HIGH_VALUE",
                "MID_VALUE1",
                "LOW_VALUE_BIG"
            ],
            "total_payout_cents": 138000,
            "utilization_weight_percent": 90.0,
            "utilization_volume_percent": 90.0
        },
        {
            "order_ids": [
                "HIGH_VALUE",
                "MID_VALUE2",
                "LOW_VALUE_BIG"
            ],
            "total_payout_cents": 133000,
            "utilization_weight_percent": 95.0,
            "utilization_volume_percent": 95.0
        }
    ]
}
```


## Example Request -- BACKTRACKING algorithm

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-backtracking.json
```

### Expected Response

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "O2",
        "O1",
        "O3"
    ],
    "total_payout_cents": 1900,
    "total_weight_lbs": 900,
    "total_volume_cuft": 450,
    "utilization_weight_percent": 90.0,
    "utilization_volume_percent": 90.0,
    "algorithm_used": "BACKTRACKING"
}
```


## Example Request -- All features combined (BALANCED + Pareto + BACKTRACKING)

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request-all-features.json
```

```json
{
    "truck_id": "T1",
    "selected_order_ids": [
        "O2",
        "O1",
        "O3"
    ],
    "total_payout_cents": 1900,
    "total_weight_lbs": 900,
    "total_volume_cuft": 450,
    "utilization_weight_percent": 90.0,
    "utilization_volume_percent": 90.0,
    "algorithm_used": "BACKTRACKING",
    "pareto_solutions": [
        {
            "order_ids": [
                "O1",
                "O2",
                "O3"
            ],
            "total_payout_cents": 1900,
            "utilization_weight_percent": 90.0,
            "utilization_volume_percent": 90.0
        }
    ]
}
```
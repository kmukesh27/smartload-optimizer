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

## Example Request -- Default

Default behavior using BITMASK_DP algorithm with MAX_REVENUE optimization.
Selects orders that maximize total payout while respecting truck capacity constraints. 

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

Prioritizes highest-paying orders even if truck space is underutilized.
Picks PREMIUM1+BULK1 ($520 payout, 60% util) over BULK1+BULK2 ($40 payout, 90% util). 

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

Prioritizes filling truck capacity (weight + volume) over revenue.
Picks BULK1+BULK2 (90% utilization) instead of higher-paying but smaller orders.

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

Weighted combination: 60% revenue + 40% utilization score.
Finds middle ground between maximizing payout and filling truck capacity.

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

Returns all non-dominated solutions where no other option is better in ALL objectives.
Shows trade-offs: higher payout vs higher utilization — user can pick based on preference.

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

Uses recursive backtracking with pruning instead of bitmask enumeration.
Sorts by efficiency (payout/weight) and prunes branches that can't beat current best.

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

Combines all bonus features: BALANCED scoring, Pareto frontier, and backtracking algorithm. 
Demonstrates full API capability with weighted optimization and multiple solution options.  

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
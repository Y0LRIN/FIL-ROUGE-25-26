# FIL-ROUGE-25-26

## Backend Test Runner

From the `backend/` folder run:

```bash
./run-tests.sh
```

This script compiles the backend sources and executes `FullTestRunner`, including:
- repository tests for all data models
- controller integration tests for HTTP routes

## Supported API routes

The backend exposes the following REST-like endpoints:

- `GET /api/clients`
- `GET /api/clients/{id}`
- `POST /api/clients`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

- `GET /api/agents`
- `GET /api/agents/{id}`
- `POST /api/agents`
- `PUT /api/agents/{id}`
- `DELETE /api/agents/{id}`

- `GET /api/addresses`
- `GET /api/addresses/{id}`
- `POST /api/addresses`
- `PUT /api/addresses/{id}`
- `DELETE /api/addresses/{id}`

- `GET /api/properties`
- `GET /api/properties/{id}`
- `POST /api/properties`
- `PUT /api/properties/{id}`
- `DELETE /api/properties/{id}`

- `GET /api/property_images`
- `GET /api/property_images/{id}`
- `POST /api/property_images`
- `PUT /api/property_images/{id}`
- `DELETE /api/property_images/{id}`

- `GET /api/favorites`
- `GET /api/favorites/{id}`
- `POST /api/favorites`
- `PUT /api/favorites/{id}`
- `DELETE /api/favorites/{id}`

- `GET /api/contracts`
- `GET /api/contracts/{id}`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `DELETE /api/contracts/{id}`

- `GET /api/transactions`
- `GET /api/transactions/{id}`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

- `GET /api/visits`
- `GET /api/visits/{id}`
- `POST /api/visits`
- `PUT /api/visits/{id}`
- `DELETE /api/visits/{id}`

All routes support `OPTIONS` for CORS preflight.

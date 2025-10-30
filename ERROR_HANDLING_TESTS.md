# Test Exception Handling for In-Memory Banking APIs

This script demonstrates the detailed exception handling implemented in the application.

## Test Cases

### 1. Account Not Found
```bash
curl -X GET http://localhost:8081/api/accounts/NONEXISTENT
```

### 2. Insufficient Funds for Withdrawal
```bash
# First create an account
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountHolder": "John Doe", "initialBalance": 100.00}'

# Try to withdraw more than available balance (assuming account ID is ACC001)
curl -X POST http://localhost:8081/api/accounts/ACC001/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'
```

### 3. Invalid Request Body
```bash
curl -X POST http://localhost:8081/api/accounts/ACC001/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": -50.00}'
```

### 4. Malformed JSON
```bash
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountHolder": "John Doe", "initialBalance": invalid}'
```

### 5. Invalid Transfer - Non-existent destination account
```bash
curl -X POST http://localhost:8081/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": "ACC001", "toAccountId": "NONEXISTENT", "amount": 50.00}'
```

## Expected Error Response Format

All error responses will include:
- `timestamp`: When the error occurred
- `status`: HTTP status code
- `error`: Error category
- `message`: Detailed error message
- `path`: API endpoint that was called
- `traceId`: Unique identifier for tracking the error
- `details`: Additional context about the error (account IDs, amounts, suggestions, etc.)
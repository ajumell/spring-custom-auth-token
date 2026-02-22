# Library Requirements Document (PRD)

## Generic Token Management Library

**Tech Stack:** Spring Boot, JPA (EntityManager), QueryDSL
**Type:** Reusable Library

---

## 1. Overview

### 1.1 Purpose

The objective of this project is to build a **generic, reusable token management library** for Spring Boot applications that supports secure token generation and validation with configurable validity rules.

This library will be used across multiple domains such as:

* Email verification
* Password reset links
* Workflow approvals
* Payment authorization links
* Idempotency tokens
* Internal system verification
* Action authorization

---

### 1.2 Core Capabilities

The library provides:

1. Token generation
2. Token validation
3. Configurable validity period
4. Usage-count based tokens (NEW)
5. One-time tokens
6. Multi-use tokens
7. Unlimited usage tokens
8. Token invalidation
9. Cleanup of expired tokens

---

## 2. Goals

### 2.1 Primary Goals

* Provide a simple API to generate tokens
* Provide a secure validation mechanism
* Support validity duration
* Support usage count control
* Ensure concurrency safety
* Provide extensibility
* Make the library production-ready

---

### 2.2 Non-Goals

* Authentication framework
* JWT replacement
* Session management
* Notification delivery (SMS/email sending)

---

## 3. Core Concepts

### 3.1 Token Definition

A token is a temporary authorization artifact linked to a business parameter.

Example parameters:

* userId
* email
* transactionId
* referenceId
* any business identifier

---

### 3.2 Token Lifecycle

GENERATED → ACTIVE → USED / EXPIRED / INVALIDATED

For multi-use tokens:

ACTIVE → PARTIALLY_USED → USED

---

## 4. Functional Requirements

---

### 4.1 Generate Token

#### Description

Generate a random secure token associated with an input parameter.

#### Input

* parameter (required)
* tokenType (optional)
* validityDuration (optional)
* usageLimit (NEW — optional)
* metadata (optional)
* hashingMode (optional)

#### Usage Limit Rules (NEW)

The generation request must support:

* 1 → one-time token
* N (>1) → multi-use token
* unlimited → no usage restriction

If not provided → default from configuration.

#### Behavior

* Generate cryptographically secure token
* Compute expiry time
* Persist token
* Store usage limit and usage count
* Return generated token

#### Output

* token
* expiryTime
* usageLimit
* remainingUses

---

### 4.2 Validate Token

#### Description

Validate whether a token is valid for a parameter.

#### Input

* parameter
* token
* tokenType (optional)

#### Validation Rules

Validation must check:

* Token exists
* Parameter matches
* Not expired
* Not invalidated
* Usage limit not exceeded
* Token type matches (if provided)

#### Behavior on Success

* Increment usage count
* If usage limit reached → mark USED

#### Output

* valid / invalid
* reason
* remainingUses

---

### 4.3 Usage Count Handling (NEW — Critical Requirement)

Each token must store:

* usageLimit
* usageCount

Validation must be atomic.

Rules:

* If usageLimit = unlimited → only expiry check applies
* If usageCount < usageLimit → allow
* If usageCount >= usageLimit → invalid

---

### 4.4 Invalidate Token

Allow manual invalidation.

Use cases:

* Security revoke
* User retry
* Admin cancel
* Workflow cancellation

---

### 4.5 Cleanup Expired Tokens

Provide:

* Scheduled cleanup option
* Manual cleanup API
* Batch deletion

---

## 5. API Design (Library Level)

### 5.1 TokenService Interface

```
public interface TokenService {

    GeneratedToken generate(TokenRequest request);

    ValidationResult validate(ValidateTokenRequest request);

    void invalidate(String token);

    long cleanupExpired();
}
```

---

### 5.2 Request Models

#### TokenRequest

* parameter
* tokenType
* validityDuration
* usageLimit (NEW)
* metadata

#### ValidateTokenRequest

* parameter
* token
* tokenType

---

## 6. Data Model

### 6.1 Token Entity

Fields:

* id
* token (unique)
* parameterValue
* tokenType
* status
* expiryTime
* usageLimit (NEW)
* usageCount (NEW)
* createdAt
* usedAt (last used)
* metadata

---

### 6.2 Status Values

* ACTIVE
* PARTIALLY_USED
* USED
* INVALIDATED
* EXPIRED

---

### 6.3 Indexing Strategy

Indexes required:

* token (unique)
* parameter + tokenType
* status
* expiryTime

---

## 7. QueryDSL Requirement

All DB operations must use:

* EntityManager
* QueryDSL

Not allowed:

* Spring Data repositories

Expected operations:

* Fetch token
* Conditional usage increment
* Atomic validation update
* Cleanup queries

---

## 8. Concurrency Requirements (Critical)

Validation must be atomic.

Required behavior:

* Increment usage count safely
* Prevent race conditions
* Prevent over-usage

Recommended strategy:

Conditional update:

```
UPDATE token
SET usage_count = usage_count + 1
WHERE token = ?
AND usage_count < usage_limit
AND status = ACTIVE
AND expiry > now
```

If update count = 1 → valid
Else → invalid

---

## 9. Security Requirements

### Token Generation

* SecureRandom mandatory
* Configurable token length
* URL-safe tokens

### Storage Modes

* Raw token mode
* Hashed token mode (recommended)

---

## 10. Configuration

Library configuration must support:

* default validity duration
* default usage limit
* unlimited usage flag representation
* token length
* hashing enabled
* cleanup enabled
* allow multiple active tokens
* token generator strategy

---

## 11. Extensibility

Library must allow:

* Custom token generator
* Custom hashing strategy
* Custom metadata serializer
* Custom cleanup strategy
* Future multi-tenant support

---

## 12. Edge Cases

* Concurrent validations
* Unlimited tokens misuse
* Token reuse attempts
* Expired during validation
* Multiple tokens for same parameter
* Large usageLimit values
* Unlimited token abuse

---

## 13. Performance Requirements

* Indexed lookup validation
* Single DB roundtrip validation
* Batch cleanup
* High read throughput support

---

## 14. Observability

Provide hooks for:

* Metrics
* Logging
* Audit events

Events:

* token_generated
* token_validated
* token_failed
* token_usage_incremented
* token_exhausted
* token_invalidated

---

## 15. Error Handling

Validation must return structured results.

Reasons:

* NOT_FOUND
* EXPIRED
* INVALIDATED
* USAGE_LIMIT_EXCEEDED (NEW)
* INVALID_PARAMETER
* INVALID_TOKEN

---

## 16. Packaging Requirements

* Spring Boot starter module
* Auto configuration
* Conditional beans
* Minimal setup
* Clear extension points

---

## 17. Future Enhancements

* Redis acceleration
* Attempt tracking
* Rate limiting
* Device binding
* IP binding
* Sliding expiration
* Token chaining
* Action-scoped tokens
* Audit dashboard
* Token analytics

---

## 18. Acceptance Criteria

* Generate token works
* Validate token works
* Usage limit enforced
* Unlimited tokens supported
* Concurrency safe
* QueryDSL used everywhere
* Expiry enforced
* Invalidation works
* Cleanup works
* Configurable defaults
* Thread safe

---

## 19. Summary

This library provides a **production-grade, extensible token management system** with:

* Expiry-based validation
* Usage-count validation (single, multi, unlimited)
* Concurrency-safe validation
* QueryDSL-based persistence
* Secure token handling
* Reusable Spring Boot starter architecture

It is designed to be a foundational internal library used across multiple services and workflows.

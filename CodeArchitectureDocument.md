# Code Architecture Document

## Custom Auth Token Library

---

## 1. Overview

This document describes the architecture for the **Custom Auth Token Library**, a reusable Spring Boot module responsible for token generation and validation with expiry and usage-limit support.

The library is designed as an **internal Spring Boot starter style module** and is consumed via dependency injection (Autowired service usage).
No controllers or REST exposure are part of this module.

---

## 2. Technology Stack

### Core Technologies

* Java 8+
* Spring Boot
* Spring Context
* Spring Auto Configuration
* JPA (EntityManager based)
* QueryDSL (for all database queries)

---

### Supporting Technologies

* Hibernate (JPA provider)
* SecureRandom (token generation)
* Jackson (optional metadata JSON handling)
* Micrometer hooks (optional metrics)
* Spring Boot Configuration Properties

---

## 3. High Level Architecture

The library follows a **layered internal architecture**:

1. Service Layer (Public API of library)
2. DAO Layer (Abstraction)
3. DAO Implementation (QueryDSL based persistence)
4. JPA Layer (Entities + QueryDSL support)
5. Auto-configuration Layer

Important characteristics:

* No controllers
* No Spring Data repositories
* EntityManager + QueryDSL only
* Service layer is entry point

---

## 4. Base Package Structure

Base package:

```
com.xeoscript.modules.customauthtoken
```

---

## 5. Detailed Package Structure

```
com.xeoscript.modules.customauthtoken
│
├── services
│   ├── TokenService
│   ├── generator
│   ├── validation
│   └── internal
│
├── dao
│   ├── TokenDAO
│
├── jpa
│   ├── entity
│   │   └── TokenEntity
│   │
│   ├── services
│   │   └── TokenDAOImpl
│   │
│   └── query
│       └── QueryDSL helpers (optional)
│
├── model
│   ├── request
│   ├── response
│   └── enums
│
├── config
│   └── CustomAuthTokenAutoConfiguration
│
└── util
```

---

## 6. Layer Responsibilities

---

### 6.1 Service Layer (Public Entry Point)

Package:

```
services
```

Responsibilities:

* Token generation orchestration
* Validation orchestration
* Usage count handling
* Business rules
* Security checks
* Calling DAO

This is the only layer exposed to consuming applications.

Consumers will use:

```
@Autowired
TokenService tokenService;
```

---

### 6.2 DAO Layer (Abstraction)

Package:

```
DAO
```

Responsibilities:

* Persistence abstraction
* Define DB operations contract
* Hide QueryDSL details from service layer

Examples of DAO responsibilities:

* Save token
* Fetch token
* Atomic validation update
* Invalidate token
* Cleanup expired tokens

---

### 6.3 DAO Implementation Layer

Package:

```
jpa.services
```

Responsibilities:

* QueryDSL query construction
* EntityManager usage
* Atomic updates
* Batch operations
* Performance optimizations

Important rule:

This layer is the only place where QueryDSL queries are written.

---

### 6.4 JPA Entity Layer

Package:

```
jpa.entity
```

Responsibilities:

* Database mapping
* Index definitions
* Token state persistence
* Usage tracking

Entities must be independent of service logic.

---

### 6.5 Model Layer

Package:

```
model
```

Contains:

* Request objects
* Response objects
* Enums
* DTOs

Examples:

* TokenRequest
* ValidateTokenRequest
* ValidationResult
* TokenStatus
* ValidationFailureReason

---

### 6.6 Utilities Layer

Package:

```
util
```

Responsibilities:

* Token generator
* Hashing utilities
* Time utilities
* Metadata serialization

---

## 7. Auto-Configuration Architecture

Package:

```
config.autoconfiguration
```

### Purpose

Provide Spring Boot starter behavior.

Responsibilities:

* Bean creation
* DAO wiring
* Service wiring
* QueryDSL wiring
* Entity scan registration
* Configuration properties registration

---

### Auto-Configuration Class Responsibilities

The auto-configuration must:

1. Register TokenService bean
2. Register DAO implementation bean
3. Register generator beans
4. Enable configuration properties
5. Configure Entity scanning
6. Configure JPA scanning (if needed)
7. Provide conditional beans (override support)

---

### Example Behavior

* If TokenDAO not provided → create default DAO
* If TokenGenerator not provided → create default generator
* Allow override by consuming application

---

## 8. Bean Wiring Flow

```
Application
   ↓
TokenService (Autowired)
   ↓
TokenDAO (interface)
   ↓
TokenDAOImpl (QueryDSL)
   ↓
EntityManager + QueryDSL
   ↓
Database
```

---

## 9. QueryDSL Integration Design

### Requirements

* QueryDSL JPA module required
* Q classes generated during build
* DAO implementation uses QTokenEntity

### Query Types

* Fetch token
* Conditional usage increment
* Invalidate token
* Cleanup expired
* Validation query

---

## 10. Configuration Properties

A properties class will exist to configure:

* default validity
* default usage limit
* token length
* hashing enabled
* cleanup enabled
* unlimited usage representation
* allow multiple active tokens

These properties are injected into service layer.

---

## 11. Extensibility Design

The architecture allows extension through:

* Custom TokenGenerator bean
* Custom hashing bean
* Custom DAO override
* Custom validation policy
* Custom cleanup strategy

Because all wiring is conditional.

---

## 12. Design Principles Used

* Clean architecture (internal layering)
* Dependency inversion (DAO abstraction)
* Spring Boot starter pattern
* QueryDSL isolation
* Override-friendly auto configuration
* Stateless service layer
* Concurrency-safe persistence design

---

## 13. No Controller Principle

This module intentionally contains:

* No controllers
* No REST exposure
* No web dependencies

It is purely a library used via:

```
@Autowired TokenService
```

---

## 14. Build / Packaging Strategy

The module is packaged as:

* Maven library (preferred)
* Spring Boot starter style
* Separate artifact

Example:

```
custom-auth-token-spring-boot-starter
```

---

## 15. Runtime Flow Example

Token generation:

Service → DAO → QueryDSL → DB → Response

Validation:

Service → DAO (atomic update) → QueryDSL → DB → Result

---

## 16. Future Architectural Extensions

* Redis caching layer
* Multi-tenant support
* Token analytics module
* Observability module
* Attempt tracking
* Rate limiting
* Event publishing

---

## 17. Summary

The Custom Auth Token Library is designed as a **clean, layered, Spring Boot starter style architecture** with:

* Service as entry point
* DAO abstraction
* QueryDSL-based persistence
* Strong separation of concerns
* Override-friendly auto configuration
* Library-first design (no controllers)

This ensures the library is reusable, testable, extensible, and production-ready.

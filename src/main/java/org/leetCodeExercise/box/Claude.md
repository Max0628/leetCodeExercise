# PROJECT ARCHITECTURE PATTERNS

This document outlines the architecture patterns used in this project, primarily following a **Clean Architecture** approach while leveraging key **Spring Boot** features and annotations for development convenience. (Do Not Follow the DDD patterns)

---

### ## 0. Fundamental (Java Code Layer)

- Must Support Backward Compatible design
- Must Follow the Immutable Functional Design (Avoid the Call by Reference)
- Avoid the common bad smell, e.g., Feature Envy, Shotgun Surgery, God Object,
- Avoid the Magic Number, arrange the variable into static Constants or Enum
- Use Combination first, then use **Inheritance**
- Avoid the Null Point Exception with Optional
- Avoid to use the Static Class and Inner Class
- Use Lombok library functions by default

#### Logging Standards (Mandatory)

All logging must support **Service Level Indicators (SLI)** measurement and **Kafka Stream + ELK/PLG** architecture.

- **Log Format Pattern (Unified)**
  ```
  [Module][Operation] Status. key1={}, key2={}
  ```
  - **Module**: Service/Adapter name, e.g., `Goods-Service`, `Payment-Adapter`, `Kafka-Publisher`
  - **Operation**: Operation type, e.g., `Query`, `Update`, `API`, `Publish`, `Consume`
  - **Status**: Result indicator, e.g., `Start.`, `Success.`, `Failed!`, `Retry triggered.`, `Fallback activated.`
  - **key={}**: Use equals sign `=` for key-value pairs (better support for log parsing and key-value structure)

- **Log Level Guidelines**

  | Level | Usage | Alert | Example |
  |-------|-------|-------|---------|
  | **DEBUG** | Detailed diagnostic info. **Disabled in Production.** | No Alert | Cache hit/miss details |
  | **INFO** | Critical path events, business state changes, SLI tracking (throughput, latency). | No Alert | `[Service][Query] Success. duration=125ms` |
  | **WARN** | **Predictive anomalies**: External system jitter, retry triggered, cache miss with degradation, 4xx errors. | Non-immediate (if > N times/hour) | `[Adapter][API] Network error, retry triggered.` |
  | **ERROR** | **Fatal errors**: DB connection failure, 5xx errors, unexpected RuntimeException, DLQ accumulation, data loss risk. | Immediate (P0) | `[Adapter][Fallback] Circuit open! action=ManualReview` |

- **Mandatory Context (TraceID + Business Key + Duration)**
  - **traceId**: Always include `MDC.get("traceId")` for distributed tracing 
    (It could be injected by opentelemetry or custom Momomsgid utility)
  - **Business Key**: Include business identifier (orderNo, goodsCode, userId, fileId)
  - **duration**: Include `duration={}ms` for all operations (for latency tracking)
  - **Input Params**: Include input parameters when ERROR occurs

- **Examples**
  ```java
  // ✅ GOOD: Structured logging with full context
  log.info("[Goods-Service][Query] Success. goodsCode={}, duration={}ms", goodsCode, System.currentTimeMillis() - startTime);
  
  // ✅ GOOD: WARN for retriable errors
  log.warn("[Payment-Adapter][API] Network error, retry triggered. orderNo={}, duration={}ms, cause={}", orderNo, duration, e.getMessage());
  
  // ✅ GOOD: ERROR for critical failures with stack trace
  log.error("[Kafka-Publisher][Fallback] Circuit open! orderNo={}, action=QueueForRetry", orderNo, e);
  
  // ❌ BAD: Missing context and business key
  log.error("Update failed");
  
  // ❌ BAD: Wrong log level (should be WARN for retriable error)
  log.error("Network timeout, will retry", e);
  ```

- **Performance Best Practices**
  - Use async appenders to avoid blocking application threads
  - Use `log.isDebugEnabled()` before expensive string operations
  - Avoid logging in high-frequency loops (use sampling: log every Nth iteration)
  - Use `{}` placeholders instead of string concatenation

---

### ## 1. Controller Pattern (Infrastructure Layer)

- **Responsibility**
  - The controller should be **thin**. It only deals with the **input/output transforming job**, delegating all business logic to a service (Use Case) class.
  - Responsible for **API request validation** and **authentication/authorization** checks.
- **Input/Output**
  - The response should be a `ResponseEntity<ApiResponse<T>>` object, where `ApiResponse` is a generic wrapper class for all API responses, ensuring consistent structure (e.g., status, data, message).
  - Use dedicated **Request DTOs** and **Response DTOs** for all API communication. **Never expose Domain Entities** directly via the controller.
  - API Pattern Must follow the **Google AIP Principles** with **Versioning** pattern, e.g., `/api/${version}/${category}/${resource}/${subType}:${action}`
- **Spring Annotations**
  - Use `@RestController` to define the controller.
  - Use `@RequiredArgsConstructor` to automatically generate a constructor for final fields (dependency injection), promoting **Immutability**.
  - Use `@PostMapping`, `@GetMapping`, `@PutMapping`, etc., to map HTTP methods and paths.
  - Use `@PathVariable` to extract variables from the URL path.
  - Use `@RequestBody` to bind the request body to a Java object.
  - Use `@Valid` or `@Validated` to enable validation on the request body object.

---

### ## 2. Service Pattern (Application / Use Case Layer)

- **Responsibility**
  - The Service layer (often called a **Use Case Interactor** in Clean Architecture) implements the **core business logic (Create, Update, Delete, Single Query, Collection Query functions)**.
  - Coordinates the flow of data to and from the domain layer (Aggregate/Entity) and external resources (Repository, API Client, MQ… etc.)
  - **Transaction boundary**: Service layer is the ONLY place to define transaction boundaries using `@Transactional`.

- **Dependencies**
    - Services should only depend on **interfaces** (e.g., Repository interfaces, API Gateway interfaces) as per the **Dependency Inversion Principle (DIP)**.
    - Use constructor injection with `@RequiredArgsConstructor` (Lombok) for immutability.

- **Spring Annotations**
  - Use `@Service` to annotate Service classes
  - Use `@Transactional(value = "tx0")` for write operations
  - Use `@Transactional(value = "tx0", readOnly = true)` for read operations
  - Set `rollbackFor = Exception.class`
  - Set appropriate `propagation` level (default: `Propagation.REQUIRED`)
  - Use `@RequiredArgsConstructor` for dependency injection

- **Error Handling**
  - Throw specific, business-meaning exceptions (defined in the Domain layer) instead of generic runtime exceptions.
  - The Controller Advice will handle the mapping to HTTP status codes.
  - All exceptions in `rollbackFor` will trigger transaction rollback.

- **Transaction Management (Critical)**
  - **MUST** use `@Transactional` at the Service method level to manage database transactions.
  - **FORBIDDEN** to use `@Transactional` in DAO/Repository layer.
  - Use appropriate transaction configuration based on operation type:
    - **Write operations**: `@Transactional(value = "tx0", rollbackFor = Exception.class)`
    - **Read operations**: `@Transactional(value = "tx0", readOnly = true, rollbackFor = Exception.class)`
  - Set `propagation` level appropriately (default: `Propagation.REQUIRED`)

- **Transaction Best Practices**

  ```java
  @Service
  @RequiredArgsConstructor
  @Slf4j
  public class GoodsService {

    private final GoodsRepository goodsRepository;

    // ✅ CORRECT: Read-only transaction for query operations
    @Transactional(value = "tx0", readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<GoodsDTO> getGoodsByCodeList(List<String> goodsCodes) {
      // [INFO] Include business key, traceId, and operation start
      log.info("[Goods-Service][Query] Start. goodsCodes={}, count={}, traceId={}", 
          goodsCodes, goodsCodes.size(), MDC.get("traceId"));
      long startTime = System.currentTimeMillis();

      try {
        List<GoodsEntity> entities = goodsRepository.findByGoodsCodes(goodsCodes);

        // [INFO] Success path with duration for SLI tracking
        log.info("[Goods-Service][Query] Success. count={}, duration={}ms", 
            entities.size(), System.currentTimeMillis() - startTime);

        return entities.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

      } catch (Exception e) {
        // [ERROR] Include full context with stack trace
        log.error("[Goods-Service][Query] Failed! goodsCodes={}, duration={}ms, cause={}", 
            goodsCodes, System.currentTimeMillis() - startTime, e.getMessage(), e);
        throw e;
      }
    }

    // ✅ CORRECT: Write transaction for update operations
    @Transactional(value = "tx0", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateGoods(GoodsDTO dto) {
      // [INFO] Command operation with business key
      log.info("[Goods-Service][Update] Start. goodsCode={}, traceId={}", 
          dto.getGoodsCode(), MDC.get("traceId"));
      long startTime = System.currentTimeMillis();

      try {
        GoodsEntity entity = goodsRepository.findById(dto.getGoodsCode())
            .orElseThrow(() -> new GoodsNotFoundException(dto.getGoodsCode()));

        // Update entity
        entity.setGoodsName(dto.getGoodsName());
        entity.setSalePrice(dto.getSalePrice());

        goodsRepository.update(entity);

        // [INFO] Success with duration
        log.info("[Goods-Service][Update] Success. goodsCode={}, duration={}ms", 
            dto.getGoodsCode(), System.currentTimeMillis() - startTime);

      } catch (GoodsNotFoundException e) {
        // [WARN] Expected business exception
        log.warn("[Goods-Service][Update] Not found. goodsCode={}, duration={}ms", 
            dto.getGoodsCode(), System.currentTimeMillis() - startTime);
        throw e;

      } catch (Exception e) {
        // [ERROR] Unexpected error with full context
        log.error("[Goods-Service][Update] Failed! goodsCode={}, input={}, duration={}ms, cause={}", 
            dto.getGoodsCode(), dto, System.currentTimeMillis() - startTime, e.getMessage(), e);
        throw e;
      }
    }

    private GoodsDTO convertToDTO(GoodsEntity entity) {
      return GoodsDTO.builder()
          .goodsCode(entity.getGoodsCode())
          .goodsName(entity.getGoodsName())
          .salePrice(entity.getSalePrice())
          .build();
    }
  }
  ```

- **Common Transaction Patterns**

  ```java
  // Pattern 1: Query with read-only optimization
  @Transactional(value = "tx0", readOnly = true, rollbackFor = Exception.class)
  public CategoryVO getCategories(CategoryVO categoryVO) throws Exception {
    // Query logic
  }

  // Pattern 2: Command with write transaction
  @Transactional(value = "tx0", rollbackFor = Exception.class)
  public void createOrder(OrderDTO orderDTO) throws Exception {
    // Write logic
  }

  // Pattern 3: Mixed operations (read + write)
  @Transactional(value = "tx0", rollbackFor = Exception.class)
  public void processOrder(String orderNo) throws Exception {
    OrderEntity order = orderRepository.findById(orderNo)
        .orElseThrow(() -> new OrderNotFoundException(orderNo));

    // Business logic
    order.updateStatus("COMPLETED");

    orderRepository.update(order);
  }
  ```
  
---

### ## 3. Repository Pattern (Persistence Adapter)

- **Responsibility**
  - **Interface Definition (Domain Layer):** The Repository interface should be defined in the Domain Layer, consisting only of methods required by the Use Cases (Service).
  - **Implementation (Infrastructure Layer):** The concrete implementation (e.g., using Spring Data JPA) resides in the Infrastructure layer.
  - **Entity Mapping:** Responsible for converting between the **Domain Aggregate/Entity** (the pure business object) and the **Persistence Entity** (the JPA/DB-specific object) before saving or after loading. **NEVER** let the Persistence Entity leak into the Service layer.
  - DAO (Data Access Object) is responsible for encapsulating all database access logic, including complex SQL queries, dynamic query construction, and pagination.
  - Use **JdbcTemplate** to handle Native SQL queries, suitable for complex scenarios such as JOIN, UNION, dynamic conditions, etc.
  - **SHOULD NOT contain business logic**, only responsible for data access and transformation.
- **Spring Annotations**

  - Use `@Repository` to define the domain repository.
  - Extend the `@CrudRepository`  CrudRepository by default
  - Support the `JpaSpecificationExecutor`  for criteria query

- **Input/Output**

  - **FORBIDDEN to return** `List<Map<String, Object>>`, must use **RowMapper** to convert to strongly-typed **DTO**.
  - All query results must be returned via **DTO** to ensure type safety and clear API contracts.
  - If multi-datasource failover is required, it should be encapsulated in **DualDataSourceExecutor**.

- **SQL Management**

  - Complex SQL should use **SqlBuilder** pattern, avoiding direct string concatenation in DAO methods.
  - SQL constants should be defined as `private static final String` for easier maintenance and testing.
  - Dynamic conditions should use `StringBuilder` with parameterized queries to avoid SQL Injection.

- **Spring Annotations**

  - Use `@Repository` to annotate DAO classes.
  - Use `@Autowired` + `@Qualifier` to inject specific `JdbcTemplate` (e.g., `oseJdbcTemplate`).
  - **FORBIDDEN**: Do NOT use `@Transactional` in DAO/Repository layer - transaction management belongs to Service layer only.
  - Use Lombok `@Setter(onMethod_ = {@Autowired, @Qualifier("...")})` for cleaner dependency injection.

- **RowMapper Pattern**

  ```java
  // ✅ Correct Example: Using RowMapper to convert to DTO
  private static final RowMapper<LimitBuyDTO> LIMIT_BUY_MAPPER = (rs, rowNum) -> {
      LimitBuyDTO dto = new LimitBuyDTO();
      dto.setLbcode(rs.getString("LBCODE"));
      dto.setGoodsCode(rs.getString("GOODS_CODE"));
      dto.setSpecialPrice(rs.getBigDecimal("SPECIAL_PRICE"));
      return dto;
  };

  public List<LimitBuyDTO> findLimitBuyList(LimitBuyVO vo) {
      String sql = sqlBuilder.buildQuery(vo);
      Object[] args = sqlBuilder.buildArgs(vo);
      return jdbcTemplate.query(sql, LIMIT_BUY_MAPPER, args);
  }
  ```

- **Dual DataSource Failover (if applicable)**

  ```java
  @Component
  @Slf4j
  public class DualDataSourceJdbcExecutor {

      @Autowired
      @Qualifier("oseJdbcTemplate")
      private JdbcTemplate oseTemplate;

      @Autowired
      @Qualifier("exaJdbcTemplate")
      private JdbcTemplate exaTemplate;

      public <T> List<T> queryWithFallback(
              String sql,
              RowMapper<T> mapper,
              Object... args) {
          long startTime = System.currentTimeMillis();
          
          try {
              List<T> result = oseTemplate.query(sql, mapper, args);
              log.info("[DataSource][OSE] Query success. rows={}, duration={}ms", 
                  result.size(), System.currentTimeMillis() - startTime);
              return result;
              
          } catch (Exception e) {
              // [WARN] OSE failed, triggering failover to EXA
              log.warn("[DataSource][Failover] OSE failed, switching to EXA. duration={}ms, cause={}", 
                  System.currentTimeMillis() - startTime, e.getMessage());
              
              try {
                  List<T> result = exaTemplate.query(sql, mapper, args);
                  log.info("[DataSource][EXA] Failover success. rows={}, duration={}ms", 
                      result.size(), System.currentTimeMillis() - startTime);
                  return result;
                  
              } catch (Exception exaException) {
                  // [ERROR] Both datasources failed - critical error
                  log.error("[DataSource][Failover] Both OSE and EXA failed! duration={}ms", 
                      System.currentTimeMillis() - startTime, exaException);
                  throw exaException;
              }
          }
      }
  }
  ```

- **Naming Conventions**
  - DAO class naming: `${DomainName}Dao`, e.g., `LimitBuyDao`
  - Method naming: `find${Entity}List`, `update${Entity}`, `delete${Entity}`
  - RowMapper constant naming: `${ENTITY}_MAPPER`, e.g., `LIMIT_BUY_MAPPER`

---

### ## 4. Domain Model (DTO/ Entity)

- **Responsibility**
  - This section defines the core of the **Domain Layer**, following **Single Responsibility Principle (SRP)**.
- **Design Principles**
  - **Behavioral Focus (SRP):** Entities should contain logic (behavior/commands) related to changing their own state, not just data (getters/setters).
  - **No I/O Operations:** It **SHOULD NOT** perform any I/O operations (e.g., database access, network calls to AI services). This ensures the Domain remains pure and testable.
  - Entity object handle the data CRUD with database (Must put Entity object into entity package of app module)
  - DTO object handle the data aggregation and to be the cross Modules/ APIs communication interfaces (Must put DTO object into dto package of common module)
  - Entity naming pattern, e.g., `${DomainObjectNaming} (Without "Entity" suffix)`
  - DTO naming pattern, e.g., `${DomainObjectNaming} (With "DTO" suffix)`
  - Arrange All Status of the Bean Lifecycle into EntityTypeEnum, and add this enum into constant package of common module
  - Naming
    - Entity,
  - **(Option) Optimistic Locking:** It contains a **`version` field** (initial value 1) for optimistic locking.
  - **(Option) State Change:** Every command that changes the state of the aggregate/entity should be done via a **method call on the aggregate/entity itself**. Once the state is changed, the **`version` field should be incremented by 1**.
- **Spring and Lombok's Annotations for Entity**

  - Use `@Entity`to define the entity.
  - Use `@Table(name= "xxxx")` to define the database table mapping.
  - Use `@Id and @GeneratedValue(strategy = GenerationType.*IDENTITY*)` to define the sequencial id fot the entity.
  - Use Lombok's `@Data` annotation to replace the getter/setter
  - Use Lombok's `@ToString and @EqualsAndHashCode` annotations to each entity

- **Responsibility**

  - **Entity:** JPA entity, corresponding to database tables, used for ORM operations.
  - **DTO (Data Transfer Object):** Data transfer object, used for cross-layer/cross-module communication.
    - **Request DTO:** API input parameter encapsulation.
    - **Response DTO:** API return result encapsulation.
    - **Query DTO:** DAO query result mapping (converted from `RowMapper`).

- **Design Principles**

  - **Single Responsibility (SRP):** DTO is only responsible for data carrying, does not contain business logic.
  - **Immutability:** Prefer using `@Value` (Lombok) to create immutable DTOs.
  - **No I/O Operations:** DTO should not perform any I/O operations.
  - **Serialization Friendly:** DTO must support JSON serialization (for REST API).

- **Naming Conventions**

  - Entity: `${DomainName}` (without "Entity" suffix), e.g., `LimitBuy`
  - Request DTO: `${DomainName}Request`, e.g., `LimitBuyRequest`
  - Response DTO: `${DomainName}DTO`, e.g., `LimitBuyDTO`
  - VO (View Object): `${DomainName}VO`, e.g., `LimitBuyVO` (internal use, not exposed to API)

- **DTO Conversion Pattern**

  ```java
  // ✅ Convert from Map (legacy code compatibility)
  public static LimitBuyDTO fromMap(Map<String, Object> map) {
      LimitBuyDTO dto = new LimitBuyDTO();
      dto.setLbcode((String) map.get("LBCODE"));
      dto.setGoodsCode((String) map.get("GOODS_CODE"));
      return dto;
  }

      dto.setLbcode(rs.getString("LBCODE"));
  ```

- **Spring Annotations**
  - Entity: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
  - DTO: `@Data` (Lombok), `@Schema` (OpenAPI), `@JsonProperty` (Jackson)
  - Request DTO: `@Valid`, `@NotNull`, `@Size` (Validation)

---

### ## 5. External Integration Adapter Pattern (Infrastructure Layer)

This pattern handles integration with external systems and resources, supporting multiple scenarios: **REST API**, **Message Queue (Kafka/NATS)**, **Cache (Redis)**, and **File I/O**. All implementations adhere to the **Open/Closed Principle (OCP)** and **Dependency Inversion Principle (DIP)**.

---

#### 5.1 Core Principles

- **Gateway Interface (Application Layer)**
  - Define a technology-agnostic **interface** in the Application/Service layer (e.g., `PaymentGateway`, `NotificationGateway`, `FileStorageGateway`).
  - Service layer depends on the interface, NOT the concrete implementation.

- **Adapter Implementation (Infrastructure Layer)**
  - Concrete implementation class (e.g., `RestApiAdapter`, `KafkaProducerAdapter`, `RedisAdapter`, `S3FileAdapter`).
  - **Encapsulation (SRP):** Responsible for all low-level details: protocol-specific logic, connection management, serialization/deserialization, error handling.
  - **Data Mapping:** Maps Domain objects to external system format and vice versa.

- **Error Handling Hierarchy (Critical)**
  
  All adapters must handle exceptions in the following order:
  
  1. **Business Exception** - Domain-specific errors (e.g., `PaymentDeclinedException`, `InsufficientInventoryException`)
  2. **I/O Exception** - Network, connection, read/write errors
  3. **Timeout Exception** - Request timeout, circuit breaker timeout
  4. **Protocol Exception** - HTTP 4xx/5xx, Kafka serialization errors, Redis connection errors
  5. **Unexpected Exception** - Unknown errors, fallback to degraded service

- **Resilience Patterns (Mandatory)**
  - **Circuit Breaker** - Prevent cascading failures (using Resilience4j)
  - **Retry Mechanism** - Retry transient failures with exponential backoff
  - **Timeout** - Set explicit timeout for all external calls
  - **Fallback** - Graceful degradation when external system fails
  - **Bulkhead** - Isolate thread pools for different external systems

---

#### 5.2 REST API Integration Pattern

**Use Cases:** External payment API, third-party data providers, microservice communication

**Technology Stack:**
- Spring `RestTemplate` or `WebClient` (reactive)
- Resilience4j for circuit breaker, retry, timeout
- Jackson for JSON serialization

**Error Handling Strategy:**
```
Business Exception (4xx with business error code)
  ↓
I/O Exception (Network errors, connection refused)
  ↓
Timeout Exception (Read timeout, connection timeout)
  ↓
HTTP Protocol Exception (5xx server errors)
  ↓
Fallback (Circuit open, degraded mode)
```

**Best Practice Example:**

```java
// Gateway Interface (Application Layer)
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}

// Adapter Implementation (Infrastructure Layer)
@Component
@Slf4j
public class ThirdPartyPaymentAdapter implements PaymentGateway {

    private final RestTemplate restTemplate;
    private final PaymentProperties properties;

    @Autowired
    public ThirdPartyPaymentAdapter(
            @Qualifier("paymentRestTemplate") RestTemplate restTemplate,
            PaymentProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService", fallbackMethod = "paymentFallback")
    @TimeLimiter(name = "paymentService")
    public PaymentResult processPayment(PaymentRequest request) {
        // [INFO] External call start - include business key, amount, traceId for tracking
        log.info("[Payment-Adapter][API] Start. orderNo={}, amount={}, traceId={}", 
            request.getOrderNo(), request.getAmount(), MDC.get("traceId"));
        long startTime = System.currentTimeMillis();

        try {
            // 1. Map Domain object to API request
            ThirdPartyPaymentApiRequest apiRequest = mapToApiRequest(request);

            // 2. Call external API
            HttpEntity<ThirdPartyPaymentApiRequest> entity = new HttpEntity<>(apiRequest, buildHeaders());
            ResponseEntity<ThirdPartyPaymentApiResponse> response = restTemplate.exchange(
                properties.getPaymentUrl(),
                HttpMethod.POST,
                entity,
                ThirdPartyPaymentApiResponse.class
            );

            // 3. Handle Business Exception (4xx with error codes)
            if (response.getStatusCode().is4xxClientError()) {
                String errorCode = response.getBody().getErrorCode();
                throw new PaymentBusinessException(errorCode, response.getBody().getErrorMessage());
            }

            // [INFO] External call success - include transaction ID and duration
            log.info("[Payment-Adapter][API] Success. orderNo={}, txnId={}, duration={}ms", 
                request.getOrderNo(), response.getBody().getTransactionId(), 
                System.currentTimeMillis() - startTime);

            // 4. Map API response to Domain object
            return mapToPaymentResult(response.getBody());

        } catch (PaymentBusinessException e) {
            // [ERROR] Business exception - do not retry, requires manual review
            log.error("[Payment-Adapter][API] Business error! orderNo={}, errorCode={}, duration={}ms", 
                request.getOrderNo(), e.getErrorCode(), System.currentTimeMillis() - startTime);
            throw e;

        } catch (ResourceAccessException e) {
            // [WARN] I/O Exception - network/connection errors, retry will be triggered
            log.warn("[Payment-Adapter][API] Network error, retry triggered. orderNo={}, duration={}ms, cause={}", 
                request.getOrderNo(), System.currentTimeMillis() - startTime, e.getMessage());
            throw new PaymentIOException("Network error during payment", e);

        } catch (HttpClientErrorException e) {
            // [ERROR] HTTP 4xx errors - invalid request from our side
            log.error("[Payment-Adapter][API] Client error (4xx)! orderNo={}, status={}, response={}, duration={}ms", 
                request.getOrderNo(), e.getStatusCode(), e.getResponseBodyAsString(), 
                System.currentTimeMillis() - startTime);
            throw new PaymentValidationException("Invalid payment request", e);

        } catch (HttpServerErrorException e) {
            // [WARN] HTTP 5xx errors - external system issue, may retry
            log.warn("[Payment-Adapter][API] Server error (5xx), retry triggered. orderNo={}, status={}, duration={}ms", 
                request.getOrderNo(), e.getStatusCode(), System.currentTimeMillis() - startTime);
            throw new PaymentServerException("Payment service unavailable", e);

        } catch (Exception e) {
            // [ERROR] Unexpected exception - requires investigation
            log.error("[Payment-Adapter][API] Unexpected error! orderNo={}, amount={}, duration={}ms, cause={}", 
                request.getOrderNo(), request.getAmount(), 
                System.currentTimeMillis() - startTime, e.getMessage(), e);
            throw new PaymentSystemException("Payment system error", e);
        }
    }

    // Fallback method for circuit breaker
    private PaymentResult paymentFallback(PaymentRequest request, Exception e) {
        // [ERROR] Circuit breaker open - service degradation, requires immediate attention
        log.error("[Payment-Adapter][Fallback] Circuit open! orderNo={}, amount={}, triggerCause={}, action=QueueForRetry", 
            request.getOrderNo(), request.getAmount(), e.getClass().getSimpleName());

        // Degraded service: return pending status, queue for retry
        return PaymentResult.builder()
            .orderNo(request.getOrderNo())
            .status(PaymentStatus.PENDING)
            .message("Payment queued for processing")
            .requiresManualReview(true)
            .build();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        return headers;
    }

    private ThirdPartyPaymentApiRequest mapToApiRequest(PaymentRequest request) {
        return ThirdPartyPaymentApiRequest.builder()
            .merchantId(properties.getMerchantId())
            .orderNo(request.getOrderNo())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .build();
    }

    private PaymentResult mapToPaymentResult(ThirdPartyPaymentApiResponse response) {
        return PaymentResult.builder()
            .transactionId(response.getTransactionId())
            .status(mapPaymentStatus(response.getStatus()))
            .message(response.getMessage())
            .build();
    }
}
```

**Configuration (application.yml):**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30000
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
  
  retry:
    instances:
      paymentService:
        maxAttempts: 3
        waitDuration: 1000
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - org.springframework.web.client.ResourceAccessException
          - com.example.exception.PaymentServerException
        ignoreExceptions:
          - com.example.exception.PaymentBusinessException
  
  timelimiter:
    instances:
      paymentService:
        timeoutDuration: 10s
```

---

#### 5.3 Message Queue (Kafka/NATS) Integration Pattern

**Use Cases:** Event publishing, asynchronous communication, event-driven architecture

**Technology Stack:**
- Spring Kafka / Spring Cloud Stream
- NATS Streaming
- Jackson for message serialization

**Error Handling Strategy:**
```
Business Exception (Invalid message format, validation errors)
  ↓
Serialization Exception (JSON parsing errors)
  ↓
Broker Connection Exception (Kafka broker unavailable)
  ↓
Timeout Exception (Producer/Consumer timeout)
  ↓
Fallback (Store to DB for retry, DLQ)
```

**Best Practice Example (Kafka Producer):**

```java
// Gateway Interface
public interface OrderEventPublisher {
    void publishOrderCreated(OrderCreatedEvent event);
}

// Kafka Adapter Implementation
@Component
@Slf4j
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderEventRepository eventRepository; // For fallback

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Autowired
    public KafkaOrderEventPublisher(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            OrderEventRepository eventRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventRepository = eventRepository;
    }

    @Override
    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "publishOrderCreatedFallback")
    @Retry(name = "kafkaPublisher")
    public void publishOrderCreated(OrderCreatedEvent event) {
        // [INFO] Event publish start - include business key, event type, traceId
        log.info("[Kafka-Publisher][OrderCreated] Start. orderNo={}, topic={}, traceId={}", 
            event.getOrderNo(), orderCreatedTopic, MDC.get("traceId"));
        long startTime = System.currentTimeMillis();

        try {
            // 1. Validate event (Business validation)
            validateEvent(event);

            // 2. Send to Kafka
            ListenableFuture<SendResult<String, OrderCreatedEvent>> future = 
                kafkaTemplate.send(orderCreatedTopic, event.getOrderNo(), event);

            // 3. Add callback for async result
            future.addCallback(
                result -> {
                    // [INFO] Async publish success - include partition, offset, duration
                    log.info("[Kafka-Publisher][OrderCreated] Success. orderNo={}, partition={}, offset={}, duration={}ms",
                        event.getOrderNo(), 
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        System.currentTimeMillis() - startTime);
                },
                ex -> {
                    // [ERROR] Async publish failed
                    log.error("[Kafka-Publisher][OrderCreated] Async failed! orderNo={}, cause={}",
                        event.getOrderNo(), ex.getMessage(), ex);
                    handlePublishFailure(event, ex);
                }
            );

        } catch (IllegalArgumentException e) {
            // [ERROR] Business exception - invalid event, do not retry
            log.error("[Kafka-Publisher][OrderCreated] Validation failed! orderNo={}, eventType={}, cause={}", 
                event.getOrderNo(), event.getClass().getSimpleName(), e.getMessage());
            throw new OrderEventValidationException("Invalid event data", e);

        } catch (SerializationException e) {
            // [ERROR] Serialization exception - data issue
            log.error("[Kafka-Publisher][OrderCreated] Serialization failed! orderNo={}, eventType={}, cause={}", 
                event.getOrderNo(), event.getClass().getSimpleName(), e.getMessage(), e);
            throw new OrderEventSerializationException("Failed to serialize event", e);

        } catch (Exception e) {
            // [WARN] Kafka connection or timeout - retry will be triggered
            log.warn("[Kafka-Publisher][OrderCreated] Publish failed, will retry. orderNo={}, duration={}ms, cause={}", 
                event.getOrderNo(), System.currentTimeMillis() - startTime, e.getMessage());
            throw new OrderEventPublishException("Event publish failed", e);
        }
    }

    // Fallback: Store to database for retry
    private void publishOrderCreatedFallback(OrderCreatedEvent event, Exception e) {
        // [ERROR] Fallback to outbox pattern - circuit open
        log.error("[Kafka-Publisher][Fallback] Circuit open, saving to outbox. orderNo={}, eventType={}, triggerCause={}", 
            event.getOrderNo(), "ORDER_CREATED", e.getClass().getSimpleName());

        try {
            // Store event to database for later retry
            OrderEventOutbox outbox = OrderEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .aggregateId(event.getOrderNo())
                .payload(toJson(event))
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

            eventRepository.save(outbox);
            
            // [INFO] Outbox save success
            log.info("[Kafka-Publisher][Outbox] Saved. orderNo={}, eventId={}", 
                event.getOrderNo(), outbox.getEventId());

        } catch (Exception dbException) {
            // [ERROR] Critical - both Kafka and DB failed, data loss risk (P0 Alert)
            log.error("[Kafka-Publisher][Outbox] Failed to store! Data loss risk! orderNo={}, eventType={}", 
                event.getOrderNo(), "ORDER_CREATED", dbException);
            // Alert monitoring system
            throw new CriticalEventLossException("Event may be lost", dbException);
        }
    }

    private void validateEvent(OrderCreatedEvent event) {
        if (event.getOrderNo() == null || event.getOrderNo().isEmpty()) {
            throw new IllegalArgumentException("Order number is required");
        }
        if (event.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
    }

    private void handlePublishFailure(OrderCreatedEvent event, Throwable ex) {
        log.error("Async publish failed for order: {}", event.getOrderNo(), ex);
        
        // Check if it's a retriable error
        if (isRetriable(ex)) {
            // Store to DB for retry
            publishOrderCreatedFallback(event, (Exception) ex);
        } else {
            // Non-retriable error, alert
            log.error("Non-retriable publish error, manual intervention required");
        }
    }

    private boolean isRetriable(Throwable ex) {
        return ex instanceof TimeoutException 
            || ex instanceof org.apache.kafka.common.errors.TimeoutException
            || ex instanceof org.apache.kafka.common.errors.NetworkException;
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new OrderEventSerializationException("JSON conversion failed", e);
        }
    }
}
```

**Best Practice Example (Kafka Consumer):**

```java
@Component
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @Autowired
    public OrderEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
        topics = "${kafka.topic.order-created}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "orderEventListenerContainerFactory"
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        exclude = {OrderEventValidationException.class}, // Don't retry business errors
        dltTopicSuffix = "-dlt" // Dead Letter Topic
    )
    public void handleOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        // Set MDC context for consumer
        MDC.put("traceId", event.getTraceId());
        MDC.put("eventType", "ORDER_CREATED");

        // [INFO] Event consumption start - include partition, offset
        log.info("[Kafka-Consumer][OrderCreated] Received. orderNo={}, partition={}, offset={}, traceId={}", 
            event.getOrderNo(), partition, offset, event.getTraceId());
        long startTime = System.currentTimeMillis();

        try {
            // 1. Business validation
            if (event.getOrderNo() == null) {
                throw new OrderEventValidationException("Order number is missing");
            }

            // 2. Process event
            orderService.processOrderCreatedEvent(event);
            
            // [INFO] Processing success - include duration
            log.info("[Kafka-Consumer][OrderCreated] Processed success. orderNo={}, duration={}ms", 
                event.getOrderNo(), System.currentTimeMillis() - startTime);

        } catch (OrderEventValidationException e) {
            // [ERROR] Business exception - send to DLT, don't retry
            log.error("[Kafka-Consumer][OrderCreated] Validation failed, sending to DLT. orderNo={}, cause={}", 
                event.getOrderNo(), e.getMessage());
            throw e;

        } catch (DataAccessException e) {
            // [WARN] DB exception - retry will be triggered
            log.warn("[Kafka-Consumer][OrderCreated] DB error, will retry. orderNo={}, duration={}ms, cause={}", 
                event.getOrderNo(), System.currentTimeMillis() - startTime, e.getMessage());
            throw new OrderEventProcessingException("DB error", e);

        } catch (Exception e) {
            // [ERROR] Unexpected error - retry will be triggered
            log.error("[Kafka-Consumer][OrderCreated] Processing failed! orderNo={}, duration={}ms, cause={}", 
                event.getOrderNo(), System.currentTimeMillis() - startTime, e.getMessage(), e);
            throw new OrderEventProcessingException("Processing failed", e);
            
        } finally {
            MDC.clear();
        }
    }

    @DltHandler
    public void handleDlt(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        
        // [ERROR] DLQ - requires manual intervention (P0 Alert)
        log.error("[Kafka-DLT][OrderCreated] Sent to DLQ! orderNo={}, errorMessage={}, action=ManualReview", 
            event.getOrderNo(), exceptionMessage);
        
        // Store to DB for manual review
        // Send alert to monitoring system
    }
}
```

---

#### 5.4 Cache (Redis) Integration Pattern

**Use Cases:** Session management, rate limiting, distributed locking, caching

**Technology Stack:**
- Spring Data Redis
- Lettuce (connection pool)
- Redisson (distributed lock)

**Error Handling Strategy:**
```
Business Exception (Invalid cache key format)
  ↓
Serialization Exception (Object serialization errors)
  ↓
Connection Exception (Redis unavailable)
  ↓
Timeout Exception (Command timeout)
  ↓
Fallback (Cache-aside, return null, query DB)
```

**Best Practice Example:**

```java
// Gateway Interface
public interface CacheGateway {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, Duration ttl);
    void delete(String key);
}

// Redis Adapter Implementation
@Component
@Slf4j
public class RedisCacheAdapter implements CacheGateway {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisCacheAdapter(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "getFallback")
    @TimeLimiter(name = "redisCache")
    public <T> Optional<T> get(String key, Class<T> type) {
        // [DEBUG] Cache lookup - only for troubleshooting, disabled in production
        log.debug("[Cache-Adapter][Get] Lookup. key={}", key);

        try {
            // 1. Validate key (Business validation)
            validateKey(key);

            // 2. Get from Redis
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                // [DEBUG] Cache miss - use DEBUG to avoid noise
                log.debug("[Cache-Adapter][Get] Cache miss. key={}", key);
                return Optional.empty();
            }

            // 3. Deserialize
            T result = objectMapper.convertValue(value, type);
            
            // [DEBUG] Cache hit - use DEBUG to avoid noise
            log.debug("[Cache-Adapter][Get] Cache hit. key={}", key);
            return Optional.of(result);

        } catch (IllegalArgumentException e) {
            // [ERROR] Business exception - invalid key format
            log.error("[Cache-Adapter][Get] Invalid key format! key={}, cause={}", 
                key, e.getMessage());
            throw new CacheKeyValidationException("Invalid key format", e);

        } catch (SerializationException e) {
            // [WARN] Serialization exception - treat as cache miss, don't break flow
            log.warn("[Cache-Adapter][Get] Deserialization failed, treating as miss. key={}, cause={}", 
                key, e.getMessage());
            return Optional.empty(); // Treat as cache miss

        } catch (RedisConnectionFailureException e) {
            // [WARN] Connection exception - Redis unavailable, will fallback
            log.warn("[Cache-Adapter][Get] Connection failed, cache unavailable. key={}, cause={}", 
                key, e.getMessage());
            throw new CacheConnectionException("Redis unavailable", e);

        } catch (QueryTimeoutException e) {
            // [WARN] Timeout exception - cache query timeout
            log.warn("[Cache-Adapter][Get] Query timeout. key={}, cause={}", 
                key, e.getMessage());
            throw new CacheTimeoutException("Cache timeout", e);

        } catch (Exception e) {
            // [WARN] Unexpected exception - degrade gracefully, don't break business flow
            log.warn("[Cache-Adapter][Get] Unexpected error, degrading gracefully. key={}, cause={}", 
                key, e.getMessage());
            return Optional.empty(); // Degrade gracefully
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "putFallback")
    public <T> void put(String key, T value, Duration ttl) {
        // [DEBUG] Cache write - use DEBUG to avoid noise
        log.debug("[Cache-Adapter][Put] Start. key={}, ttl={}", key, ttl);

        try {
            validateKey(key);
            if (value == null) {
                throw new IllegalArgumentException("Cache value cannot be null");
            }

            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("[Cache-Adapter][Put] Success. key={}", key);

        } catch (IllegalArgumentException e) {
            // [ERROR] Invalid cache put request
            log.error("[Cache-Adapter][Put] Invalid request! key={}, cause={}", 
                key, e.getMessage());
            throw new CacheKeyValidationException("Invalid cache put", e);

        } catch (SerializationException e) {
            // [ERROR] Serialization error - data issue
            log.error("[Cache-Adapter][Put] Serialization failed! key={}, cause={}", 
                key, e.getMessage(), e);
            throw new CacheSerializationException("Serialization failed", e);

        } catch (RedisConnectionFailureException e) {
            // [WARN] Redis unavailable - cache write failure should not break business flow
            log.warn("[Cache-Adapter][Put] Connection failed, cache put skipped. key={}, cause={}", 
                key, e.getMessage());
            // Don't throw - cache write failure should not break business flow

        } catch (Exception e) {
            // [WARN] Unexpected error - swallow exception, cache write is not critical
            log.warn("[Cache-Adapter][Put] Unexpected error, cache write skipped. key={}, cause={}", 
                key, e.getMessage());
            // Swallow exception - cache write is not critical
        }
    }

    @Override
    public void delete(String key) {
        try {
            validateKey(key);
            redisTemplate.delete(key);
            log.debug("[Cache-Adapter][Delete] Success. key={}", key);
            
        } catch (Exception e) {
            // [WARN] Cache delete failure is acceptable
            log.warn("[Cache-Adapter][Delete] Failed, cache invalidation skipped. key={}, cause={}", 
                key, e.getMessage());
            // Swallow exception - cache delete failure is acceptable
        }
    }

    // Fallback for get operation
    private <T> Optional<T> getFallback(String key, Class<T> type, Exception e) {
        log.warn("[Cache-Adapter][Fallback] Get fallback triggered. key={}, action=QueryDB", 
            key);
        return Optional.empty(); // Cache miss, query will hit DB
    }

    // Fallback for put operation
    private <T> void putFallback(String key, T value, Duration ttl, Exception e) {
        log.warn("[Cache-Adapter][Fallback] Put fallback triggered. key={}, action=SkipCache", 
            key);
        // Do nothing - cache write failure is acceptable
    }

    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (key.length() > 512) {
            throw new IllegalArgumentException("Cache key too long");
        }
    }
}
```

**Service Usage with Cache-Aside Pattern:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class GoodsService {

    private final GoodsDao goodsDao;
    private final CacheGateway cacheGateway;

    private static final String CACHE_KEY_PREFIX = "goods:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Transactional(value = "tx0", readOnly = true, rollbackFor = Exception.class)
    public GoodsDTO getGoodsByCode(String goodsCode) {
        String cacheKey = CACHE_KEY_PREFIX + goodsCode;
        long startTime = System.currentTimeMillis();

        try {
            // 1. Try cache first
            Optional<GoodsDTO> cached = cacheGateway.get(cacheKey, GoodsDTO.class);
            if (cached.isPresent()) {
                // [INFO] Cache hit - important for monitoring cache effectiveness
                log.info("[Goods-Service][Query] Cache hit. goodsCode={}, duration={}ms", 
                    goodsCode, System.currentTimeMillis() - startTime);
                return cached.get();
            }

            // [INFO] Cache miss - important for SLI tracking
            log.info("[Goods-Service][Query] Cache miss, querying DB. goodsCode={}", goodsCode);

            // 2. Cache miss - query DB
            GoodsDTO goods = goodsDao.findByCode(goodsCode)
                .orElseThrow(() -> new GoodsNotFoundException(goodsCode));

            // 3. Put to cache asynchronously (don't block on cache write)
            CompletableFuture.runAsync(() -> {
                try {
                    cacheGateway.put(cacheKey, goods, CACHE_TTL);
                } catch (Exception e) {
                    // [WARN] Cache write failure - acceptable, don't break flow
                    log.warn("[Goods-Service][Cache] Failed to cache goods. goodsCode={}, cause={}", 
                        goodsCode, e.getMessage());
                }
            });

            log.info("[Goods-Service][Query] DB query success. goodsCode={}, duration={}ms", 
                goodsCode, System.currentTimeMillis() - startTime);
            return goods;

        } catch (CacheConnectionException e) {
            // [WARN] Cache unavailable - query DB directly (degraded mode)
            log.warn("[Goods-Service][Query] Cache unavailable, querying DB directly. goodsCode={}", 
                goodsCode);
            return goodsDao.findByCode(goodsCode)
                .orElseThrow(() -> new GoodsNotFoundException(goodsCode));
        }
    }

    @Transactional(value = "tx0", rollbackFor = Exception.class)
    public void updateGoods(GoodsDTO goods) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Update DB
            goodsDao.update(goods);

            // 2. Invalidate cache (write-through pattern)
            String cacheKey = CACHE_KEY_PREFIX + goods.getGoodsCode();
            try {
                cacheGateway.delete(cacheKey);
            } catch (Exception e) {
                // [WARN] Cache invalidation failure - acceptable
                log.warn("[Goods-Service][Update] Failed to invalidate cache. goodsCode={}, cause={}", 
                    goods.getGoodsCode(), e.getMessage());
                // Don't fail the transaction - cache invalidation is best-effort
            }
            
            log.info("[Goods-Service][Update] Success. goodsCode={}, duration={}ms", 
                goods.getGoodsCode(), System.currentTimeMillis() - startTime);
                
        } catch (Exception e) {
            log.error("[Goods-Service][Update] Failed! goodsCode={}, duration={}ms, cause={}", 
                goods.getGoodsCode(), System.currentTimeMillis() - startTime, e.getMessage(), e);
            throw e;
        }
    }
}
```

---

#### 5.5 File I/O Integration Pattern

**Use Cases:** File upload/download, report generation, log file processing

**Technology Stack:**
- Spring Resource abstraction
- AWS S3 / Azure Blob Storage / Local FileSystem
- Apache Commons IO

**Error Handling Strategy:**
```
Business Exception (Invalid file format, file size exceeded)
  ↓
I/O Exception (Read/write errors, disk full)
  ↓
Connection Exception (S3 unavailable, network error)
  ↓
Timeout Exception (Upload/download timeout)
  ↓
Fallback (Retry, alternative storage, temp storage)
```

**Best Practice Example:**

```java
// Gateway Interface
public interface FileStorageGateway {
    String uploadFile(String fileName, InputStream inputStream, long fileSize);
    InputStream downloadFile(String fileId);
    void deleteFile(String fileId);
}

// S3 Adapter Implementation
@Component
@Slf4j
public class S3FileStorageAdapter implements FileStorageGateway {

    private final AmazonS3 s3Client;
    private final FileStorageProperties properties;

    @Autowired
    public S3FileStorageAdapter(AmazonS3 s3Client, FileStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    @CircuitBreaker(name = "s3Storage", fallbackMethod = "uploadFileFallback")
    @Retry(name = "s3Storage")
    @TimeLimiter(name = "s3Storage")
    public String uploadFile(String fileName, InputStream inputStream, long fileSize) {
        // [INFO] File upload start - include file name, size, traceId
        log.info("[S3-Adapter][Upload] Start. fileName={}, size={}bytes, traceId={}", 
            fileName, fileSize, MDC.get("traceId"));
        long startTime = System.currentTimeMillis();

        try {
            // 1. Business validation
            validateFile(fileName, fileSize);

            // 2. Generate unique file ID
            String fileId = generateFileId(fileName);
            String s3Key = buildS3Key(fileId);

            // 3. Prepare metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(detectContentType(fileName));

            // 4. Upload to S3
            PutObjectRequest putRequest = new PutObjectRequest(
                properties.getBucketName(),
                s3Key,
                inputStream,
                metadata
            );

            s3Client.putObject(putRequest);
            
            // [INFO] Upload success - include fileId and duration
            log.info("[S3-Adapter][Upload] Success. fileId={}, fileName={}, duration={}ms", 
                fileId, fileName, System.currentTimeMillis() - startTime);

            return fileId;

        } catch (IllegalArgumentException e) {
            // [ERROR] Business exception - invalid file
            log.error("[S3-Adapter][Upload] Validation failed! fileName={}, size={}bytes, cause={}", 
                fileName, fileSize, e.getMessage());
            throw new FileValidationException("Invalid file", e);

        } catch (AmazonServiceException e) {
            // [ERROR/WARN] AWS service exception
            if (e.getStatusCode() == 403) {
                log.error("[S3-Adapter][Upload] Access denied! fileName={}, status={}", 
                    fileName, e.getStatusCode());
                throw new FileStoragePermissionException("Access denied", e);
            } else if (e.getStatusCode() >= 500) {
                // [WARN] 5xx errors - may retry
                log.warn("[S3-Adapter][Upload] S3 server error, will retry. fileName={}, status={}, duration={}ms", 
                    fileName, e.getStatusCode(), System.currentTimeMillis() - startTime);
                throw new FileStorageServerException("S3 server error", e);
            }
            log.error("[S3-Adapter][Upload] S3 service error! fileName={}, status={}, errorCode={}", 
                fileName, e.getStatusCode(), e.getErrorCode());
            throw new FileStorageException("S3 error", e);

        } catch (AmazonClientException e) {
            // [WARN] Connection/network exception - may retry
            log.warn("[S3-Adapter][Upload] Connection failed, will retry. fileName={}, duration={}ms, cause={}", 
                fileName, System.currentTimeMillis() - startTime, e.getMessage());
            throw new FileStorageConnectionException("S3 connection failed", e);

        } catch (IOException e) {
            // [ERROR] I/O exception
            log.error("[S3-Adapter][Upload] File I/O error! fileName={}, cause={}", 
                fileName, e.getMessage(), e);
            throw new FileIOException("File read error", e);

        } catch (Exception e) {
            // [ERROR] Unexpected exception
            log.error("[S3-Adapter][Upload] Unexpected error! fileName={}, size={}bytes, cause={}", 
                fileName, fileSize, e.getMessage(), e);
            throw new FileStorageException("Upload failed", e);

        } finally {
            // Always close input stream
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("[S3-Adapter][Upload] Failed to close input stream. fileName={}", fileName);
            }
        }
    }

    @Override
    @CircuitBreaker(name = "s3Storage", fallbackMethod = "downloadFileFallback")
    @TimeLimiter(name = "s3Storage")
    public InputStream downloadFile(String fileId) {
        // [INFO] File download start
        log.info("[S3-Adapter][Download] Start. fileId={}, traceId={}", 
            fileId, MDC.get("traceId"));
        long startTime = System.currentTimeMillis();

        try {
            validateFileId(fileId);
            String s3Key = buildS3Key(fileId);

            S3Object s3Object = s3Client.getObject(
                properties.getBucketName(), 
                s3Key
            );

            log.info("[S3-Adapter][Download] Success. fileId={}, duration={}ms", 
                fileId, System.currentTimeMillis() - startTime);
            return s3Object.getObjectContent();

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                // [WARN] File not found - expected scenario
                log.warn("[S3-Adapter][Download] File not found. fileId={}", fileId);
                throw new FileNotFoundException("File not found: " + fileId);
            }
            // [ERROR] Other S3 exceptions
            log.error("[S3-Adapter][Download] S3 error! fileId={}, status={}", 
                fileId, e.getStatusCode());
            throw new FileStorageException("Download failed", e);

        } catch (AmazonClientException e) {
            // [WARN] Connection error - may retry
            log.warn("[S3-Adapter][Download] Connection failed. fileId={}, duration={}ms, cause={}", 
                fileId, System.currentTimeMillis() - startTime, e.getMessage());
            throw new FileStorageConnectionException("S3 unavailable", e);

        } catch (Exception e) {
            // [ERROR] Unexpected error
            log.error("[S3-Adapter][Download] Unexpected error! fileId={}, cause={}", 
                fileId, e.getMessage(), e);
            throw new FileStorageException("Download failed", e);
        }
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            validateFileId(fileId);
            String s3Key = buildS3Key(fileId);

            s3Client.deleteObject(properties.getBucketName(), s3Key);
            log.info("[S3-Adapter][Delete] Success. fileId={}", fileId);

        } catch (Exception e) {
            // [WARN] Delete failure is acceptable - idempotent operation
            log.warn("[S3-Adapter][Delete] Failed, operation skipped. fileId={}, cause={}", 
                fileId, e.getMessage());
            // Swallow exception - file delete is idempotent
        }
    }

    // Fallback: Save to local temp storage for retry
    private String uploadFileFallback(String fileName, InputStream inputStream, 
                                     long fileSize, Exception e) {
        // [ERROR] Fallback to local temp storage
        log.error("[S3-Adapter][Fallback] S3 unavailable, saving to local temp. fileName={}, triggerCause={}", 
            fileName, e.getClass().getSimpleName());

        try {
            String fileId = generateFileId(fileName);
            Path tempPath = Paths.get(properties.getTempDirectory(), fileId);

            // Save to local filesystem
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            
            // [INFO] Temp storage success
            log.info("[S3-Adapter][TempStorage] Saved. fileId={}, path={}, action=QueueForRetry", 
                fileId, tempPath);

            // Queue for background upload to S3
            queueFileForRetry(fileId, tempPath.toString());

            return fileId;

        } catch (IOException ioException) {
            // [ERROR] Critical - both S3 and local storage failed (P0 Alert)
            log.error("[S3-Adapter][Fallback] Failed to save to temp! Data loss risk! fileName={}", 
                fileName, ioException);
            throw new CriticalFileStorageException("File storage failed completely", ioException);
        }
    }

    private InputStream downloadFileFallback(String fileId, Exception e) {
        // [WARN] Fallback to local temp storage
        log.warn("[S3-Adapter][Fallback] S3 unavailable, checking temp storage. fileId={}", fileId);

        try {
            Path tempPath = Paths.get(properties.getTempDirectory(), fileId);
            if (Files.exists(tempPath)) {
                log.info("[S3-Adapter][TempStorage] File found. fileId={}", fileId);
                return Files.newInputStream(tempPath);
            }
        } catch (IOException ioException) {
            log.error("[S3-Adapter][TempStorage] Failed to read. fileId={}", fileId, ioException);
        }

        // [ERROR] File not available anywhere
        log.error("[S3-Adapter][Fallback] File not available! fileId={}", fileId);
        throw new FileNotFoundException("File not available: " + fileId);
    }

    private void validateFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        if (fileSize > properties.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds limit: " + properties.getMaxFileSize());
        }
        if (!isAllowedFileType(fileName)) {
            throw new IllegalArgumentException("File type not allowed: " + fileName);
        }
    }

    private void validateFileId(String fileId) {
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("File ID is required");
        }
    }

    private String generateFileId(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }

    private String buildS3Key(String fileId) {
        return properties.getKeyPrefix() + "/" + fileId;
    }

    private String detectContentType(String fileName) {
        // Use Apache Tika or simple extension mapping
        return URLConnection.guessContentTypeFromName(fileName);
    }

    private boolean isAllowedFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return properties.getAllowedExtensions().contains(extension);
    }

    private void queueFileForRetry(String fileId, String localPath) {
        // Store to DB for background worker to retry upload
        log.info("Queuing file for retry: {}", fileId);
        // Implementation: save to file_upload_queue table
    }
}
```

---

#### 5.6 Resilience Configuration

**Centralized Resilience4j Configuration:**

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 30s
        failureRateThreshold: 50
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 60s
    instances:
      paymentService:
        baseConfig: default
        failureRateThreshold: 60
      kafkaPublisher:
        baseConfig: default
        failureRateThreshold: 70
      redisCache:
        baseConfig: default
        failureRateThreshold: 80
        recordExceptions:
          - com.example.exception.CacheConnectionException
      s3Storage:
        baseConfig: default
        failureRateThreshold: 50

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        enableExponentialBackoff: true
    instances:
      paymentService:
        baseConfig: default
        retryExceptions:
          - org.springframework.web.client.ResourceAccessException
          - com.example.exception.PaymentServerException
        ignoreExceptions:
          - com.example.exception.PaymentBusinessException
      kafkaPublisher:
        baseConfig: default
        maxAttempts: 5
      s3Storage:
        baseConfig: default
        maxAttempts: 3

  timelimiter:
    configs:
      default:
        timeoutDuration: 10s
    instances:
      paymentService:
        timeoutDuration: 15s
      kafkaPublisher:
        timeoutDuration: 5s
      redisCache:
        timeoutDuration: 2s
      s3Storage:
        timeoutDuration: 30s

  bulkhead:
    instances:
      paymentService:
        maxConcurrentCalls: 10
        maxWaitDuration: 5s
      kafkaPublisher:
        maxConcurrentCalls: 20
      s3Storage:
        maxConcurrentCalls: 5
```

---

#### 5.7 Monitoring and Alerting

**Key Metrics to Monitor:**

1. **Circuit Breaker State**: Open/Closed/Half-Open
2. **Retry Count**: Number of retries per operation
3. **Timeout Rate**: Percentage of operations timing out
4. **Fallback Trigger Rate**: How often fallback is executed
5. **Error Rate by Type**: Business/IO/Timeout/Protocol exceptions
6. **Response Time**: P50, P95, P99 latencies

**Spring Boot Actuator Endpoints:**
```
GET /actuator/health
GET /actuator/metrics/resilience4j.circuitbreaker.calls
GET /actuator/metrics/resilience4j.retry.calls
```

**Exception Hierarchy Summary:**

```
ExternalIntegrationException (base)
├── BusinessException (4xx, validation errors) - DON'T RETRY
├── IOException (network, connection) - RETRY
├── TimeoutException (request timeout) - RETRY
├── ProtocolException (HTTP 5xx, Kafka errors) - RETRY
└── SystemException (unexpected errors) - FALLBACK
```

---

### ## 6. Test

- **Responsible**
  - Based on the Junit and Spring Test Frameworks, perform the test cases to make sure the qualiy of all user scenarios.
  - The test mechanism should be idempotent.
- **Test Cases Pattern**
  - **Naming of Test Cases: test${MethodName}_${Scenario} ,
    example: testCreateOrder_success, testCreateOrder_noSuchProductFail**
  - **Naming format : Camel Case**
  - Unit Test
    - test the single unit with mockito framework
    - do not test the dao / repository layer
    - provide the basic happy and failure cases
  - API Integration Test
    - test the api with spring mvc test
    - test data should be idempotent
  - Follow the Gherkin Format to arrange the test methods

---

### ## 7. Project Structure

- Root Project Structure

  ```jsx
  Root
  	- Module1
  		- module1-api
  		- module1-service
  		- module1-dao
  		- module1-common
  	- Module2
  		- module2-api
  		- module2-service
  		- module2-dao
  		- module2-common
  	- ...
  ```

  - Module Structure

    ````jsx
    module1-api
      - src
          - main
              - java
                  - com.ooo.xxx
                      - config
                      - controller
                      - interceptor
                      - listener
                      - scheduler
                      Application
              - test
                 (same package with main)

    module1-service
      - src
          - main
              - java
                  - com.ooo.xxx
                      - service
              - test
                 (same package with main)

    module1-dao
      - src
          - main
              - java
                  - com.ooo.xxx
                      - repository
                      - dao
                      - entity
                      - mapper
                      Application
              - test
                 (same package with main)

    module1-common
      - src
          - main
              - java
                  - com.ooo.xxx
                      - constant
                      - dto
                      - util
              - test
                 (same package with main)
            ```
    ````

---

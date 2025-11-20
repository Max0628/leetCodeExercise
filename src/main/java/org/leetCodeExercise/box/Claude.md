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
      log.info("Finding goods by codes: {}", goodsCodes);

      List<GoodsEntity> entities = goodsRepository.findByGoodsCodes(goodsCodes);

      return entities.stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList());
    }

    // ✅ CORRECT: Write transaction for update operations
    @Transactional(value = "tx0", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateGoods(GoodsDTO dto) {
      log.info("Updating goods: {}", dto.getGoodsCode());

      GoodsEntity entity = goodsRepository.findById(dto.getGoodsCode())
          .orElseThrow(() -> new GoodsNotFoundException(dto.getGoodsCode()));

      // Update entity
      entity.setGoodsName(dto.getGoodsName());
      entity.setSalePrice(dto.getSalePrice());

      goodsRepository.update(entity);
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
          try {
              return oseTemplate.query(sql, mapper, args);
          } catch (Exception e) {
              log.error("OSE query failed, switching to EXA", e);
              return exaTemplate.query(sql, mapper, args);
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
        log.info("Processing payment for order: {}", request.getOrderNo());

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

            // 4. Map API response to Domain object
            return mapToPaymentResult(response.getBody());

        } catch (PaymentBusinessException e) {
            // Business exception - do not retry
            log.error("Payment business error: {}", e.getMessage());
            throw e;

        } catch (ResourceAccessException e) {
            // I/O Exception (network, connection errors)
            log.error("Payment I/O error: {}", e.getMessage(), e);
            throw new PaymentIOException("Network error during payment", e);

        } catch (HttpClientErrorException e) {
            // HTTP 4xx errors
            log.error("Payment client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentValidationException("Invalid payment request", e);

        } catch (HttpServerErrorException e) {
            // HTTP 5xx errors (may retry)
            log.error("Payment server error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentServerException("Payment service unavailable", e);

        } catch (Exception e) {
            // Unexpected exception
            log.error("Unexpected payment error", e);
            throw new PaymentSystemException("Payment system error", e);
        }
    }

    // Fallback method for circuit breaker
    private PaymentResult paymentFallback(PaymentRequest request, Exception e) {
        log.error("Payment fallback triggered for order: {}, reason: {}", 
            request.getOrderNo(), e.getMessage());

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
        log.info("Publishing order created event: {}", event.getOrderNo());

        try {
            // 1. Validate event (Business validation)
            validateEvent(event);

            // 2. Send to Kafka
            ListenableFuture<SendResult<String, OrderCreatedEvent>> future = 
                kafkaTemplate.send(orderCreatedTopic, event.getOrderNo(), event);

            // 3. Add callback for async result
            future.addCallback(
                result -> {
                    log.info("Order event published successfully: {} to partition: {}",
                        event.getOrderNo(), result.getRecordMetadata().partition());
                },
                ex -> {
                    handlePublishFailure(event, ex);
                }
            );

        } catch (IllegalArgumentException e) {
            // Business exception - invalid event
            log.error("Invalid order event: {}", e.getMessage());
            throw new OrderEventValidationException("Invalid event data", e);

        } catch (SerializationException e) {
            // Serialization exception
            log.error("Event serialization error: {}", e.getMessage(), e);
            throw new OrderEventSerializationException("Failed to serialize event", e);

        } catch (Exception e) {
            // Kafka connection or timeout exception
            log.error("Failed to publish order event", e);
            throw new OrderEventPublishException("Event publish failed", e);
        }
    }

    // Fallback: Store to database for retry
    private void publishOrderCreatedFallback(OrderCreatedEvent event, Exception e) {
        log.error("Kafka publish fallback triggered for order: {}, storing to DB", 
            event.getOrderNo(), e);

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
            log.info("Order event stored to outbox: {}", event.getOrderNo());

        } catch (Exception dbException) {
            log.error("Failed to store event to outbox, data loss risk!", dbException);
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
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

        log.info("Received order created event: {} from partition: {}", event.getOrderNo(), partition);

        try {
            // 1. Business validation
            if (event.getOrderNo() == null) {
                throw new OrderEventValidationException("Order number is missing");
            }

            // 2. Process event
            orderService.processOrderCreatedEvent(event);
            log.info("Order event processed successfully: {}", event.getOrderNo());

        } catch (OrderEventValidationException e) {
            // Business exception - send to DLT, don't retry
            log.error("Invalid order event, sending to DLT: {}", e.getMessage());
            throw e;

        } catch (DataAccessException e) {
            // DB exception - retry
            log.error("Database error processing event, will retry", e);
            throw new OrderEventProcessingException("DB error", e);

        } catch (Exception e) {
            // Unexpected error - retry
            log.error("Unexpected error processing event", e);
            throw new OrderEventProcessingException("Processing failed", e);
        }
    }

    @DltHandler
    public void handleDlt(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        
        log.error("Message sent to DLT: order={}, error={}", 
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
        log.debug("Getting cache value for key: {}", key);

        try {
            // 1. Validate key (Business validation)
            validateKey(key);

            // 2. Get from Redis
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }

            // 3. Deserialize
            T result = objectMapper.convertValue(value, type);
            log.debug("Cache hit for key: {}", key);
            return Optional.of(result);

        } catch (IllegalArgumentException e) {
            // Business exception - invalid key format
            log.error("Invalid cache key: {}", e.getMessage());
            throw new CacheKeyValidationException("Invalid key format", e);

        } catch (SerializationException e) {
            // Serialization exception
            log.error("Cache deserialization error for key: {}", key, e);
            return Optional.empty(); // Treat as cache miss

        } catch (RedisConnectionFailureException e) {
            // Connection exception
            log.error("Redis connection failed, cache unavailable", e);
            throw new CacheConnectionException("Redis unavailable", e);

        } catch (QueryTimeoutException e) {
            // Timeout exception
            log.error("Redis query timeout for key: {}", key, e);
            throw new CacheTimeoutException("Cache timeout", e);

        } catch (Exception e) {
            // Unexpected exception
            log.error("Unexpected cache error for key: {}", key, e);
            return Optional.empty(); // Degrade gracefully
        }
    }

    @Override
    @CircuitBreaker(name = "redisCache", fallbackMethod = "putFallback")
    public <T> void put(String key, T value, Duration ttl) {
        log.debug("Putting cache value for key: {}, ttl: {}", key, ttl);

        try {
            validateKey(key);
            if (value == null) {
                throw new IllegalArgumentException("Cache value cannot be null");
            }

            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cache value set successfully for key: {}", key);

        } catch (IllegalArgumentException e) {
            log.error("Invalid cache put request: {}", e.getMessage());
            throw new CacheKeyValidationException("Invalid cache put", e);

        } catch (SerializationException e) {
            log.error("Cache serialization error for key: {}", key, e);
            throw new CacheSerializationException("Serialization failed", e);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis unavailable, cache put skipped for key: {}", key);
            // Don't throw - cache write failure should not break business flow

        } catch (Exception e) {
            log.error("Unexpected error putting cache for key: {}", key, e);
            // Swallow exception - cache write is not critical
        }
    }

    @Override
    public void delete(String key) {
        try {
            validateKey(key);
            redisTemplate.delete(key);
            log.debug("Cache deleted for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting cache for key: {}", key, e);
            // Swallow exception - cache delete failure is acceptable
        }
    }

    // Fallback for get operation
    private <T> Optional<T> getFallback(String key, Class<T> type, Exception e) {
        log.warn("Redis cache fallback triggered for key: {}, error: {}", 
            key, e.getMessage());
        return Optional.empty(); // Cache miss, query will hit DB
    }

    // Fallback for put operation
    private <T> void putFallback(String key, T value, Duration ttl, Exception e) {
        log.warn("Redis cache put fallback, skipping cache for key: {}", key);
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

        try {
            // 1. Try cache first
            Optional<GoodsDTO> cached = cacheGateway.get(cacheKey, GoodsDTO.class);
            if (cached.isPresent()) {
                log.info("Goods found in cache: {}", goodsCode);
                return cached.get();
            }

            // 2. Cache miss - query DB
            log.info("Cache miss, querying DB for goods: {}", goodsCode);
            GoodsDTO goods = goodsDao.findByCode(goodsCode)
                .orElseThrow(() -> new GoodsNotFoundException(goodsCode));

            // 3. Put to cache asynchronously (don't block on cache write)
            CompletableFuture.runAsync(() -> {
                try {
                    cacheGateway.put(cacheKey, goods, CACHE_TTL);
                } catch (Exception e) {
                    log.error("Failed to cache goods: {}", goodsCode, e);
                }
            });

            return goods;

        } catch (CacheConnectionException e) {
            // Cache unavailable - query DB directly (degraded mode)
            log.warn("Cache unavailable, querying DB directly: {}", goodsCode);
            return goodsDao.findByCode(goodsCode)
                .orElseThrow(() -> new GoodsNotFoundException(goodsCode));
        }
    }

    @Transactional(value = "tx0", rollbackFor = Exception.class)
    public void updateGoods(GoodsDTO goods) {
        // 1. Update DB
        goodsDao.update(goods);

        // 2. Invalidate cache (write-through pattern)
        String cacheKey = CACHE_KEY_PREFIX + goods.getGoodsCode();
        try {
            cacheGateway.delete(cacheKey);
        } catch (Exception e) {
            log.error("Failed to invalidate cache for goods: {}", goods.getGoodsCode(), e);
            // Don't fail the transaction - cache invalidation is best-effort
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
        log.info("Uploading file: {}, size: {} bytes", fileName, fileSize);

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
            log.info("File uploaded successfully: {}", fileId);

            return fileId;

        } catch (IllegalArgumentException e) {
            // Business exception - invalid file
            log.error("Invalid file upload request: {}", e.getMessage());
            throw new FileValidationException("Invalid file", e);

        } catch (AmazonServiceException e) {
            // AWS service exception (4xx/5xx)
            log.error("S3 service error: {} - {}", e.getStatusCode(), e.getErrorMessage());
            if (e.getStatusCode() == 403) {
                throw new FileStoragePermissionException("Access denied", e);
            } else if (e.getStatusCode() >= 500) {
                throw new FileStorageServerException("S3 server error", e);
            }
            throw new FileStorageException("S3 error", e);

        } catch (AmazonClientException e) {
            // Connection/network exception
            log.error("S3 client error: {}", e.getMessage(), e);
            throw new FileStorageConnectionException("S3 connection failed", e);

        } catch (IOException e) {
            // I/O exception
            log.error("File I/O error: {}", e.getMessage(), e);
            throw new FileIOException("File read error", e);

        } catch (Exception e) {
            // Unexpected exception
            log.error("Unexpected file upload error", e);
            throw new FileStorageException("Upload failed", e);

        } finally {
            // Always close input stream
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Failed to close input stream", e);
            }
        }
    }

    @Override
    @CircuitBreaker(name = "s3Storage", fallbackMethod = "downloadFileFallback")
    @TimeLimiter(name = "s3Storage")
    public InputStream downloadFile(String fileId) {
        log.info("Downloading file: {}", fileId);

        try {
            validateFileId(fileId);
            String s3Key = buildS3Key(fileId);

            S3Object s3Object = s3Client.getObject(
                properties.getBucketName(), 
                s3Key
            );

            log.info("File downloaded successfully: {}", fileId);
            return s3Object.getObjectContent();

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                log.error("File not found: {}", fileId);
                throw new FileNotFoundException("File not found: " + fileId);
            }
            throw new FileStorageException("Download failed", e);

        } catch (AmazonClientException e) {
            log.error("S3 client error downloading file: {}", fileId, e);
            throw new FileStorageConnectionException("S3 unavailable", e);

        } catch (Exception e) {
            log.error("Unexpected error downloading file: {}", fileId, e);
            throw new FileStorageException("Download failed", e);
        }
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            validateFileId(fileId);
            String s3Key = buildS3Key(fileId);

            s3Client.deleteObject(properties.getBucketName(), s3Key);
            log.info("File deleted successfully: {}", fileId);

        } catch (Exception e) {
            log.error("Error deleting file: {}", fileId, e);
            // Swallow exception - file delete is idempotent
        }
    }

    // Fallback: Save to local temp storage for retry
    private String uploadFileFallback(String fileName, InputStream inputStream, 
                                     long fileSize, Exception e) {
        log.error("S3 upload fallback triggered for file: {}, saving to local temp", fileName, e);

        try {
            String fileId = generateFileId(fileName);
            Path tempPath = Paths.get(properties.getTempDirectory(), fileId);

            // Save to local filesystem
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to temp storage: {}", tempPath);

            // Queue for background upload to S3
            queueFileForRetry(fileId, tempPath.toString());

            return fileId;

        } catch (IOException ioException) {
            log.error("Failed to save file to temp storage", ioException);
            throw new CriticalFileStorageException("File storage failed completely", ioException);
        }
    }

    private InputStream downloadFileFallback(String fileId, Exception e) {
        log.error("S3 download fallback triggered for file: {}, checking temp storage", fileId, e);

        try {
            Path tempPath = Paths.get(properties.getTempDirectory(), fileId);
            if (Files.exists(tempPath)) {
                log.info("File found in temp storage: {}", fileId);
                return Files.newInputStream(tempPath);
            }
        } catch (IOException ioException) {
            log.error("Failed to read from temp storage", ioException);
        }

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

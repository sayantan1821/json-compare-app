# Testing Guide

## Test Structure

### Unit Tests
- **Service Tests**: `AuthServiceTest`, `CompareServiceTest`
  - Test business logic in isolation using mocks
  - Fast execution, no database required
  - Located in: `src/test/java/com/jsoncompare/service/`

### Integration Tests
- **Controller Tests**: `AuthControllerIntegrationTest`, `CompareControllerIntegrationTest`
  - Test full HTTP layer with MockMvc
  - Use H2 in-memory database
  - Located in: `src/test/java/com/jsoncompare/controller/`

### Repository Tests
- **Repository Tests**: `UserRepositoryTest`
  - Test database queries and operations
  - Use H2 in-memory database
  - Located in: `src/test/java/com/jsoncompare/repository/`

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests AuthServiceTest
```

### Run Tests with Coverage
```bash
./gradlew test jacocoTestReport
```

## Test Configuration

### Test Profile
Tests use the `test` profile which:
- Uses H2 in-memory database
- Disables Swagger UI
- Sets minimal logging

### Test Database
- **H2 Database**: In-memory, auto-created/dropped per test
- **Location**: `src/test/resources/application-test.properties`

## Known Issues

### H2 JSONB Support
H2 database has limited JSONB support. For full compatibility:
- Integration tests may need PostgreSQL test container
- Or use `@SpringBootTest` with real PostgreSQL (slower but more accurate)

### Current Status
- ✅ Unit tests (Service layer) - Working
- ⚠️ Integration tests - May need PostgreSQL for full JSONB support
- ✅ Repository tests - Working with H2

## Test Coverage Goals

- **Services**: 80%+ coverage
- **Controllers**: 70%+ coverage  
- **Repositories**: 60%+ coverage

## Adding New Tests

1. **Unit Test Template**:
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void testMethod_Success() {
        // Arrange, Act, Assert
    }
}
```

2. **Integration Test Template**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MyControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk());
    }
}
```


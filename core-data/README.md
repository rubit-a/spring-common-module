# Core Data - 공통 Data/JPA 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 JPA 인프라 모듈입니다.

## 목표 범위

- 공통 BaseEntity 제공
- JPA Auditing 자동 활성화
- 기본 AuditorAware 제공 (필요 시 교체)

## 제공 구성요소

### 1. BaseEntity / BaseTimeEntity

- `BaseTimeEntity` : `createdAt`, `updatedAt`
- `BaseEntity` : `id`, `version` + `BaseTimeEntity` 상속

```kotlin
@Entity
class SampleEntity(
    var name: String
) : BaseEntity()
```

### 2. JPA Auditing

`core.data.auditing-enabled=true` (기본값)일 때 자동으로 `@EnableJpaAuditing`이 적용됩니다.

Auditor 값이 필요하면 프로젝트에서 `AuditorAware`를 직접 등록하세요.

```kotlin
@Configuration
class AuditorConfig {
    @Bean
    fun auditorAware(): AuditorAware<String> =
        AuditorAware { Optional.of("system") }
}
```

## 설정

```yaml
core:
  data:
    auditing-enabled: true
```

## 참고

- `AuditorAware`를 등록하지 않으면 기본 구현이 `Optional.empty()`를 반환합니다.
- `BaseEntity`의 `version` 필드는 낙관적 락(optimistic locking)에 사용됩니다.

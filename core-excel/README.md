# Core Excel - 공통 Excel 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 Excel 처리 유틸을 모아두는 모듈입니다.

## 포함 의존성

- Apache POI (poi-ooxml)

## 주요 기능

- Workbook/Sheet 빌더 유틸
- 템플릿 기반 엑셀 생성
- 다운로드용 컨트롤러(선택 활성화)

## 사용 방법

`build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation(project(":core-excel"))
}
```

## 유틸 사용 예시

```kotlin
val bytes = workbookBuilderFactory.create()
    .sheet("Users") {
        header(listOf("ID", "Name", "Created"))
        rows(users.map { listOf(it.id, it.name, it.createdAt) })
        autoSizeColumns()
    }
    .toByteArray()
```

## 템플릿/다운로드 사용 예시

템플릿과 데이터 공급자를 등록하면 `/api/excel/{templateId}`로 다운로드가 가능합니다.

```kotlin
class UserExcelTemplate : ExcelTemplate<User> {
    override val templateId = "users"
    override val sheetName = "Users"

    override fun headers() = listOf("ID", "Name", "Status")

    override fun mapRow(item: User) = listOf(item.id, item.name, item.status)

    override fun autoSizeColumns() = true
}

class UserExcelDataProvider(
    private val userService: UserService
) : ExcelDataProvider<User> {
    override val templateId = "users"

    override fun fetch(params: Map<String, String>): List<User> {
        val status = params["status"]
        return userService.findUsers(status)
    }

    override fun fileName(params: Map<String, String>) = "users.xlsx"
}
```

`application.yml`:

```yaml
core:
  excel:
    download:
      enabled: true
      path: /api/excel
```

요청 예시:

```
GET /api/excel/users?status=ACTIVE
```

## 오류 응답 포맷

`core.web.response.enabled=true`일 때 다운로드 예외는 `core-web`의 `ApiResponse` 형식으로 반환됩니다.

```yaml
core:
  web:
    response:
      enabled: true
```

## 설정

```yaml
core:
  excel:
    download:
      enabled: false
      path: /api/excel
      default-file-name: export.xlsx
    style:
      date: yyyy-MM-dd
      date-time: yyyy-MM-dd HH:mm:ss
      time: HH:mm:ss
      header-bold: true
```

## 빌드

```bash
./gradlew :core-excel:build
```

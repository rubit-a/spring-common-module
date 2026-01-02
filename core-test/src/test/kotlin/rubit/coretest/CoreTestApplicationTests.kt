package rubit.coretest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("CoreTest 애플리케이션 기본 테스트")
class CoreTestApplicationTests {

    @Test
    @DisplayName("애플리케이션 컨텍스트가 로드된다")
    fun contextLoads() {
        // Spring Boot 애플리케이션이 정상적으로 시작되는지 확인
    }
}

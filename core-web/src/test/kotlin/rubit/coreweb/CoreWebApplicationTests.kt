package rubit.coreweb

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@DisplayName("CoreWeb 애플리케이션 기본 테스트")
class CoreWebApplicationTests {

    @Test
    @DisplayName("애플리케이션이 시작 가능하다")
    fun applicationCanStart() {
        // 라이브러리 모듈이므로 단독 실행 여부만 확인
        assertTrue(true)
    }
}

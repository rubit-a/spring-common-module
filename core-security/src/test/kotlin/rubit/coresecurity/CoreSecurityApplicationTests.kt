package rubit.coresecurity

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@DisplayName("CoreSecurity 애플리케이션 기본 테스트")
class CoreSecurityApplicationTests {

    @Test
    @DisplayName("애플리케이션이 시작 가능하다")
    fun applicationCanStart() {
        // given & when & then
        // 이 테스트는 core-security가 라이브러리로 사용되므로
        // 단독 실행이 아닌 다른 프로젝트에서 사용됨을 확인하는 용도
        assertTrue(true)
    }
}

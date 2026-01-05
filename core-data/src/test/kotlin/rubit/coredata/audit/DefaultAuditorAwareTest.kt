package rubit.coredata.audit

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DefaultAuditorAware 테스트")
class DefaultAuditorAwareTest {

    @Test
    @DisplayName("기본 구현은 auditor가 비어있다")
    fun defaultAuditorIsEmpty() {
        val auditorAware = DefaultAuditorAware()

        assertTrue(auditorAware.currentAuditor.isEmpty)
    }
}

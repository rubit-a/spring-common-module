package rubit.coretest.coreauth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest(
    properties = [
        "auth.mode=session",
        "auth.password-encoder.strength=12"
    ]
)
@DisplayName("core-security PasswordEncoder 자동 설정 통합 테스트")
class PasswordEncoderAutoConfigurationIntegrationTest(
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    @Test
    @DisplayName("BCryptPasswordEncoder가 자동 등록되고 strength가 적용된다")
    fun passwordEncoderAutoConfigured() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder::class.java)
        val encoded = requireNotNull(passwordEncoder.encode("sample-password")) {
            "Password encoder returned null"
        }
        val cost = encoded.split("$").getOrNull(2) ?: error("Missing bcrypt cost")
        assertThat(cost).isEqualTo("12")
    }
}

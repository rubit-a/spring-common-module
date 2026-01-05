package rubit.coresecurity.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import rubit.coresecurity.TestSecurityApplication

@SpringBootTest(
    classes = [TestSecurityApplication::class],
    properties = ["auth.mode=session"]
)
class PasswordEncoderAutoConfigurationTest(
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    @Test
    fun `provides BCrypt password encoder by default`() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder::class.java)
    }
}

package rubit.testweb.coreauth.config

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import rubit.testweb.coreauth.entity.UserEntity
import rubit.testweb.coreauth.repository.UserRepository

@Configuration
class SessionDataInitializer {

    @Bean
    fun sessionDataSeeder(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): ApplicationRunner {
        return ApplicationRunner {
            if (!userRepository.existsById("testuser")) {
                userRepository.save(
                    UserEntity(
                        username = "testuser",
                        password = requireNotNull(passwordEncoder.encode("password123")) {
                            "Password encoder returned null for testuser"
                        },
                        enabled = true,
                        authorities = mutableSetOf("ROLE_USER")
                    )
                )
            }

            if (!userRepository.existsById("admin")) {
                userRepository.save(
                    UserEntity(
                        username = "admin",
                        password = requireNotNull(passwordEncoder.encode("admin123")) {
                            "Password encoder returned null for admin"
                        },
                        enabled = true,
                        authorities = mutableSetOf("ROLE_ADMIN", "ROLE_USER")
                    )
                )
            }
        }
    }
}

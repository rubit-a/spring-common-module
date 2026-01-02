package rubit.testweb.coreauth.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import rubit.testweb.coreauth.repository.UserRepository

@Service
class JpaUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return User.withUsername(user.username)
            .password(user.password)
            .authorities(*user.authorities.toTypedArray())
            .disabled(!user.enabled)
            .build()
    }
}

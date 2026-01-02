package rubit.testweb.coreauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import rubit.testweb.coreauth.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByUsername(username: String): UserEntity?
}

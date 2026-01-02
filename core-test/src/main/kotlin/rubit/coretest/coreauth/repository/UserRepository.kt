package rubit.coretest.coreauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import rubit.coretest.coreauth.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByUsername(username: String): UserEntity?
}

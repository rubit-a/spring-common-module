package rubit.testweb.commonauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import rubit.testweb.commonauth.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByUsername(username: String): UserEntity?
}

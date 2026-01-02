package rubit.testweb.coreauth.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(length = 50, nullable = false)
    var username: String = "",

    @Column(length = 500, nullable = false)
    var password: String = "",

    @Column(nullable = false)
    var enabled: Boolean = true,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "authorities", joinColumns = [JoinColumn(name = "username")])
    @Column(name = "authority", length = 50, nullable = false)
    var authorities: MutableSet<String> = mutableSetOf()
)

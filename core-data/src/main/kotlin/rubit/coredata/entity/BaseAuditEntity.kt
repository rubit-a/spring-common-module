package rubit.coredata.entity

import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy

@MappedSuperclass
abstract class BaseAuditEntity : BaseEntity() {
    @CreatedBy
    var createdBy: String? = null

    @LastModifiedBy
    var updatedBy: String? = null
}

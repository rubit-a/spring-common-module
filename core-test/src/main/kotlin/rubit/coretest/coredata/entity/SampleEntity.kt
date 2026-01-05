package rubit.coretest.coredata.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import rubit.coredata.entity.BaseAuditEntity

@Entity
@Table(name = "data_samples")
class SampleEntity(
    @Column(nullable = false, length = 100)
    var name: String = ""
) : BaseAuditEntity()

package rubit.coretest.coredata.repository

import org.springframework.data.jpa.repository.JpaRepository
import rubit.coretest.coredata.entity.SampleEntity

interface SampleRepository : JpaRepository<SampleEntity, Long>

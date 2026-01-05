package rubit.coretest.coredata.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import rubit.coretest.coredata.entity.SampleEntity
import rubit.coretest.coredata.repository.SampleRepository
import java.time.Instant

@RestController
@RequestMapping("/api/data/samples")
class SampleController(
    private val sampleRepository: SampleRepository
) {
    @PostMapping
    fun create(@RequestBody request: SampleCreateRequest): SampleResponse {
        val saved = sampleRepository.save(SampleEntity(name = request.name))
        return SampleResponse.from(saved)
    }

    @GetMapping
    fun list(): List<SampleResponse> {
        return sampleRepository.findAll().map(SampleResponse::from)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: SampleUpdateRequest
    ): SampleResponse {
        val entity = sampleRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Sample not found")
        }
        entity.name = request.name
        return SampleResponse.from(sampleRepository.save(entity))
    }
}

data class SampleCreateRequest(
    val name: String
)

data class SampleUpdateRequest(
    val name: String
)

data class SampleResponse(
    val id: Long?,
    val name: String,
    val createdBy: String?,
    val createdAt: Instant?,
    val updatedBy: String?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(entity: SampleEntity): SampleResponse {
            return SampleResponse(
                id = entity.id,
                name = entity.name,
                createdBy = entity.createdBy,
                createdAt = entity.createdAt,
                updatedBy = entity.updatedBy,
                updatedAt = entity.updatedAt
            )
        }
    }
}

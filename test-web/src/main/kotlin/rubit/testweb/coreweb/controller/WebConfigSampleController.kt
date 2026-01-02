package rubit.testweb.coreweb.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/test/web-config")
class WebConfigSampleController {

    @GetMapping("/dto")
    fun dto(): WebConfigSampleDto {
        return WebConfigSampleDto(
            id = 1,
            createdAt = LocalDateTime.of(2025, 1, 1, 10, 15, 30),
            optionalNote = null
        )
    }

    @PostMapping("/dto")
    fun dtoPost(@RequestBody request: WebConfigSampleRequest): WebConfigSampleRequest {
        return request
    }

    @GetMapping("/date")
    fun date(@RequestParam date: LocalDate): Map<String, String> {
        return mapOf("value" to date.format(DateTimeFormatter.ISO_DATE))
    }
}

data class WebConfigSampleDto(
    val id: Long,
    val createdAt: LocalDateTime,
    val optionalNote: String? = null
)

data class WebConfigSampleRequest(
    val id: Long,
    val createdAt: LocalDateTime
)

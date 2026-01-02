package rubit.testweb.coreweb.controller

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import rubit.coreweb.validation.DateRange
import rubit.coreweb.validation.DateRangeType
import rubit.coreweb.validation.EnumValue
import rubit.coreweb.validation.PatternValue
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/api/public/validation")
class ValidationSampleController {

    @GetMapping("/enum")
    fun enumParam(
        @RequestParam
        @EnumValue(enumClass = SampleStatus::class, allowNull = false)
        status: String
    ): Map<String, String> {
        return mapOf("status" to status)
    }

    @GetMapping("/date")
    fun dateParam(
        @RequestParam
        @DateRange(min = "2025-01-01", max = "2025-12-31", type = DateRangeType.DATE)
        date: LocalDate
    ): Map<String, String> {
        return mapOf("date" to date.toString())
    }

    @GetMapping("/pattern")
    fun patternParam(
        @RequestParam
        @PatternValue(regexp = "^[a-z]+$")
        code: String
    ): Map<String, String> {
        return mapOf("code" to code)
    }

    @PostMapping("/body")
    fun body(@Valid @RequestBody request: ValidationSampleRequest): ValidationSampleRequest {
        return request
    }
}

enum class SampleStatus {
    ACTIVE,
    INACTIVE
}

data class ValidationSampleRequest(
    @field:EnumValue(enumClass = SampleStatus::class, allowNull = false)
    val status: String,
    @field:DateRange(min = "2025-01-01", max = "2025-12-31", type = DateRangeType.DATE, allowNull = false)
    val dateText: String
)

package rubit.coretest.coreweb.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rubit.coreweb.response.ApiResponse

@RestController
@RequestMapping("/api/public/core-web")
class CoreWebSampleController {

    @GetMapping("/ping")
    fun ping(): Map<String, String> {
        return mapOf("message" to "pong")
    }

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody request: CoreWebSampleRequest): Map<String, String> {
        return mapOf("name" to request.name)
    }

    @GetMapping("/error")
    fun error(): Map<String, String> {
        throw IllegalArgumentException("invalid request")
    }

    @GetMapping("/raw")
    fun raw(): ApiResponse<Map<String, String>> {
        return ApiResponse.success(mapOf("message" to "raw response"))
    }
}

data class CoreWebSampleRequest(
    @field:NotBlank val name: String
)

package rubit.coretest.coreexcel.controller

import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import rubit.coreexcel.download.ExcelDownloadRequest
import rubit.coreexcel.download.ExcelDownloadResponse
import rubit.coreexcel.download.ExcelDownloadService

@RestController
@RequestMapping("/api/test/excel")
class UserExcelDownloadController(
    private val excelDownloadService: ExcelDownloadService
) {
    @GetMapping("/users")
    fun downloadUsers(@RequestParam(required = false) params: MultiValueMap<String, String>?) =
        ExcelDownloadResponse.toResponseEntity(
            excelDownloadService.download(
                ExcelDownloadRequest("users", params?.toSingleValueMap() ?: emptyMap())
            )
        )

    @GetMapping("/users-report")
    fun downloadUsersReport(@RequestParam(required = false) params: MultiValueMap<String, String>?) =
        ExcelDownloadResponse.toResponseEntity(
            excelDownloadService.download(
                ExcelDownloadRequest("users-report", params?.toSingleValueMap() ?: emptyMap())
            )
        )
}

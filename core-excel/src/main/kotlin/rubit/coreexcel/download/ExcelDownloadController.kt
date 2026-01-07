package rubit.coreexcel.download

import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${core.excel.download.path:/api/excel}")
class ExcelDownloadController(
    private val excelDownloadService: ExcelDownloadService
) {
    @GetMapping("/{templateId}")
    fun download(
        @PathVariable templateId: String,
        @RequestParam params: MultiValueMap<String, String>
    ) = ExcelDownloadResponse.toResponseEntity(
        excelDownloadService.download(
            ExcelDownloadRequest(templateId, params.toSingleValueMap())
        )
    )
}

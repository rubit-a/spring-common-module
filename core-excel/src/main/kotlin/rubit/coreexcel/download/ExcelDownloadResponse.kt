package rubit.coreexcel.download

import java.nio.charset.StandardCharsets
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

object ExcelDownloadResponse {
    private val contentType = MediaType(
        "application",
        "vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

    fun toResponseEntity(result: ExcelDownloadResult): ResponseEntity<ByteArray> {
        val headers = HttpHeaders().apply {
            contentType = ExcelDownloadResponse.contentType
            contentDisposition = ContentDisposition.attachment()
                .filename(result.fileName, StandardCharsets.UTF_8)
                .build()
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(result.content)
    }
}

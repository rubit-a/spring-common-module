package rubit.coreexcel.download

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ExcelDownloadResponse {
    private val contentType = MediaType(
        "application",
        "vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

    fun toResponseEntity(result: ExcelDownloadResult): ResponseEntity<ByteArray> {
        val headers = HttpHeaders().apply {
            contentType = ExcelDownloadResponse.contentType
            add(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(result.fileName))
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(result.content)
    }

    private fun buildContentDisposition(fileName: String): String {
        val asciiFallback = toAsciiFallback(fileName)
        if (asciiFallback == fileName) {
            return "attachment; filename=\"$asciiFallback\""
        }
        val encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            .replace("+", "%20")
        return "attachment; filename=\"$asciiFallback\"; filename*=UTF-8''$encoded"
    }

    private fun toAsciiFallback(fileName: String): String {
        val builder = StringBuilder(fileName.length)
        for (ch in fileName) {
            val code = ch.code
            if (code in 32..126 && ch != '"' && ch != '\\') {
                builder.append(ch)
            } else {
                builder.append('_')
            }
        }
        return builder.toString()
    }
}

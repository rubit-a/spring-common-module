package rubit.coretest.coreexcel

import java.io.ByteArrayInputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import rubit.coretest.coreauth.dto.LoginRequest
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@DisplayName("core-excel download integration tests")
class ExcelDownloadIntegrationTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val excelContentType = MediaType(
        "application",
        "vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @DisplayName("downloads users excel with headers and rows")
    fun downloadsUsersExcel() {
        val accessToken = login("testuser", "password123")

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/excel/users")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(excelContentType))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("users-all.xlsx")))
            .andReturn()

        val bytes = result.response.contentAsByteArray

        ByteArrayInputStream(bytes).use { input ->
            XSSFWorkbook(input).use { workbook ->
                val sheet = workbook.getSheet("Users") ?: error("Missing Users sheet")
                val headerRow = sheet.getRow(0) ?: error("Missing header row")

                assertEquals("Username", headerRow.getCell(0).stringCellValue)
                assertEquals("Enabled", headerRow.getCell(1).stringCellValue)
                assertEquals("Authorities", headerRow.getCell(2).stringCellValue)

                val usernames = mutableSetOf<String>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    val cell = row.getCell(0) ?: continue
                    usernames.add(cell.stringCellValue)
                }

                assertTrue(usernames.contains("testuser"))
                assertTrue(usernames.contains("admin"))
            }
        }
    }

    @Test
    @DisplayName("downloads users excel with enabled filter filename")
    fun downloadsUsersExcelWithEnabledFilter() {
        val accessToken = login("testuser", "password123")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/excel/users")
                .param("enabled", "true")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("users-enabled.xlsx")))
    }

    private fun login(username: String, password: String): String {
        val payload = objectMapper.writeValueAsString(LoginRequest(username, password))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isOk)
            .andReturn()

        val root = objectMapper.readTree(result.response.contentAsString)
        val dataNode = root.path("data")
        val tokenNode = if (dataNode.isMissingNode || dataNode.isNull) root.path("accessToken") else dataNode.path("accessToken")
        return tokenNode.asString()
    }
}

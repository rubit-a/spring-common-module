package rubit.coresecurity.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import rubit.coresecurity.TestSecurityApplication
import org.junit.jupiter.api.BeforeEach

@SpringBootTest(
    classes = [TestSecurityApplication::class, CurrentUserTestController::class],
    properties = ["auth.mode=session"],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class CurrentUserArgumentResolverTest(
    @Autowired private val context: WebApplicationContext
) {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @WithMockUser(username = "alice")
    fun `CurrentUser resolves username`() {
        mockMvc.perform(get("/test/current-user").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("alice"))
    }
}

@RestController
private class CurrentUserTestController {

    @GetMapping("/test/current-user")
    fun currentUser(@CurrentUser username: String?): Map<String, String?> {
        return mapOf("username" to username)
    }
}

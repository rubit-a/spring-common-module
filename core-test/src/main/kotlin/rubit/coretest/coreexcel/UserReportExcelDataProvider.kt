package rubit.coretest.coreexcel

import org.springframework.stereotype.Component
import rubit.coreexcel.template.ExcelDataProvider
import rubit.coretest.coreauth.entity.UserEntity
import rubit.coretest.coreauth.repository.UserRepository

@Component
class UserReportExcelDataProvider(
    private val userRepository: UserRepository
) : ExcelDataProvider<UserEntity> {
    override val templateId: String = "users-report"

    override fun fetch(params: Map<String, String>): List<UserEntity> {
        val enabledFilter = params["enabled"]?.trim()?.lowercase()
        val users = userRepository.findAll()

        return when (enabledFilter) {
            "true" -> users.filter { it.enabled }
            "false" -> users.filter { !it.enabled }
            else -> users
        }
    }

    override fun fileName(params: Map<String, String>): String? {
        return "users-report"
    }
}

package rubit.coretest.coreexcel

import org.springframework.stereotype.Component
import rubit.coreexcel.template.ExcelTemplate
import rubit.coretest.coreauth.entity.UserEntity

@Component
class UserExcelTemplate : ExcelTemplate<UserEntity> {
    override val templateId: String = "users"
    override val sheetName: String = "Users"

    override fun headers(): List<String> {
        return listOf("Username", "Enabled", "Authorities")
    }

    override fun mapRow(item: UserEntity): List<Any?> {
        return listOf(
            item.username,
            item.enabled,
            item.authorities.sorted().joinToString(", ")
        )
    }

    override fun autoSizeColumns(): Boolean = true
}

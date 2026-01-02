package rubit.coreweb.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateRangeValidator::class])
@MustBeDocumented
annotation class DateRange(
    val min: String = "",
    val max: String = "",
    val type: DateRangeType = DateRangeType.AUTO,
    val allowNull: Boolean = true,
    val allowBlank: Boolean = false,
    val message: String = "must be between {min} and {max}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

enum class DateRangeType(val defaultPattern: String?) {
    AUTO(null),
    DATE("yyyy-MM-dd"),
    DATE_TIME("yyyy-MM-dd'T'HH:mm:ss"),
    TIME("HH:mm:ss")
}

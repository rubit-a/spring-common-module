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
@Constraint(validatedBy = [EnumValueValidator::class])
@MustBeDocumented
annotation class EnumValue(
    val enumClass: KClass<out Enum<*>>,
    val ignoreCase: Boolean = false,
    val allowNull: Boolean = true,
    val allowBlank: Boolean = false,
    val message: String = "must be one of {enumClass}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

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
@Constraint(validatedBy = [PatternValueValidator::class])
@MustBeDocumented
annotation class PatternValue(
    val regexp: String,
    val flags: Int = 0,
    val allowNull: Boolean = true,
    val allowBlank: Boolean = false,
    val message: String = "must match \"{regexp}\"",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

package rubit.coreweb.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValueValidator : ConstraintValidator<EnumValue, CharSequence?> {
    private lateinit var allowedValues: Set<String>
    private var ignoreCase: Boolean = false
    private var allowNull: Boolean = true
    private var allowBlank: Boolean = false

    override fun initialize(constraintAnnotation: EnumValue) {
        allowedValues = constraintAnnotation.enumClass.java.enumConstants
            .map { it.name }
            .toSet()
        ignoreCase = constraintAnnotation.ignoreCase
        allowNull = constraintAnnotation.allowNull
        allowBlank = constraintAnnotation.allowBlank
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return allowNull
        }
        val text = value.toString()
        if (text.isBlank()) {
            return allowBlank
        }
        return if (ignoreCase) {
            allowedValues.any { it.equals(text, ignoreCase = true) }
        } else {
            allowedValues.contains(text)
        }
    }
}

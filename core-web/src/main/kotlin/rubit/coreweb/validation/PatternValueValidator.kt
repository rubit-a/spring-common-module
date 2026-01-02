package rubit.coreweb.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class PatternValueValidator : ConstraintValidator<PatternValue, CharSequence?> {
    private lateinit var pattern: Pattern
    private var allowNull: Boolean = true
    private var allowBlank: Boolean = false

    override fun initialize(constraintAnnotation: PatternValue) {
        pattern = Pattern.compile(constraintAnnotation.regexp, constraintAnnotation.flags)
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
        return pattern.matcher(text).matches()
    }
}
